package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    // =====================================================
    // IDENTIFICADOR INTERNO DE HELPDESKPRO
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código interno del incidente: INC-001, INC-002...
    @Column(name = "codigo", length = 50)
    private String codigo;


    // =====================================================
    // DATOS UTILIZADOS POR EL SISTEMA DE RIESGO
    // =====================================================

    @Column(name = "remitente", length = 320)
    private String remitente;

    @Column(name = "dominioRegistrado", length = 255)
    private String dominioRegistrado;

    @Column(name = "remitenteConocido")
    private Boolean remitenteConocido;

    @Column(name = "destinatario", length = 320)
    private String destinatario;

    @Column(name = "adjunto", length = 500)
    private String adjunto;

    @Column(name = "asunto", length = 500)
    private String asunto;

    @Column(name = "clasificacion", length = 100)
    private String clasificacion;

    @Column(name = "riskScore")
    private Integer riskScore;

    @Column(name = "estado", length = 100)
    private String estado;


    // =====================================================
    // DATOS PROVENIENTES DE GMAIL
    // =====================================================

    /*
     * El ID de Gmail NO será nuestra clave primaria.
     *
     * Nuestra PK sigue siendo "id" (Long).
     *
     * Este campo sirve para identificar el correo de Gmail
     * y evitar guardar el mismo mensaje dos veces.
     */
    @Column(
        name = "idGmailMessage",
        unique = true,
        length = 255
    )
    private String idGmailMessage;

    @Column(name = "idGmailThread", length = 255)
    private String idGmailThread;

    @Column(name = "nombreRemitente", length = 150)
    private String nombreRemitente;

    @Column(name = "correoRemitente", length = 320)
    private String correoRemitente;

    @Column(name = "dominioRemitente", length = 255)
    private String dominioRemitente;


    // =====================================================
    // FECHA DEL EVENTO
    // =====================================================

    @Column(name = "fechaHora")
    private LocalDateTime fechaHora;


    // =====================================================
    // CONTENIDO DEL CORREO
    // =====================================================

    @Lob
    @Column(name = "contenidoTexto")
    private String contenidoTexto;

    @Lob
    @Column(name = "contenidoHTML")
    private String contenidoHTML;


    // =====================================================
    // ESTADO DE LA CONEXIÓN CON GMAIL
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "estadoConexion", length = 20)
    private EstadoConexion estadoConexion;


    // =====================================================
    // ENLACES ENCONTRADOS EN EL CORREO
    // =====================================================

    @Builder.Default
    @OneToMany(
        mappedBy = "evento",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Enlace> enlaces = new ArrayList<>();


    // =====================================================
    // ARCHIVOS ADJUNTOS DEL CORREO
    // =====================================================

    @Builder.Default
    @OneToMany(
        mappedBy = "evento",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Archivo> archivos = new ArrayList<>();
}