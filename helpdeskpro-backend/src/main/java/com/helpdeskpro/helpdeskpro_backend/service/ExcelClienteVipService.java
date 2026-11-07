package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.ConfiguracionEmpresaRepository;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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

                // A - RUC
                String ruc = obtenerTexto(
                        fila.getCell(0),
                        formatter
                );

                // B - Razón social
                String razonSocial = obtenerTexto(
                        fila.getCell(1),
                        formatter
                );

                // C - Dominio autorizado
                String dominioAutorizado = obtenerTexto(
                        fila.getCell(2),
                        formatter
                );

                // D - Correo autorizado
                String correoAutorizado = obtenerTexto(
                        fila.getCell(3),
                        formatter
                );

                // E - Monto promedio habitual
                String montoPromedioTexto = obtenerTexto(
                        fila.getCell(4),
                        formatter
                );

                // F - Monto máximo habitual
                String montoMaximoTexto = obtenerTexto(
                        fila.getCell(5),
                        formatter
                );

                // G - VIP
                String vipTexto = obtenerTexto(
                        fila.getCell(6),
                        formatter
                );

                // H - Estado
                String estadoTexto = obtenerTexto(
                        fila.getCell(7),
                        formatter
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

                BigDecimal montoPromedio =
                        convertirMonto(
                                montoPromedioTexto
                        );

                BigDecimal montoMaximo =
                        convertirMonto(
                                montoMaximoTexto
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
                        "Monto promedio: " + montoPromedio
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

                cliente.setMontoPromedioHabitual(
                        montoPromedio
                );

                cliente.setMontoMaximoHabitual(
                        montoMaximo
                );

                cliente.setVip(vip);

                cliente.setActivo(activo);

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

    // ==========================================
    // MÉTODO PARA CONVERTIR MONTOS
    // ==========================================

    private BigDecimal convertirMonto(
            String texto) {

        if (texto == null || texto.isEmpty()) {
            return BigDecimal.ZERO;
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

            return BigDecimal.ZERO;
        }
    }
}
