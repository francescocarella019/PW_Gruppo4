package com.gruppo4.pw.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gruppo4.pw.model.Acquisto;
import com.gruppo4.pw.model.Recensione;
import com.gruppo4.pw.model.Utente;
import com.gruppo4.pw.repository.AcquistoRepository;
import com.gruppo4.pw.repository.RecensioneRepository;
import com.gruppo4.pw.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardProfiloController {

    private final UtenteRepository utenteRepository;
    private final AcquistoRepository acquistoRepository;
    private final RecensioneRepository recensioneRepository;

    public DashboardProfiloController(UtenteRepository utenteRepository,
            AcquistoRepository acquistoRepository,
            RecensioneRepository recensioneRepository) {
        this.utenteRepository = utenteRepository;
        this.acquistoRepository = acquistoRepository;
        this.recensioneRepository = recensioneRepository;
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

        if (isAdmin(utente)) {
            return "redirect:/staff";
        }

        List<Acquisto> acquisti = acquistoRepository.findByUtenteIdOrderByDataAcquistoDesc(utenteId);
        List<Recensione> recensioniUtente = recensioneRepository.findByUtenteIdOrderByDataDesc(utenteId);

        model.addAttribute("utente", utente);
        model.addAttribute("inizialeUtente", inizialeUtente(utente));
        model.addAttribute("acquisti", acquisti);
        model.addAttribute("recensioniUtente", recensioniUtente);
        model.addAttribute("oggi", LocalDate.now());

        return "dashboardProfilo";
    }

    @PostMapping("/dashboard/recensioni/{id}")
    public String aggiornaRecensione(@PathVariable Long id,
            @RequestParam Integer valutazione,
            @RequestParam String contenuto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("errore", "Accedi per modificare le tue recensioni.");
            return "redirect:/login";
        }

        Recensione recensione = recensioneRepository.findById(id).orElse(null);
        if (recensione == null || recensione.getUtente() == null || !utenteId.equals(recensione.getUtente().getId())) {
            redirectAttributes.addFlashAttribute("erroreDashboard", "Recensione non trovata o non modificabile.");
            return "redirect:/dashboard#reviews";
        }

        int voto = Math.max(1, Math.min(5, valutazione));
        String testo = contenuto == null ? "" : contenuto.trim();

        recensione.setValutazione(voto);
        recensione.setContenuto(testo);
        recensione.setLunghezzaContenuto(testo.length());
        recensioneRepository.save(recensione);

        redirectAttributes.addFlashAttribute("successoDashboard", "Recensione aggiornata correttamente.");
        return "redirect:/dashboard#reviews";
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
