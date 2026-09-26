package com.helpdeskpro.helpdeskpro_backend.service;


import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.repository.ConfiguracionEmpresaRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionEmpresaService {

    private final ConfiguracionEmpresaRepository repository;

    public ConfiguracionEmpresaService(ConfiguracionEmpresaRepository repository) {
        this.repository = repository;
    }

    public ConfiguracionEmpresa guardar(ConfiguracionEmpresa configuracion) {
        return repository.save(configuracion);
    }
}
