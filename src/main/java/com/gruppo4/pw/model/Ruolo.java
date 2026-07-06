package com.gruppo4.pw.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ruoli")
public class Ruolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    private String nome;
    private String tipologia;
}