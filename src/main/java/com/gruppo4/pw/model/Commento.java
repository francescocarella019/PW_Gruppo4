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

@Data
@Entity

@Table(name = "commenti")
public class Commento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String commento;

    @Column(name = "lunghezza_commento")
    private Integer lunghezzaCommento;

    @Column(name = "id_utente")
    private Long idUtente;

    @Column(name = "data_commento")
    private LocalDate dataCommento;

    @ManyToOne
    @JoinColumn(name = "id_recensione")
    private Recensione recensione;

}
