package com.helpdeskpro.helpdeskpro_backend.model;

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
    @JoinColumn(
        name = "idEvento",
        nullable = false,
        referencedColumnName = "idGmailMessage"
    )
    private Evento evento;

    @Column(name = "dominio", length = 255)
    private String dominio;

    @Column(name = "url", length = 2048)
    private String url;
}
