package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.helpdeskpro.helpdeskpro_backend.enums.CategoriaIncidencia;
import com.helpdeskpro.helpdeskpro_backend.enums.ConfiabilidadImpacto;

@Entity
@Table(name = "incidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidencia {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_evento",
        nullable = false
    )
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_cliente",
        nullable = false
    )
    private Cliente cliente;

    @Column(
        name = "perdida_potencial_directa",
        precision = 10,
        scale = 2
    )
    private BigDecimal perdidaPotencialDirecta;

    @Column(
        name = "volumen_potencial_por_interrupcion",
        precision = 10,
        scale = 2
    )
    private BigDecimal volumenPotencialPorInterrupcion;

    @Column(
        name = "perdida_potencial_por_interrupcion",
        precision = 10,
        scale = 2
    )
    private BigDecimal perdidaPotencialPorInterrupcion;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "categoria",
        length = 50
    )
    private CategoriaIncidencia categoria;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "confiabilidad_exposicion", length = 30, nullable = false)
    private ConfiabilidadImpacto confiabilidadExposicion;

    @Lob
    @Column(name = "fuentes_calculo")
    private String fuentesCalculo;

    @Lob
    @Column(name = "explicacion_calculo")
    private String explicacionCalculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "idMoneda",
        nullable = false,
        referencedColumnName = "id"
    )
    private Moneda moneda;

    @Column(name = "monto_semanal", precision = 10, scale = 2)
    private BigDecimal montoSemanal;

    @Column(name = "monto_diario", precision = 10, scale = 2)
    private BigDecimal montoDiario;

    @Column(name = "comision", precision = 9, scale = 6)
    private BigDecimal comision;

    @Column(name = "dias_operativos")
    private Integer diasOperativos;

    @Column(name = "tiempo_interrupcion")
    private Integer tiempoInterrupcion;

    @PrePersist
    private void asignarFechaCalculo() {
        if (fechaCalculo == null) {
            fechaCalculo = LocalDateTime.now();
        }
    }

}