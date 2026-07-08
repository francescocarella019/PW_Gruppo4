package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ruoli")
@Data
public class Ruolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    private String nome;
    private String tipologia;
}
