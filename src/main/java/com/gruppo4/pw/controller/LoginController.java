package com.gruppo4.pw.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gruppo4.pw.model.Utente;
import com.gruppo4.pw.repository.UtenteRepository;

@Controller
public class LoginController {

    private final UtenteRepository utenteRepository;
    private final BCryptPasswordEncoder passwordEncoder; // 1. Aggiunto l'encoder

    // Aggiornato il costruttore per iniettare sia la Repository che BCrypt
    public LoginController(UtenteRepository utenteRepository, BCryptPasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String mostraLogin() {
        return "authLogin";
    }

    @GetMapping("/register")
    public String mostraRegister(Model model) {
        model.addAttribute("utente", new Utente());
        return "registerUser";
    }

    //Gestione della Registrazione con Password Criptata
    @PostMapping("/register")
    public String register(@ModelAttribute("utente") Utente utente,
            @RequestParam String passwordConferma,
            Model model) {

        // Controllo lato server se le password in chiaro coincidono
        if (!utente.getPassword().equals(passwordConferma)) {
            model.addAttribute("errore", "Le password non coincidono.");
            return "registerUser";
        }

        // Controllo email già registrata
        if (utenteRepository.existsByEmail(utente.getEmail())) {
            model.addAttribute("errore", "Questa email è già registrata.");
            return "registerUser";
        }

        // --- CRIPTAZIONE DELLA PASSWORD ---
        String passwordInChiaro = utente.getPassword();
        String passwordCriptata = passwordEncoder.encode(passwordInChiaro);
        utente.setPassword(passwordCriptata); // Ora nel database finirà l'hash sicuro
        // ----------------------------------

        // Imposta i campi automatici
        utente.setDataRegistrazione(LocalDate.now());

        // TODO: impostare il ruolo di default
        // utente.setRuolo(...);
        utenteRepository.save(utente);

        return "redirect:/login";
    }

    //Gestione del Login con verifica della Password Criptata
    @PostMapping("/login")
    public String login(@RequestParam String email, 
                        @RequestParam String password, 
                        Model model) {
        
        // 1. Cerchiamo l'utente tramite email nel database
        // Nota: Assicurati di avere il metodo "findByEmail" definito nella tua UtenteRepository!
        Optional<Utente> utenteOpt = utenteRepository.findByEmail(email);

        if (utenteOpt.isPresent()) {
            Utente utente = utenteOpt.get();

            // 2. Usiamo passwordEncoder.matches per confrontare il testo inserito con l'hash del database
            if (passwordEncoder.matches(password, utente.getPassword())) {
                // Login riuscito! Sostituisci "/home" o "/" con la tua pagina principale post-login
                return "redirect:/home"; 
            }
        }

        // 3. Se l'email non esiste o la password è errata, ricarica il login con un errore
        model.addAttribute("errore", "Email o password non valide.");
        return "authLogin";
    }
}