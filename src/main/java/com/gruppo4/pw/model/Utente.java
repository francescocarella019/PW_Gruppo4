package com.gruppo4.pw.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cognome;
    
    @Column(unique = true)
    private String email;
    
    private String password;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Column(name = "n_telefono")
    private String nTelefono;

    @Column(name = "data_registrazione")
    private LocalDate dataRegistrazione;

    @ManyToOne
    @JoinColumn(name = "id_luogo")
    private Luogo luogo;

    @ManyToOne
    @JoinColumn(name = "id_ruolo")
    private Ruolo ruolo;
}