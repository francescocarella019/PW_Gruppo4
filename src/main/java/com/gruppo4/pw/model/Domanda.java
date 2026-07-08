package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "domande")
@Data
public class Domanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenuto;

    @Column(name = "lunghezza_contenuto")
    private Integer lunghezzaContenuto;

    @Column(name = "data_domanda")
    private LocalDateTime dataDomanda;

    @ManyToOne
    @JoinColumn(name = "id_utente", nullable = false)
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "id_recensione")
    private Recensione recensione;

    @ManyToOne
    @JoinColumn(name = "id_esperienza")
    private Esperienza esperienza;
}
