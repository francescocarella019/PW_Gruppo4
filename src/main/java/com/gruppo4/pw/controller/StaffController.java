package com.gruppo4.pw.controller;

import com.gruppo4.pw.model.Contatto;
import com.gruppo4.pw.model.Utente;
import com.gruppo4.pw.repository.ContattoRepository;
import com.gruppo4.pw.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StaffController {

    @Autowired
    private ContattoRepository ContattoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @GetMapping("/staff")
    public String mostraDashboardStaff(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("errore", "Accedi come amministratore per entrare nello staff.");
            return "redirect:/login";
        }

        Utente utente = utenteRepository.findById(utenteId).orElse(null);
        if (utente == null || !isAdmin(utente)) {
            redirectAttributes.addFlashAttribute("errore", "Non hai i permessi per accedere alla dashboard staff.");
            return "redirect:/dashboard";
        }

        // Recupera tutti i messaggi salvati nella tabella messaggi_contatto
        List<Contatto> messaggi = ContattoRepository.findAll();
        
        // Passa la lista e il conteggio totale al template HTML
        model.addAttribute("messaggi", messaggi);
        model.addAttribute("totaleMessaggi", messaggi.size());
        
        return "dashboard-staff"; // Nome del file HTML (dashboard-staff.html)
    }

    private boolean isAdmin(Utente utente) {
        if (utente.getRuolo() == null) {
            return false;
        }

        return contieneRuoloAdmin(utente.getRuolo().getNome())
                || contieneRuoloAdmin(utente.getRuolo().getTipologia());
    }

    private boolean contieneRuoloAdmin(String valore) {
        if (valore == null) {
            return false;
        }

        String ruolo = valore.trim().toLowerCase();
        return ruolo.contains("admin")
                || ruolo.contains("staff")
                || ruolo.contains("amministratore");
    }
}
