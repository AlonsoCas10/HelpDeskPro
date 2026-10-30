package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @Column(name = "idGmailMessage", nullable = false, length = 255)
    private String idGmailMessage;

    @Column(name = "idGmailThread", length = 255)
    private String idGmailThread;

    @Column(name = "nombreRemitente", length = 150)
    private String nombreRemitente;

    @Column(name = "correoRemitente", length = 320)
    private String correoRemitente;

    @Column(name = "dominioRemitente", length = 255)
    private String dominioRemitente;

    @Column(name = "asunto", length = 500)
    private String asunto;

    @Column(name = "fechaHora")
    private LocalDateTime fechaHora;

    @Lob
    @Column(name = "contenidoTexto")
    private String contenidoTexto;

    @Lob
    @Column(name = "contenidoHTML")
    private String contenidoHTML;

    @Enumerated(EnumType.STRING)
    @Column(name = "estadoConexion", length = 20)
    private EstadoConexion estadoConexion;

    @Builder.Default
    @OneToMany(
        mappedBy = "evento",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Enlace> enlaces = new ArrayList<>();

    @Builder.Default
    @OneToMany(
        mappedBy = "evento",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Archivo> archivos = new ArrayList<>();
}
