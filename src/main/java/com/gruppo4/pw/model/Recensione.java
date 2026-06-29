package com.gruppo4.pw.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data 
@Table(name = "recensioni")
public class Recensione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Valutazione")
    private Integer valutazione;

    @Column(name = "Data")
    private LocalDate data;

    @Column(name = "Contenuto", columnDefinition = "TEXT")
    private String contenuto;

    @Column(name = "lunghezza_contenuto")
    private Integer lunghezzaContenuto;

    @ManyToOne
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "id_esperienza")
    private Esperienza esperienza;
}
