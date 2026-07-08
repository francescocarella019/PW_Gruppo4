package com.gruppo4.pw.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gruppo4.pw.model.Acquisto;
import com.gruppo4.pw.model.Utente;
import com.gruppo4.pw.repository.AcquistoRepository;
import com.gruppo4.pw.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardProfiloController {

    private final UtenteRepository utenteRepository;
    private final AcquistoRepository acquistoRepository;

    public DashboardProfiloController(UtenteRepository utenteRepository, AcquistoRepository acquistoRepository) {
        this.utenteRepository = utenteRepository;
        this.acquistoRepository = acquistoRepository;
    }

    @GetMapping({"/dashboard", "/profilo", "/dashboard-profilo"})
    public String mostraDashboardProfilo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long utenteId = (Long) session.getAttribute("utenteId");

        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("errore", "Accedi per vedere il tuo profilo.");
            return "redirect:/login";
        }

        Utente utente = utenteRepository.findById(utenteId).orElse(null);
        if (utente == null) {
            session.removeAttribute("utenteId");
            redirectAttributes.addFlashAttribute("errore", "Sessione non valida. Effettua di nuovo l'accesso.");
            return "redirect:/login";
        }

        List<Acquisto> acquisti = acquistoRepository.findByUtenteIdOrderByDataAcquistoDesc(utenteId);

        model.addAttribute("utente", utente);
        model.addAttribute("inizialeUtente", inizialeUtente(utente));
        model.addAttribute("acquisti", acquisti);
        model.addAttribute("oggi", LocalDate.now());

        return "dashboardProfilo";
    }

    private String inizialeUtente(Utente utente) {
        if (utente.getNome() != null && !utente.getNome().isBlank()) {
            return utente.getNome().substring(0, 1).toUpperCase();
        }

        if (utente.getEmail() != null && !utente.getEmail().isBlank()) {
            return utente.getEmail().substring(0, 1).toUpperCase();
        }

        return "U";
    }
}
