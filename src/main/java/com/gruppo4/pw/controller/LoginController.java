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

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UtenteRepository utenteRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

    @PostMapping("/register")
    public String register(@ModelAttribute("utente") Utente utente,
            @RequestParam String passwordConferma,
            Model model) {

        if (!utente.getPassword().equals(passwordConferma)) {
            model.addAttribute("errore", "Le password non coincidono.");
            return "registerUser";
        }

        if (utenteRepository.existsByEmail(utente.getEmail())) {
            model.addAttribute("errore", "Questa email e' gia' registrata.");
            return "registerUser";
        }

        String passwordCriptata = passwordEncoder.encode(utente.getPassword());
        utente.setPassword(passwordCriptata);
        utente.setDataRegistrazione(LocalDate.now());

        // TODO: impostare il ruolo di default
        // utente.setRuolo(...);
        utenteRepository.save(utente);

        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        Optional<Utente> utenteOpt = utenteRepository.findByEmail(email);

        if (utenteOpt.isEmpty()) {
            model.addAttribute("errore", "Account inesistente. Controlla l'email o registrati.");
            return "authLogin";
        }

        Utente utente = utenteOpt.get();

        if (passwordEncoder.matches(password, utente.getPassword())) {
            session.setAttribute("utenteId", utente.getId());
            return "redirect:/dashboard";
        }

        model.addAttribute("errore", "Email registrata: la password inserita non e' corretta.");
        return "authLogin";
    }
}
