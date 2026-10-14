package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Deudor;
import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.ConfiguracionEmpresaRepository;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ExcelDeudorService {

    private final DeudorRepository repository;
    private final ConfiguracionEmpresaRepository configuracionEmpresaRepository;

    public ExcelDeudorService(
            DeudorRepository repository,
            ConfiguracionEmpresaRepository configuracionEmpresaRepository) {

        this.repository = repository;
        this.configuracionEmpresaRepository =
                configuracionEmpresaRepository;
    }

    public int importarDeudores(
            MultipartFile archivo,
            Long configuracionEmpresaId) throws Exception {

        System.out.println(
                "===== IMPORTANDO EXCEL DE DEUDORES ====="
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

            // ==========================================
            // PRIMERA HOJA DEL EXCEL
            // ==========================================

            Sheet hoja = workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

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

                // E - Estado
                String estadoTexto = obtenerTexto(
                        fila.getCell(4),
                        formatter
                );


                // ==========================================
                // MOSTRAR DATOS LEÍDOS DEL EXCEL
                // ==========================================

                System.out.println(
                        "===== DATOS CRUDOS DEL EXCEL ====="
                );

                System.out.println(
                        "Columna A - RUC: "
                                + ruc
                );

                System.out.println(
                        "Columna B - Razón social: "
                                + razonSocial
                );

                System.out.println(
                        "Columna C - Dominio: "
                                + dominioAutorizado
                );

                System.out.println(
                        "Columna D - Correo: "
                                + correoAutorizado
                );

                System.out.println(
                        "Columna E - Estado: ["
                                + estadoTexto
                                + "]"
                );

                System.out.println(
                        "=================================="
                );


                // ==========================================
                // VALIDAR RUC
                // ==========================================

                if (ruc.isEmpty()) {

                    System.out.println(
                            "Fila "
                                    + i
                                    + " sin RUC. Se omite."
                    );

                    continue;
                }


                // ==========================================
                // VALIDAR RAZÓN SOCIAL
                // ==========================================

                if (razonSocial.isEmpty()) {

                    System.out.println(
                            "Fila "
                                    + i
                                    + " sin razón social. Se omite."
                    );

                    continue;
                }


                // ==========================================
                // CONVERTIR ESTADO
                // ==========================================

                boolean activo =
                        estadoTexto.equalsIgnoreCase("Activo")
                                || estadoTexto.equalsIgnoreCase("Sí")
                                || estadoTexto.equalsIgnoreCase("Si")
                                || estadoTexto.equalsIgnoreCase("true");


                System.out.println(
                        "Activo convertido a boolean: "
                                + activo
                );


                // ==========================================
                // BUSCAR DEUDOR POR RUC
                // ==========================================

                Optional<Deudor> deudorExistente =
                        repository.findByRuc(ruc);

                Deudor deudor;


                // ==========================================
                // ACTUALIZAR O CREAR
                // ==========================================

                if (deudorExistente.isPresent()) {

                    deudor =
                            deudorExistente.get();

                    System.out.println(
                            "Deudor existente. Se actualizará."
                    );

                } else {

                    deudor =
                            new Deudor();

                    deudor.setRuc(ruc);

                    System.out.println(
                            "Nuevo deudor. Se creará."
                    );
                }


                // ==========================================
                // ASIGNAR DATOS
                // ==========================================

                deudor.setRazonSocial(
                        razonSocial
                );

                deudor.setDominioAutorizado(
                        dominioAutorizado
                );

                deudor.setCorreoAutorizado(
                        correoAutorizado
                );

                deudor.setActivo(
                        activo
                );


                // ==========================================
                // ASIGNAR CONFIGURACIÓN DE EMPRESA
                // ==========================================

                deudor.setConfiguracionEmpresa(
                        configuracion
                );


                // ==========================================
                // GUARDAR EN BASE DE DATOS
                // ==========================================

                repository.save(
                        deudor
                );


                procesados++;
            }

        } finally {

            workbook.close();
        }


        // ==========================================
        // RESULTADO FINAL
        // ==========================================

        System.out.println(
                "Total de deudores procesados: "
                        + procesados
        );

        System.out.println(
                "===== FIN IMPORTACIÓN DE DEUDORES ====="
        );

        return procesados;
    }


    // ==========================================
    // OBTENER TEXTO DE UNA CELDA
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
}
