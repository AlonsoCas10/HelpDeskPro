package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "configuracion_empresa")
public class ConfiguracionEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreEmpresa;
    private String moneda;
    private BigDecimal umbralMontoRelevante;

    private Integer cantidadColaboradores;
    private String areasCriticas;

    private String documentosRecibidosDiariamente;
    private Integer promedioOperacionesDiarias;

    private String situacionesRiesgosas;

    // ==========================================
    // RANGOS DE PÉRDIDA POTENCIAL
    // ==========================================

    private BigDecimal perdidaMinNoVip;
    private BigDecimal perdidaMaxNoVip;

    private BigDecimal perdidaMinVip;
    private BigDecimal perdidaMaxVip;

    // ==========================================
    // RELACIONES
    // ==========================================

    @OneToMany(mappedBy = "configuracionEmpresa")
    private List<Cliente> clientes = new ArrayList<>();

    @OneToMany(mappedBy = "configuracionEmpresa")
    private List<Deudor> deudores = new ArrayList<>();

    @OneToMany(mappedBy = "configuracionEmpresa")
    private List<UsuarioCritico> usuariosCriticos = new ArrayList<>();

    public ConfiguracionEmpresa() {
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getUmbralMontoRelevante() {
        return umbralMontoRelevante;
    }

    public void setUmbralMontoRelevante(BigDecimal umbralMontoRelevante) {
        this.umbralMontoRelevante = umbralMontoRelevante;
    }

    public Integer getCantidadColaboradores() {
        return cantidadColaboradores;
    }

    public void setCantidadColaboradores(Integer cantidadColaboradores) {
        this.cantidadColaboradores = cantidadColaboradores;
    }

    public String getAreasCriticas() {
        return areasCriticas;
    }

    public void setAreasCriticas(String areasCriticas) {
        this.areasCriticas = areasCriticas;
    }

    public String getDocumentosRecibidosDiariamente() {
        return documentosRecibidosDiariamente;
    }

    public void setDocumentosRecibidosDiariamente(String documentosRecibidosDiariamente) {
        this.documentosRecibidosDiariamente = documentosRecibidosDiariamente;
    }

    public Integer getPromedioOperacionesDiarias() {
        return promedioOperacionesDiarias;
    }

    public void setPromedioOperacionesDiarias(Integer promedioOperacionesDiarias) {
        this.promedioOperacionesDiarias = promedioOperacionesDiarias;
    }

    public String getSituacionesRiesgosas() {
        return situacionesRiesgosas;
    }

    public void setSituacionesRiesgosas(String situacionesRiesgosas) {
        this.situacionesRiesgosas = situacionesRiesgosas;
    }

    // ==========================================
    // GETTERS Y SETTERS DE PÉRDIDAS
    // ==========================================

    public BigDecimal getPerdidaMinNoVip() {
        return perdidaMinNoVip;
    }

    public void setPerdidaMinNoVip(BigDecimal perdidaMinNoVip) {
        this.perdidaMinNoVip = perdidaMinNoVip;
    }

    public BigDecimal getPerdidaMaxNoVip() {
        return perdidaMaxNoVip;
    }

    public void setPerdidaMaxNoVip(BigDecimal perdidaMaxNoVip) {
        this.perdidaMaxNoVip = perdidaMaxNoVip;
    }

    public BigDecimal getPerdidaMinVip() {
        return perdidaMinVip;
    }

    public void setPerdidaMinVip(BigDecimal perdidaMinVip) {
        this.perdidaMinVip = perdidaMinVip;
    }

    public BigDecimal getPerdidaMaxVip() {
        return perdidaMaxVip;
    }

    public void setPerdidaMaxVip(BigDecimal perdidaMaxVip) {
        this.perdidaMaxVip = perdidaMaxVip;
    }

    // ==========================================
    // GETTERS Y SETTERS DE RELACIONES
    // ==========================================

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public List<Deudor> getDeudores() {
        return deudores;
    }

    public void setDeudores(List<Deudor> deudores) {
        this.deudores = deudores;
    }

    public List<UsuarioCritico> getUsuariosCriticos() {
        return usuariosCriticos;
    }

    public void setUsuariosCriticos(List<UsuarioCritico> usuariosCriticos) {
        this.usuariosCriticos = usuariosCriticos;
    }
}