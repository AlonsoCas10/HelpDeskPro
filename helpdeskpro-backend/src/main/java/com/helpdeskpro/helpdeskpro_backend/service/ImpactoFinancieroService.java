package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.model.ResultadoImpactoFinanciero;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImpactoFinancieroService {

    private static final int DIAS_OPERATIVOS_POR_DEFECTO = 5;
    private static final BigDecimal VEINTICINCO_PORCIENTO = new BigDecimal("0.25");
    private static final BigDecimal CINCUENTA_PORCIENTO = new BigDecimal("0.50");

    private final ClienteRepository clienteRepository;

    public ImpactoFinancieroService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public ResultadoImpactoFinanciero calcular(
            String ruc,
            Integer riskScore,
            BigDecimal montoDirecto) {

        validarMontoDirecto(montoDirecto);

        Cliente cliente = clienteRepository.findByRuc(ruc)
                .orElse(null);

        if (cliente == null) {
            return noEstimable(
                    "Impacto financiero no estimable por falta de contexto."
            );
        }

        if (!cliente.isActivo()) {
            return noEstimable(
                    "Impacto financiero no estimable para un cliente inactivo."
            );
        }

        String moneda = cliente.getMoneda() == null
                ? null
                : cliente.getMoneda().getNombre();
        String nivelRiesgo = clasificarRiesgo(riskScore);
        List<String> advertencias = new ArrayList<>();
        Map<String, String> fuentes = new LinkedHashMap<>();

        BigDecimal montoSemanal = cliente.getMontoSemanal();
        BigDecimal montoDiario = cliente.getMontoDiario();
        Integer diasOperativos = cliente.getDiasOperativos();

        if (montoSemanal != null) {
            fuentes.put("montoSemanal", "Data Helper");
        }

        if (montoDiario != null) {
            fuentes.put("montoDiario", "Data Helper");
        } else if (montoSemanal != null) {
            diasOperativos = diasOperativos == null || diasOperativos <= 0
                    ? DIAS_OPERATIVOS_POR_DEFECTO
                    : diasOperativos;
            montoDiario = montoSemanal.divide(
                    BigDecimal.valueOf(diasOperativos),
                    2,
                    RoundingMode.HALF_UP
            );
            fuentes.put("montoDiario", "Derivado de Data Helper");
        }

        if (diasOperativos != null) {
            fuentes.put(
                    "diasOperativos",
                    cliente.getDiasOperativos() == null || cliente.getDiasOperativos() <= 0
                            ? "Regla por defecto"
                            : "Data Helper"
            );
        }

        Integer diasInterrupcion = cliente.getTiempoInterrupcion();
        if (diasInterrupcion != null) {
            fuentes.put("diasInterrupcion", "Data Helper");
        } else if (nivelRiesgo != null) {
            diasInterrupcion = diasPorDefecto(nivelRiesgo);
            fuentes.put("diasInterrupcion", "Regla estándar MVP");
            advertencias.add(
                    "Los días de interrupción corresponden a un supuesto estándar del MVP."
            );
        } else {
            advertencias.add(
                    "No se pudo determinar el nivel de riesgo para estimar la interrupción."
            );
        }

        BigDecimal volumenAfectado = null;
        if (montoDiario != null && diasInterrupcion != null) {
            volumenAfectado = montoDiario.multiply(
                    BigDecimal.valueOf(diasInterrupcion)
            );
        } else {
            advertencias.add(
                    "Volumen afectado no estimable por falta de volumen o días de interrupción."
            );
        }

        BigDecimal comision = cliente.getComision();
        if (comision != null) {
            fuentes.put("comision", "Data Helper");
        }

        BigDecimal perdidaIngresos = null;
        if (volumenAfectado != null && comision != null) {
            perdidaIngresos = volumenAfectado.multiply(comision)
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (volumenAfectado != null) {
            advertencias.add(
                    "Pérdida estimada de ingresos no estimable por falta de comisión."
            );
        }

        BigDecimal exposicionDirecta = montoDirecto;
        if (exposicionDirecta != null) {
            fuentes.put("exposicionDirecta", "Evento");
        } else {
            advertencias.add(
                    "Exposición directa no estimable porque el evento no contiene un monto."
            );
        }

        String categoria = determinarCategoria(
                exposicionDirecta,
                volumenAfectado,
                cliente.getConfiguracionEmpresa(),
                advertencias
        );

        boolean estimable = moneda != null
                && (exposicionDirecta != null || volumenAfectado != null);

        if (moneda == null) {
            advertencias.add("Moneda no disponible en el contexto del cliente.");
        }

        String mensaje = estimable
                ? "Impacto financiero estimado; no representa una pérdida confirmada."
                : "Impacto financiero no estimable por falta de información suficiente.";

        return new ResultadoImpactoFinanciero(
                estimable,
                mensaje,
                cliente.getId(),
                cliente.getRuc(),
                cliente.getRazonSocial(),
                moneda,
                nivelRiesgo,
                exposicionDirecta,
                montoSemanal,
                diasOperativos,
                montoDiario,
                diasInterrupcion,
                volumenAfectado,
                comision,
                perdidaIngresos,
                categoria,
                List.copyOf(advertencias),
                Map.copyOf(fuentes)
        );
    }

    private String determinarCategoria(
            BigDecimal exposicionDirecta,
            BigDecimal volumenAfectado,
            ConfiguracionEmpresa configuracion,
            List<String> advertencias) {

        BigDecimal referencia = mayor(exposicionDirecta, volumenAfectado);

        if (referencia == null) {
            return null;
        }

        if (configuracion == null
                || configuracion.getUmbralMontoRelevante() == null
                || configuracion.getUmbralMontoRelevante().signum() <= 0) {
            advertencias.add(
                    "Categoría no estimable porque no existe un umbral económico válido."
            );
            return null;
        }

        BigDecimal umbral = configuracion.getUmbralMontoRelevante();

        if (referencia.compareTo(umbral.multiply(VEINTICINCO_PORCIENTO)) < 0) {
            return "Bajo";
        }

        if (referencia.compareTo(umbral.multiply(CINCUENTA_PORCIENTO)) < 0) {
            return "Medio";
        }

        if (referencia.compareTo(umbral) < 0) {
            return "Alto";
        }

        return "Crítico";
    }

    private BigDecimal mayor(BigDecimal primero, BigDecimal segundo) {
        if (primero == null) {
            return segundo;
        }

        if (segundo == null) {
            return primero;
        }

        return primero.max(segundo);
    }

    private String clasificarRiesgo(Integer riskScore) {
        if (riskScore == null || riskScore < 0) {
            return null;
        }

        if (riskScore <= 24) {
            return "Bajo";
        }

        if (riskScore <= 49) {
            return "Medio";
        }

        if (riskScore <= 74) {
            return "Alto";
        }

        return "Crítico";
    }

    private int diasPorDefecto(String nivelRiesgo) {
        return switch (nivelRiesgo) {
            case "Alto" -> 1;
            case "Crítico" -> 2;
            default -> 0;
        };
    }

    private void validarMontoDirecto(BigDecimal montoDirecto) {
        if (montoDirecto != null && montoDirecto.signum() < 0) {
            throw new IllegalArgumentException(
                    "El monto directo no puede ser negativo."
            );
        }
    }

    private ResultadoImpactoFinanciero noEstimable(String mensaje) {
        return new ResultadoImpactoFinanciero(
                false,
                mensaje,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                Map.of()
        );
    }
}
