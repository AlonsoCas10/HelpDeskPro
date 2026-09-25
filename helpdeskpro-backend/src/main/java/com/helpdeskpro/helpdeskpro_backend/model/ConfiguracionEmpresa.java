package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class ConfiguracionEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreEmpresa;
    private String moneda;
    private BigDecimal umbralMontoRelevante;

    private Integer cantidadColaboradores;
    private String areasCriticas;
    private String usuariosAreasCriticas;
    private String informacionCritica;

    private String documentosRecibidosDiariamente;
    private Integer promedioOperacionesDiarias;

    private String clientesVip;

    private BigDecimal perdidaVipMin;
    private BigDecimal perdidaVipMax;

    private BigDecimal perdidaNoVipMin;
    private BigDecimal perdidaNoVipMax;

    private String situacionesRiesgosas;

    public ConfiguracionEmpresa() {
    }

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

    public String getUsuariosAreasCriticas() {
        return usuariosAreasCriticas;
    }

    public void setUsuariosAreasCriticas(String usuariosAreasCriticas) {
        this.usuariosAreasCriticas = usuariosAreasCriticas;
    }

    public String getInformacionCritica() {
        return informacionCritica;
    }

    public void setInformacionCritica(String informacionCritica) {
        this.informacionCritica = informacionCritica;
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

    public String getClientesVip() {
        return clientesVip;
    }

    public void setClientesVip(String clientesVip) {
        this.clientesVip = clientesVip;
    }

    public BigDecimal getPerdidaVipMin() {
        return perdidaVipMin;
    }

    public void setPerdidaVipMin(BigDecimal perdidaVipMin) {
        this.perdidaVipMin = perdidaVipMin;
    }

    public BigDecimal getPerdidaVipMax() {
        return perdidaVipMax;
    }

    public void setPerdidaVipMax(BigDecimal perdidaVipMax) {
        this.perdidaVipMax = perdidaVipMax;
    }

    public BigDecimal getPerdidaNoVipMin() {
        return perdidaNoVipMin;
    }

    public void setPerdidaNoVipMin(BigDecimal perdidaNoVipMin) {
        this.perdidaNoVipMin = perdidaNoVipMin;
    }

    public BigDecimal getPerdidaNoVipMax() {
        return perdidaNoVipMax;
    }

    public void setPerdidaNoVipMax(BigDecimal perdidaNoVipMax) {
        this.perdidaNoVipMax = perdidaNoVipMax;
    }

    public String getSituacionesRiesgosas() {
        return situacionesRiesgosas;
    }

    public void setSituacionesRiesgosas(String situacionesRiesgosas) {
        this.situacionesRiesgosas = situacionesRiesgosas;
    }
}
