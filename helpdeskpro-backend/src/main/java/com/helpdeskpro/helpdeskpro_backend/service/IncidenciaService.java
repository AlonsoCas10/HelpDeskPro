package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.enums.CategoriaIncidencia;
import com.helpdeskpro.helpdeskpro_backend.enums.ConfiabilidadImpacto;
import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.model.Incidencia;
import com.helpdeskpro.helpdeskpro_backend.model.ResultadoImpactoFinanciero;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.IncidenciaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final ClienteRepository clienteRepository;
    private final ImpactoFinancieroService impactoFinancieroService;

    public IncidenciaService(
            IncidenciaRepository incidenciaRepository,
            ClienteRepository clienteRepository,
            ImpactoFinancieroService impactoFinancieroService) {
        this.incidenciaRepository = incidenciaRepository;
        this.clienteRepository = clienteRepository;
        this.impactoFinancieroService = impactoFinancieroService;
    }

    @Transactional
    public Incidencia generarDesdeEvento(Evento evento) {
        if (evento == null || evento.getId() == null) {
            return null;
        }

        Optional<Incidencia> existente = incidenciaRepository.findByEventoId(evento.getId());
        if (existente.isPresent()) {
            return existente.get();
        }

        ClienteCorrelacion correlacion = correlacionPorRemitenteAutorizado(evento);
        if (correlacion == null || correlacion.cliente().getMoneda() == null) {
            return null;
        }

        Cliente cliente = correlacion.cliente();
        ConfiabilidadImpacto confiabilidad;
        BigDecimal montoDirecto;

        String moneda = cliente.getMoneda().getNombre();
        montoDirecto = extraerMontoDirecto(evento, moneda);

        if (montoDirecto == null) {
            confiabilidad = ConfiabilidadImpacto.NO_ESTIMABLE;
        } else if (correlacion.porCorreoExacto()) {
            confiabilidad = ConfiabilidadImpacto.CONFIRMADO_POR_CONTEXTO;
        } else {
            confiabilidad = ConfiabilidadImpacto.REFERENCIAL;
        }

        ResultadoImpactoFinanciero resultado = impactoFinancieroService.calcular(
                cliente.getRuc(),
                evento.getRiskScore(),
                montoDirecto
        );

        Incidencia incidencia = Incidencia.builder()
                .id("INC-" + String.format("%03d", evento.getId()))
                .evento(evento)
                .cliente(cliente)
                .perdidaPotencialDirecta(resultado.exposicionDirecta())
                .volumenPotencialPorInterrupcion(resultado.volumenAfectado())
                .perdidaPotencialPorInterrupcion(resultado.perdidaIngresos())
                .categoria(convertirCategoria(resultado.categoria()))
                .fechaCalculo(LocalDateTime.now())
                .moneda(cliente.getMoneda())
                .montoSemanal(resultado.montoSemanal())
                .montoDiario(resultado.montoDiario())
                .comision(resultado.comision())
                .diasOperativos(resultado.diasOperativos())
                .tiempoInterrupcion(resultado.diasInterrupcion())
                .confiabilidadExposicion(confiabilidad)
                .fuentesCalculo(construirFuentes(resultado))
                .explicacionCalculo(construirExplicacion(resultado))
                .build();

        return incidenciaRepository.save(incidencia);
    }

    private ClienteCorrelacion correlacionPorRemitenteAutorizado(Evento evento) {
        if (evento.getCorreoRemitente() != null) {
            Optional<Cliente> porCorreo = clienteRepository
                    .findByCorreoAutorizadoIgnoreCase(evento.getCorreoRemitente().trim());

            if (porCorreo.isPresent()) {
                return new ClienteCorrelacion(porCorreo.get(), true);
            }
        }

        String ruc = extraerRuc(evento);
        if (ruc != null) {
            Optional<Cliente> porRuc = clienteRepository.findByRuc(ruc);

            if (porRuc.isPresent()) {
                return new ClienteCorrelacion(porRuc.get(), true);
            }
        }

        String dominio = evento.getDominioRemitente();
        if (dominio == null || dominio.isBlank()) {
            return null;
        }

        Optional<Cliente> porDominio = clienteRepository
                .findByDominioAutorizadoIgnoreCase(dominio.trim());

        return porDominio
                .map(cliente -> new ClienteCorrelacion(cliente, false))
                .orElse(null);
    }

    private String extraerRuc(Evento evento) {
        String texto = ((evento.getAsunto() == null ? "" : evento.getAsunto()) + " "
                + (evento.getContenidoTexto() == null ? "" : evento.getContenidoTexto()));

        Matcher matcher = Pattern.compile("(?i)\\b(?:RUC\\s*[:#-]?\\s*)?(20\\d{9}|10\\d{9}|15\\d{9}|17\\d{9})\\b")
                .matcher(texto);

        return matcher.find() ? matcher.group(1) : null;
    }

    private BigDecimal extraerMontoDirecto(Evento evento, String monedaCliente) {
        if (monedaCliente == null) {
            return null;
        }

        String texto = ((evento.getAsunto() == null ? "" : evento.getAsunto()) + " "
                + (evento.getContenidoTexto() == null ? "" : evento.getContenidoTexto()));

        String moneda = monedaCliente.toUpperCase(Locale.ROOT);
        Pattern patron;

        if ("PEN".equals(moneda)) {
            patron = Pattern.compile("(?i)(?:S\\s*/\\s*|PEN\\s*)([0-9][0-9.,]*)");
        } else if ("USD".equals(moneda)) {
            patron = Pattern.compile("(?i)(?:USD\\s*|\\$\\s*)([0-9][0-9.,]*)");
        } else {
            patron = Pattern.compile("(?i)" + Pattern.quote(moneda) + "\\s*([0-9][0-9.,]*)");
        }

        Matcher matcher = patron.matcher(texto);
        if (!matcher.find()) {
            return null;
        }

        try {
            return normalizarMonto(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal normalizarMonto(String texto) {
        String valor = texto.replace(" ", "").trim();

        if (valor.contains(".") && valor.contains(",")) {
            char separadorDecimal = valor.lastIndexOf('.') > valor.lastIndexOf(',')
                    ? '.'
                    : ',';
            valor = valor.replace(separadorDecimal == '.' ? "," : ".", "");
            if (separadorDecimal == ',') {
                valor = valor.replace(',', '.');
            }
        } else if (valor.contains(",") || valor.contains(".")) {
            char separador = valor.contains(",") ? ',' : '.';
            int ultimo = valor.lastIndexOf(separador);
            int decimales = valor.length() - ultimo - 1;

            if (decimales == 1 || decimales == 2) {
                valor = valor.substring(0, ultimo).replace(".", "").replace(",", "")
                        + "." + valor.substring(ultimo + 1);
            } else {
                valor = valor.replace(".", "").replace(",", "");
            }
        }

        return new BigDecimal(valor).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private CategoriaIncidencia convertirCategoria(String categoria) {
        if (categoria == null) {
            return null;
        }

        return switch (categoria) {
            case "Medio" -> CategoriaIncidencia.MEDIO;
            case "Alto" -> CategoriaIncidencia.ALTO;
            case "Crítico" -> CategoriaIncidencia.CRITICO;
            default -> CategoriaIncidencia.BAJO;
        };
    }

    private String construirFuentes(ResultadoImpactoFinanciero resultado) {
        if (resultado.fuentes() == null || resultado.fuentes().isEmpty()) {
            return "Sin fuentes disponibles";
        }

        return resultado.fuentes().entrySet().stream()
                .map(entrada -> entrada.getKey() + "=" + entrada.getValue())
                .collect(Collectors.joining("; "));
    }

    private String construirExplicacion(ResultadoImpactoFinanciero resultado) {
        StringBuilder texto = new StringBuilder("Estimación potencial, no pérdida confirmada.");

        if (resultado.montoSemanal() != null) {
            texto.append(" Monto semanal: ").append(resultado.montoSemanal()).append(".");
        }

        if (resultado.diasOperativos() != null) {
            texto.append(" Días operativos: ").append(resultado.diasOperativos()).append(".");
        }

        if (resultado.montoDiario() != null) {
            texto.append(" Monto diario: ").append(resultado.montoDiario()).append(".");
        }

        if (resultado.diasInterrupcion() != null) {
            texto.append(" Días de interrupción: ").append(resultado.diasInterrupcion()).append(".");
        }

        if (resultado.volumenAfectado() != null) {
            texto.append(" Volumen afectado: ").append(resultado.volumenAfectado()).append(".");
        }

        if (resultado.comision() != null) {
            texto.append(" Comisión: ").append(resultado.comision()).append(".");
        }

        if (resultado.perdidaIngresos() != null) {
            texto.append(" Pérdida estimada de ingresos: ").append(resultado.perdidaIngresos()).append(".");
        }

        if (resultado.exposicionDirecta() != null) {
            texto.append(" Exposición directa: ").append(resultado.exposicionDirecta()).append(".");
        }

        return texto.toString();
    }

    private record ClienteCorrelacion(Cliente cliente, boolean porCorreoExacto) {
    }
}
