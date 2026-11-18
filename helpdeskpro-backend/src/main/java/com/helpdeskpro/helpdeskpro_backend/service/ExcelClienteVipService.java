package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.ConfiguracionEmpresaRepository;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class ExcelClienteVipService {

    private final ClienteRepository repository;
    private final ConfiguracionEmpresaRepository configuracionEmpresaRepository;

    public ExcelClienteVipService(
            ClienteRepository repository,
            ConfiguracionEmpresaRepository configuracionEmpresaRepository) {

        this.repository = repository;
        this.configuracionEmpresaRepository =
                configuracionEmpresaRepository;
    }

    public int importarClientesVip(
            MultipartFile archivo,
            Long configuracionEmpresaId) throws Exception {

        System.out.println(
                "===== IMPORTANDO EXCEL DE CLIENTES ====="
        );

        System.out.println(
                "Configuración de empresa ID: "
                        + configuracionEmpresaId
        );

        int procesados = 0;

        // ==========================================
        // BUSCAR CONFIGURACIÓN DE EMPRESA
        // ==========================================

        ConfiguracionEmpresa configuracion =
                configuracionEmpresaRepository
                        .findById(configuracionEmpresaId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No existe la configuración de empresa con ID: "
                                                + configuracionEmpresaId
                                )
                        );

        Workbook workbook =
                WorkbookFactory.create(
                        archivo.getInputStream()
                );

        try {

            Sheet hoja = workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> columnas = construirMapaColumnas(
                    hoja.getRow(0),
                    formatter
            );

            System.out.println(
                    "Cantidad de filas: "
                            + hoja.getLastRowNum()
            );

            // ==========================================
            // RECORRER FILAS
            // ==========================================

            // Fila 0 = encabezados
            for (int i = 1;
                 i <= hoja.getLastRowNum();
                 i++) {

                Row fila = hoja.getRow(i);

                if (fila == null) {
                    continue;
                }

                System.out.println(
                        "Procesando fila: " + i
                );

                // ==========================================
                // LEER COLUMNAS DEL EXCEL
                // ==========================================

                String ruc = obtenerTexto(
                        fila, columnas, formatter, 0,
                        "ruc"
                );

                String razonSocial = obtenerTexto(
                        fila, columnas, formatter, 1,
                        "razonSocial", "razon social"
                );

                String dominioAutorizado = obtenerTexto(
                        fila, columnas, formatter, 2,
                        "dominioAutorizado", "dominio autorizado"
                );

                String correoAutorizado = obtenerTexto(
                        fila, columnas, formatter, 3,
                        "correoAutorizado", "correo autorizado"
                );

                String montoSemanalTexto = obtenerTexto(
                        fila, columnas, formatter, 4,
                        "montoSemanal", "monto promedio habitual", "monto promedio"
                );

                String montoDiarioTexto = obtenerTexto(
                        fila, columnas, formatter, -1,
                        "montoDiario", "monto diario"
                );

                String montoPromedioOperacionTexto = obtenerTexto(
                        fila, columnas, formatter, -1,
                        "montoPromedioOperacion", "monto promedio por operación"
                );

                String montoMaximoTexto = obtenerTexto(
                        fila, columnas, formatter, 5,
                        "montoMaximo", "monto máximo habitual", "monto máximo"
                );

                String comisionTexto = obtenerTexto(
                        fila, columnas, formatter, -1,
                        "comision", "comisión"
                );

                String diasOperativosTexto = obtenerTexto(
                        fila, columnas, formatter, -1,
                        "diasOperativos", "días operativos"
                );

                String tiempoInterrupcionTexto = obtenerTexto(
                        fila, columnas, formatter, -1,
                        "tiempoInterrupcion",
                        "tiempo interrupción",
                        "tiempo de interrupción",
                        "tiempo de interrupción (días)",
                        "tiempo estimado de interrupción",
                        "tiempo estimado de interrupción (días)",
                        "días de interrupción",
                        "días estimados de interrupción"
                );

                String vipTexto = obtenerTexto(
                        fila, columnas, formatter, 6,
                        "vip"
                );

                String estadoTexto = obtenerTexto(
                        fila, columnas, formatter, 7,
                        "activo", "estado"
                );

                // ==========================================
                // VALIDACIÓN BÁSICA
                // ==========================================

                if (ruc.isEmpty()) {

                    System.out.println(
                            "Fila "
                                    + i
                                    + " sin RUC. Se omite."
                    );

                    continue;
                }

                if (razonSocial.isEmpty()) {

                    System.out.println(
                            "Fila "
                                    + i
                                    + " sin razón social. Se omite."
                    );

                    continue;
                }

                // ==========================================
                // CONVERTIR MONTOS
                // ==========================================

                BigDecimal montoSemanal =
                        convertirMonto(
                                montoSemanalTexto
                        );

                BigDecimal montoDiario = convertirMonto(montoDiarioTexto);

                BigDecimal montoPromedioOperacion = convertirMonto(
                        montoPromedioOperacionTexto
                );

                BigDecimal montoMaximo =
                        convertirMonto(
                                montoMaximoTexto
                        );

                BigDecimal comision = convertirComision(comisionTexto);
                Integer diasOperativos = convertirEntero(diasOperativosTexto);
                Integer tiempoInterrupcion = convertirEntero(
                        tiempoInterrupcionTexto
                );

                // ==========================================
                // CONVERTIR VIP
                // ==========================================

                boolean vip =
                        vipTexto.equalsIgnoreCase("Sí")
                                || vipTexto.equalsIgnoreCase("Si")
                                || vipTexto.equalsIgnoreCase("true");

                // ==========================================
                // CONVERTIR ESTADO
                // ==========================================

                boolean activo =
                        estadoTexto.equalsIgnoreCase("Activo")
                                || estadoTexto.equalsIgnoreCase("Sí")
                                || estadoTexto.equalsIgnoreCase("Si")
                                || estadoTexto.equalsIgnoreCase("true");

                // ==========================================
                // MOSTRAR INFORMACIÓN LEÍDA
                // ==========================================

                System.out.println(
                        "RUC: " + ruc
                );

                System.out.println(
                        "Razón social: " + razonSocial
                );

                System.out.println(
                        "Dominio: " + dominioAutorizado
                );

                System.out.println(
                        "Correo: " + correoAutorizado
                );

                System.out.println(
                        "Monto semanal: " + montoSemanal
                );

                System.out.println(
                        "Monto máximo: " + montoMaximo
                );

                System.out.println(
                        "VIP: " + vip
                );

                System.out.println(
                        "Activo: " + activo
                );

                // ==========================================
                // BUSCAR CLIENTE EXISTENTE
                // ==========================================

                Optional<Cliente> clienteExistente =
                        repository.findByRuc(ruc);

                Cliente cliente;

                if (clienteExistente.isPresent()) {

                    // Si ya existe, actualizamos sus datos
                    cliente =
                            clienteExistente.get();

                    System.out.println(
                            "Cliente existente. Se actualizará."
                    );

                } else {

                    // Si no existe, creamos uno nuevo
                    cliente = new Cliente();

                    cliente.setRuc(ruc);

                    System.out.println(
                            "Nuevo cliente. Se creará."
                    );
                }

                // ==========================================
                // ASIGNAR DATOS
                // ==========================================

                cliente.setRazonSocial(
                        razonSocial
                );

                cliente.setDominioAutorizado(
                        dominioAutorizado
                );

                cliente.setCorreoAutorizado(
                        correoAutorizado
                );

                cliente.setMoneda(configuracion.getMoneda());

                asignarSiExiste(
                        columnas, cliente::setMontoSemanal, montoSemanal,
                        "montosemanal", "montopromediohabitual", "montopromedio"
                );
                asignarSiExiste(
                        columnas, cliente::setMontoDiario, montoDiario,
                        "montodiario"
                );
                asignarSiExiste(
                        columnas, cliente::setMontoPromedioOperacion,
                        montoPromedioOperacion, "montopromediooperacion",
                        "montopromedioporoperacion"
                );
                asignarSiExiste(
                        columnas, cliente::setMontoMaximo, montoMaximo,
                        "montomaximo", "montomaximohabitual"
                );
                asignarSiExiste(
                        columnas, cliente::setComision, comision,
                        "comision"
                );
                asignarSiExiste(
                        columnas, cliente::setDiasOperativos, diasOperativos,
                        "diasoperativos"
                );
                asignarSiExiste(
                        columnas, cliente::setTiempoInterrupcion,
                        tiempoInterrupcion,
                        "tiempointerrupcion",
                        "tiempodeinterrupcion",
                        "tiempodeinterrupciondias",
                        "tiempointerrupcionestimado",
                        "tiempoestimadodeinterrupciondias",
                        "diasdeinterrupcion",
                        "diasestimadosdeinterrupcion"
                );

                if (tieneColumna(columnas, "vip")) {
                    cliente.setVip(vip);
                }

                if (tieneColumna(columnas, "activo", "estado")) {
                    cliente.setActivo(activo);
                }

                // ==========================================
                // ASIGNAR CONFIGURACIÓN DE EMPRESA
                // ==========================================

                cliente.setConfiguracionEmpresa(
                        configuracion
                );

                // ==========================================
                // GUARDAR
                // ==========================================

                repository.save(cliente);

                procesados++;
            }

        } finally {

            workbook.close();
        }

        System.out.println(
                "Total de clientes procesados: "
                        + procesados
        );

        return procesados;
    }

    // ==========================================
    // MÉTODO PARA LEER UNA CELDA COMO TEXTO
    // ==========================================

    private String obtenerTexto(
            Cell celda,
            DataFormatter formatter) {

        if (celda == null) {
            return "";
        }

        return formatter
                .formatCellValue(celda)
                .trim();
    }

        private Map<String, Integer> construirMapaColumnas(
                        Row encabezados,
                        DataFormatter formatter) {

                Map<String, Integer> columnas = new HashMap<>();

                if (encabezados == null) {
                        return columnas;
                }

                for (Cell celda : encabezados) {
                        String encabezado = normalizar(celda, formatter);

                        if (!encabezado.isEmpty()) {
                                columnas.put(encabezado, celda.getColumnIndex());
                        }
                }

                return columnas;
        }

        private String obtenerTexto(
                        Row fila,
                        Map<String, Integer> columnas,
                        DataFormatter formatter,
                        int indiceAnterior,
                        String... nombres) {

                for (String nombre : nombres) {
                        Integer indice = columnas.get(normalizar(nombre));

                        if (indice != null) {
                                return obtenerTexto(fila.getCell(indice), formatter);
                        }
                }

                if (indiceAnterior >= 0) {
                        return obtenerTexto(fila.getCell(indiceAnterior), formatter);
                }

                return "";
        }

        private boolean tieneColumna(
                        Map<String, Integer> columnas,
                        String... nombres) {

                for (String nombre : nombres) {
                        if (columnas.containsKey(normalizar(nombre))) {
                                return true;
                        }
                }

                return false;
        }

        private <T> void asignarSiExiste(
                        Map<String, Integer> columnas,
                        java.util.function.Consumer<T> asignador,
                        T valor,
                        String... nombres) {

                if (tieneColumna(columnas, nombres)) {
                        asignador.accept(valor);
                }
        }

        private String normalizar(
                        Cell celda,
                        DataFormatter formatter) {
                return normalizar(obtenerTexto(celda, formatter));
        }

        private String normalizar(String valor) {
                return java.text.Normalizer
                                .normalize(valor == null ? "" : valor, java.text.Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "")
                                .replaceAll("[^a-zA-Z0-9]", "")
                                .toLowerCase(Locale.ROOT);
        }

    // ==========================================
    // MÉTODO PARA CONVERTIR MONTOS
    // ==========================================

    private BigDecimal convertirMonto(
            String texto) {

        if (texto == null || texto.isEmpty()) {
                        return null;
        }

        try {

            // Permite valores como:
            // 50000
            // 50000.50

            return new BigDecimal(
                    texto.replace(",", "")
            );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Monto inválido: " + texto
            );

                        return null;
        }
    }

        private BigDecimal convertirComision(String texto) {

                if (texto == null || texto.isEmpty()) {
                        return null;
                }

                boolean esPorcentaje = texto.endsWith("%");
                String valor = texto.replace("%", "").replace(",", "").trim();

                try {
                        BigDecimal comision = new BigDecimal(valor);
                        return esPorcentaje
                                        ? comision.movePointLeft(2)
                                        : comision;
                } catch (NumberFormatException e) {
                        System.out.println("Comisión inválida: " + texto);
                        return null;
                }
        }

        private Integer convertirEntero(String texto) {

                if (texto == null || texto.isEmpty()) {
                        return null;
                }

                try {
                        java.util.regex.Matcher coincidencia = java.util.regex.Pattern
                                        .compile("-?\\d+")
                                        .matcher(texto.replace(",", ""));

                        if (!coincidencia.find()) {
                                throw new NumberFormatException();
                        }

                        return Integer.valueOf(coincidencia.group());
                } catch (NumberFormatException e) {
                        System.out.println("Número entero inválido: " + texto);
                        return null;
                }
        }
}
