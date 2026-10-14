package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_criticos")
public class UsuarioCritico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String area;

    @ManyToOne
    @JoinColumn(name = "configuracion_empresa_id")
    private ConfiguracionEmpresa configuracionEmpresa;

    public UsuarioCritico() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public ConfiguracionEmpresa getConfiguracionEmpresa() {
        return configuracionEmpresa;
    }

    public void setConfiguracionEmpresa(ConfiguracionEmpresa configuracionEmpresa) {
        this.configuracionEmpresa = configuracionEmpresa;
    }
}