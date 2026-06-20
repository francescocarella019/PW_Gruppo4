package com.gruppo4.pw.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "risposte")
@Data
public class Risposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenuto;
    private int lunghezza_risposta;
    private LocalDateTime data_risposta;

    @ManyToOne
    @JoinColumn(name = "id_utente", nullable = false)
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "id_domanda", nullable = false)
    private Domanda domanda;
}
