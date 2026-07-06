package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "acquisti")
@Data
public class Acquisto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "data_acquisto")
    private LocalDateTime dataAcquisto;

    @ManyToOne
    @JoinColumn(name = "id_esperienza", nullable = false)
    private Esperienza esperienza;

    // Relazione verso l'Utente che eredita tutte le sue info (Nome, Cognome, Email)
    @ManyToOne
    @JoinColumn(name = "id_utente", nullable = false)
    private Utente utente;
}