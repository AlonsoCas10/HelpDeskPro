package com.helpdeskpro.helpdeskpro_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moneda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moneda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;   
}
