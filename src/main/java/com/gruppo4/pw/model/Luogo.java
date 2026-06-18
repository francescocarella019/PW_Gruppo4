package com.gruppo4.pw.model;

import jakarta.persistence.*;

@Entity
@Table(name = "luoghi")
public class Luogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String paese;
    private String citta;
    private String cap;
    private String provincia;
}