package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.service.ExcelDeudorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/deudores")
@CrossOrigin(origins = "http://localhost:5173")
public class ExcelDeudorController {

    private final ExcelDeudorService service;

    public ExcelDeudorController(ExcelDeudorService service) {
        this.service = service;
    }

    @PostMapping("/importar")
    public ResponseEntity<String> importarDeudores(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("configuracionEmpresaId") Long configuracionEmpresaId) {

        try {

            // ==========================================
            // DIAGNÓSTICO DEL ARCHIVO RECIBIDO
            // ==========================================

            System.out.println("======================================");
            System.out.println("ARCHIVO RECIBIDO POR SPRING BOOT");
            System.out.println("======================================");

            System.out.println(
                    "Nombre: " + archivo.getOriginalFilename()
            );

            System.out.println(
                    "Tamaño: " + archivo.getSize()
            );

            System.out.println(
                    "Tipo: " + archivo.getContentType()
            );

            System.out.println(
                    "Configuración de empresa ID: "
                            + configuracionEmpresaId
            );

            System.out.println("======================================");

            // ==========================================
            // PROCESAR EXCEL
            // ==========================================

            int procesados =
                    service.importarDeudores(
                            archivo,
                            configuracionEmpresaId
                    );

            return ResponseEntity.ok(
                    "Importación completada. Deudores procesados: "
                            + procesados
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    "No se pudo procesar el archivo Excel: "
                            + e.getMessage()
            );
        }
    }
}
