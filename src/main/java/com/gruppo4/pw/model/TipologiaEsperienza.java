package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipologie_esperienze")
@Data
public class TipologiaEsperienza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_esperienza", nullable = false)
    private Esperienza esperienza;

    @ManyToOne
    @JoinColumn(name = "id_tipologia", nullable = false)
    private Tipologia tipologia;
}