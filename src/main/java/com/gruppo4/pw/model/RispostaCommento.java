package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Risposta_commento (STAFF)")
public class RispostaCommento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_risposta_commento")
    private Long id;

    private String contenuto;

    @Column(name = "lunghezza_risposta")
    private Integer lunghezzaRisposta;

    @Column(name = "data_risposta")
    private LocalDateTime dataRisposta;

    @ManyToOne
    @JoinColumn(name = "id_utente")
    private Utente utente;
    
}