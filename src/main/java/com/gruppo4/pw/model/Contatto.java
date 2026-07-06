package com.gruppo4.pw.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messaggi_contatto")
public class Contatto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 50)
    private String cognome;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messaggio;

    @Column(name = "data_invio", nullable = false)
    private LocalDateTime dataInvio;

    // Costruttore vuoto richiesto da JPA
    public Contatto() {}

    // Costruttore comodo per il Controller
    public Contatto(String nome, String cognome, String email, String messaggio) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.messaggio = messaggio;
        this.dataInvio = LocalDateTime.now(); // Imposta automaticamente la data corrente
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }
    public LocalDateTime getDataInvio() { return dataInvio; }
    public void setDataInvio(LocalDateTime dataInvio) { this.dataInvio = dataInvio; }
}
