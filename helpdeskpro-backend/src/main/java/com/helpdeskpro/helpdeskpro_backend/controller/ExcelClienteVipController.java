package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.service.ExcelClienteVipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:5173")
public class ExcelClienteVipController {

    private final ExcelClienteVipService service;

    public ExcelClienteVipController(ExcelClienteVipService service) {
        this.service = service;
    }

    @PostMapping("/importar-vip")
    public ResponseEntity<String> importarClientesVip(
            @RequestParam("archivo") MultipartFile archivo) {

        try {

            int actualizados = service.importarClientesVip(archivo);

            return ResponseEntity.ok(
                    "Importación completada. Clientes VIP actualizados: "
                            + actualizados
            );

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.internalServerError().body(
                "No se pudo procesar el archivo Excel: " + e.getMessage()
        );
    }
}
}