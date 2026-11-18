package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "configuracion_empresa_id")
    private ConfiguracionEmpresa configuracionEmpresa;

    private String razonSocial;
    private String ruc;

    private String dominioAutorizado;
    private String correoAutorizado;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "idMoneda",
        nullable = false,
        referencedColumnName = "id"
    )
    private Moneda moneda;

    @Column(name = "monto_maximo", precision = 10, scale = 2)
    private BigDecimal montoMaximo;

    @Column(name = "monto_semanal", precision = 10, scale = 2)
    private BigDecimal montoSemanal;

    @Column(name = "monto_diario", precision = 10, scale = 2)
    private BigDecimal montoDiario;

    @Column(name = "monto_promedio_operacion", precision = 10, scale = 2)
    private BigDecimal montoPromedioOperacion;

    private boolean vip;
    private boolean activo;

    @Column(name = "comision", precision = 9, scale = 6)
    private BigDecimal comision;

    @Column(name = "dias_operativos")
    private Integer diasOperativos;

    @Column(name = "tiempo_interrupcion")
    private Integer tiempoInterrupcion;
}