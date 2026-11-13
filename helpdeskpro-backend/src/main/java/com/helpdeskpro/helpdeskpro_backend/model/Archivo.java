package com.helpdeskpro.helpdeskpro_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @JsonIgnore
    @JoinColumn(
        name = "idEvento",
        nullable = false,
        referencedColumnName = "id"
    )
    private Evento evento;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "tipo", length = 150)
    private String tipo;

    @Column(name = "extension", length = 20)
    private String extension;
}

