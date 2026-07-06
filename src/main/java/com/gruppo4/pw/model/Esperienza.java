package com.gruppo4.pw.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "esperienze")
@Data
public class Esperienza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titolo;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    private LocalDate data; // 

    @Column(precision = 10, scale = 2)
    private BigDecimal prezzo;

    @Column(name = "is_disponibile")
    private Boolean isDisponibile;

    @ManyToOne
    @JoinColumn(name = "id_luogo")
    private Luogo luogo;
}