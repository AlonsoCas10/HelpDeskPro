package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deudor")
public class Deudor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razonSocial;
    private String ruc;

    private String dominioAutorizado;
    private String correoAutorizado;

    private boolean activo;

    @ManyToOne
    @JoinColumn(name = "configuracion_empresa_id")
    private ConfiguracionEmpresa configuracionEmpresa;

    public Deudor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDominioAutorizado() {
        return dominioAutorizado;
    }

    public void setDominioAutorizado(String dominioAutorizado) {
        this.dominioAutorizado = dominioAutorizado;
    }

    public String getCorreoAutorizado() {
        return correoAutorizado;
    }

    public void setCorreoAutorizado(String correoAutorizado) {
        this.correoAutorizado = correoAutorizado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public ConfiguracionEmpresa getConfiguracionEmpresa() {
        return configuracionEmpresa;
    }

    public void setConfiguracionEmpresa(ConfiguracionEmpresa configuracionEmpresa) {
        this.configuracionEmpresa = configuracionEmpresa;
    }
}