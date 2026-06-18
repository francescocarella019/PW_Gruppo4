package com.gruppo4.pw.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "lunghezza_contenuto")
    private Integer lunghezzaContenuto;

    @Column(name = "id_utente")
    private Long idUtente;

    @Column(name = "id_esperienza")
    private Long idEsperienza;
}
