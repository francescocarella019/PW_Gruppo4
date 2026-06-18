package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Assegnazione")
public class Assegnazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_assegnato")
    private Long idAssegnato;

    @Column(name = "id_assegnatore")
    private Long idAssegnatore;

    @Column(name = "Data assegnazione")
    private LocalDateTime dataAssegnazione;

    @Column(name = "Stato")
    private String stato;

    @ManyToOne
    @JoinColumn(name = "id_commento")
    private Commento commento;

    @ManyToOne
    @JoinColumn(name = "id_assegnato", insertable = false, updatable = false)
    private RispostaCommento rispostaCommento;
}
