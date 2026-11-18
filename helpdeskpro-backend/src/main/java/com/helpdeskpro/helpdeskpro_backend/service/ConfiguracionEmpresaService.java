package com.helpdeskpro.helpdeskpro_backend.service;


import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.repository.ConfiguracionEmpresaRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionEmpresaService {

    private final ConfiguracionEmpresaRepository repository;
    private final MonedaService monedaService;

    public ConfiguracionEmpresaService(
            ConfiguracionEmpresaRepository repository,
            MonedaService monedaService) {
        this.repository = repository;
        this.monedaService = monedaService;
    }

    public ConfiguracionEmpresa guardar(ConfiguracionEmpresa configuracion) {
        if (configuracion.getMoneda() == null || configuracion.getMoneda().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una moneda válida.");
        }

        configuracion.setMoneda(
                monedaService.buscarPorId(configuracion.getMoneda().getId())
        );

        return repository.save(configuracion);
    }
}
