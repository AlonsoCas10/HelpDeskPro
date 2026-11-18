package com.helpdeskpro.helpdeskpro_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "configuracion_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreEmpresa;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "idMoneda",
        nullable = false,
        referencedColumnName = "id"
    )
    private Moneda moneda;
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
    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "configuracionEmpresa")
    private List<Cliente> clientes = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "configuracionEmpresa")
    private List<Deudor> deudores = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "configuracionEmpresa")
    private List<UsuarioCritico> usuariosCriticos = new ArrayList<>();
}