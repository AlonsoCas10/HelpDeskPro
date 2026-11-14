package com.helpdeskpro.helpdeskpro_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Enlace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enlace {

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

    @Column(name = "dominio", length = 255)
    private String dominio;

    @Column(name = "url", length = 2048)
    private String url;
}
