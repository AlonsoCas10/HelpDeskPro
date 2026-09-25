package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.FacturaSimulada;
import com.helpdeskpro.helpdeskpro_backend.service.FacturaSimuladaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facturas")
@CrossOrigin(origins = "http://localhost:5173")
public class FacturaSimuladaController {

    private final FacturaSimuladaService service;

    public FacturaSimuladaController(FacturaSimuladaService service) {
        this.service = service;
    }

    @GetMapping
    public List<FacturaSimulada> listar() {
        return service.listar();
    }

    @PostMapping
    public FacturaSimulada guardar(@RequestBody FacturaSimulada factura) {
        return service.guardar(factura);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}