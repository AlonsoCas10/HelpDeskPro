package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.ClienteSimulado;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteSimuladoRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ExcelClienteVipService {

    private final ClienteSimuladoRepository repository;

    public ExcelClienteVipService(ClienteSimuladoRepository repository) {
        this.repository = repository;
    }

    public int importarClientesVip(MultipartFile archivo) throws Exception {

        System.out.println("===== IMPORTANDO EXCEL VIP =====");

        int procesados = 0;

        Workbook workbook = WorkbookFactory.create(archivo.getInputStream());

        try {

            Sheet hoja = workbook.getSheetAt(0);

            System.out.println("Cantidad de filas: " + hoja.getLastRowNum());

            DataFormatter formatter = new DataFormatter();

            // Comenzamos desde la fila 1 porque la fila 0 contiene los encabezados
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {

                Row fila = hoja.getRow(i);

                System.out.println("Procesando fila: " + i);

                if (fila == null) {
                    continue;
                }

                // Columna A: RUC
                Cell celdaRuc = fila.getCell(0);

                // Columna B: Razón Social
                Cell celdaRazonSocial = fila.getCell(1);

                if (celdaRuc == null) {
                    System.out.println("Fila sin RUC. Se omite.");
                    continue;
                }

                // Leer RUC
                String ruc = formatter.formatCellValue(celdaRuc).trim();

                // Leer Razón Social
                String razonSocial = "";

                if (celdaRazonSocial != null) {
                    razonSocial = formatter.formatCellValue(celdaRazonSocial).trim();
                }

                System.out.println("RUC leído desde Excel: [" + ruc + "]");
                System.out.println("Razón social: [" + razonSocial + "]");

                // Buscar si el cliente ya existe en la base de datos
                Optional<ClienteSimulado> cliente =
                        repository.findByRuc(ruc);

                if (cliente.isPresent()) {

                    // ==========================================
                    // CASO 1: EL CLIENTE YA EXISTE
                    // ==========================================

                    ClienteSimulado clienteEncontrado = cliente.get();

                    // Lo marcamos como VIP
                    clienteEncontrado.setVip(true);

                    repository.save(clienteEncontrado);

                    System.out.println(
                            "Cliente existente actualizado como VIP."
                    );

                } else {

                    // ==========================================
                    // CASO 2: EL CLIENTE NO EXISTE
                    // ==========================================

                    ClienteSimulado nuevoCliente =
                            new ClienteSimulado();

                    nuevoCliente.setRuc(ruc);
                    nuevoCliente.setRazonSocial(razonSocial);

                    // El cliente importado desde Excel será VIP
                    nuevoCliente.setVip(true);

                    repository.save(nuevoCliente);

                    System.out.println(
                            "Nuevo cliente VIP creado."
                    );
                }

                procesados++;
            }

        } finally {

            workbook.close();
        }

        System.out.println(
                "Total de clientes procesados: " + procesados
        );

        return procesados;
    }
}