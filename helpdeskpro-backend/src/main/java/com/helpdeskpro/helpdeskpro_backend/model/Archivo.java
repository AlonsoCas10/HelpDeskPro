package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Archivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "idEvento",
        nullable = false,
        referencedColumnName = "idGmailMessage"
    )
    private Evento evento;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "tipo", length = 150)
    private String tipo;

    @Column(name = "extension", length = 20)
    private String extension;
}

