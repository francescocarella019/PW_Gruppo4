package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipologie")
@Data
public class Tipologia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descrizione;
}