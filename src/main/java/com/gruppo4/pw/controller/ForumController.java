package com.gruppo4.pw.controller;

import com.gruppo4.pw.model.*;
import com.gruppo4.pw.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ForumController {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private TipologiaRepository tipologiaRepository;

    @Autowired
    private CommentoRepository commentoRepository;

    @Autowired
    private DomandaRepository domandaRepository;

    @Autowired
    private RispostaRepository rispostaRepository;

    @Autowired
    private TipologiaEsperienzaRepository tipologiaEsperienzaRepository;

    @Autowired
    private EsperienzaRepository manteneraEsperienzaRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @GetMapping("/chi-siamo")
public String chiSiamo() {
    return "chi-siamo"; // Deve corrispondere ESATTAMENTE al nome del file .html senza estensione
}
@GetMapping("/contattaci")
public String contattaci(){
    return "contattaci"; // Deve corrispondere ESATTAMENTE al nome del file .html senza estensione
}
    @GetMapping({"/", "/reviews"})
    public String mostraForum(Model model, 
                              @RequestParam(required = false) Long categoriaId,
                              @RequestParam(required = false) Long esperienzaId,
                              @RequestParam(required = false) Long recensioneId,
                              HttpSession session) {
        aggiungiDatiSessione(model, session);
        
        // ================= CASO 1: DETTAGLIO DI TUTTE LE RECENSIONI DI UN'ESPERIENZA =================
        if (esperienzaId != null) {
            Esperienza esperienza = manteneraEsperienzaRepository.findById(esperienzaId)
                    .orElseThrow(() -> new IllegalArgumentException("Esperienza non trovata: " + esperienzaId));

            // Recuperiamo la lista di TUTTE le recensioni di questa esperienza (niente più card singola!)
            List<Recensione> recensioni = recensioneRepository.findByEsperienzaId(esperienzaId);

            // Mappe per agganciare commenti, domande e risposte a ogni rispettiva recensione
            Map<Long, List<Commento>> commentiMappa = new HashMap<>();
            Map<Long, List<Domanda>> domandeMappa = new HashMap<>();
            Map<Long, List<Risposta>> risposteMappa = new HashMap<>();

            for (Recensione rec : recensioni) {
                List<Commento> commenti = commentoRepository.findByRecensioneId(rec.getId());
                commentiMappa.put(rec.getId(), commenti);

                List<Domanda> domande = domandaRepository.findByRecensioneId(rec.getId());
                domandeMappa.put(rec.getId(), domande);

                for (Domanda domanda : domande) {
                    List<Risposta> risposte = rispostaRepository.findByDomandaId(domanda.getId());
                    risposteMappa.put(domanda.getId(), risposte);
                }
            }

            model.addAttribute("esperienza", esperienza);
            model.addAttribute("recensioni", recensioni); // Passiamo l'intera lista al template
            model.addAttribute("commentiMappa", commentiMappa);
            model.addAttribute("domandeMappa", domandeMappa);
            model.addAttribute("risposteMappa", risposteMappa);

            return "esperienza-singola";
        }

        // ================= CASO 2: DETTAGLIO DI UNA SINGOLA RECENSIONE SPECIFICA =================
        if (recensioneId != null) {
            Recensione recensione = recensioneRepository.findById(recensioneId)
                    .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata: " + recensioneId));

            List<Commento> commenti = commentoRepository.findByRecensioneId(recensioneId);
            List<Domanda> domande = domandaRepository.findByRecensioneId(recensioneId);

            Map<Long, List<Risposta>> rispostePerDomanda = new HashMap<>();
            for (Domanda domanda : domande) {
                List<Risposta> risposte = rispostaRepository.findByDomandaId(domanda.getId());
                rispostePerDomanda.put(domanda.getId(), risposte);
            }

            model.addAttribute("recensione", recensione);
            model.addAttribute("commenti", commenti);
            model.addAttribute("domande", domande);
            model.addAttribute("risposteMappa", rispostePerDomanda);

            return "esperienza-singola";
        }

        // ================= CASO 3: LOGICA DI FILTRAGGIO MAIN PAGE (ESPERIENZE UNICHE) =================
        model.addAttribute("categorie", tipologiaRepository.findAll());
        
        List<Esperienza> esperienzeVisualizzate = new ArrayList<>();
        
        if (categoriaId != null) {
            // Filtro per Categoria passando dalla tabella ponte
            List<TipologiaEsperienza> legami = tipologiaEsperienzaRepository.findByTipologiaId(categoriaId);
            for (TipologiaEsperienza legame : legami) {
                if (legame.getEsperienza() != null) {
                    esperienzeVisualizzate.add(legame.getEsperienza());
                }
            }
            model.addAttribute("filtroAttivo", "Categoria ID: " + categoriaId);
            model.addAttribute("selectedCategoria", categoriaId);
        } else {
            // Mostra tutte le esperienze uniche disponibili a sistema
            Iterable<Esperienza> tutteLeEsp = manteneraEsperienzaRepository.findAll();
            tutteLeEsp.forEach(esperienzeVisualizzate::add);
            model.addAttribute("filtroAttivo", "Tutte le esperienze");
        }
        
        // --- COSTRUZIONE DELLE MAPPE PER LE CARD AGGREGATE ---
        Map<Long, List<TipologiaEsperienza>> categoriePerEsperienza = new HashMap<>();
        Map<Long, Double> mediaValutazioni = new HashMap<>();
        Map<Long, Integer> totaleRecensioni = new HashMap<>();
        Map<Long, Recensione> ultimaRecensionePerEsp = new HashMap<>();
        
        if (!esperienzeVisualizzate.isEmpty()) {
            for (Esperienza esp : esperienzeVisualizzate) {
                Long espId = esp.getId();
                
                // Categorie della card (CORRETTO il nome della mappa qui sotto)
                List<TipologiaEsperienza> teList = tipologiaEsperienzaRepository.findByEsperienzaId(espId);
                categoriePerEsperienza.put(espId, teList);
                
                // Storico recensioni dell'esperienza per calcolare i dati aggregati
                List<Recensione> recensioniEsp = recensioneRepository.findByEsperienzaId(espId);
                totaleRecensioni.put(espId, recensioniEsp.size());
                
                if (!recensioniEsp.isEmpty()) {
                    double somma = 0;
                    for (Recensione rec : recensioniEsp) {
                        somma += rec.getValutazione();
                    }
                    double media = somma / recensioniEsp.size();
                    mediaValutazioni.put(espId, Math.round(media * 10.0) / 10.0);
                    
                    // L'ultima recensione inserita diventa l'anteprima visibile sulla card
                    ultimaRecensionePerEsp.put(espId, recensioniEsp.get(recensioniEsp.size() - 1));
                } else {
                    mediaValutazioni.put(espId, 0.0);
                }
            }
        }
        
        model.addAttribute("esperienze", esperienzeVisualizzate);
        model.addAttribute("tutteEsperienze", iterableToList(manteneraEsperienzaRepository.findAll()));
        model.addAttribute("categorieMappa", categoriePerEsperienza);
        model.addAttribute("mediaValutazioni", mediaValutazioni);
        model.addAttribute("totaleRecensioni", totaleRecensioni);
        model.addAttribute("ultimeRecensioni", ultimaRecensionePerEsp);
        
        return "reviews"; // Deve corrispondere ESATTAMENTE al nome del file .html senza estensione
    }

    @PostMapping("/reviews/nuova")
    public String creaRecensione(@RequestParam Long esperienzaId,
                                 @RequestParam Integer valutazione,
                                 @RequestParam String contenuto,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Accedi per pubblicare una recensione.");
            return "redirect:/login";
        }

        Utente utente = utenteRepository.findById(utenteId).orElse(null);
        Esperienza esperienza = manteneraEsperienzaRepository.findById(esperienzaId).orElse(null);

        if (utente == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("errore", "Sessione non valida. Effettua di nuovo l'accesso.");
            return "redirect:/login";
        }

        if (esperienza == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Esperienza non trovata.");
            return "redirect:/reviews";
        }

        String testo = contenuto == null ? "" : contenuto.trim();
        if (testo.isBlank()) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Scrivi un testo prima di pubblicare la recensione.");
            return "redirect:/reviews";
        }

        Recensione recensione = new Recensione();
        recensione.setUtente(utente);
        recensione.setEsperienza(esperienza);
        recensione.setData(LocalDate.now());
        recensione.setValutazione(Math.max(1, Math.min(5, valutazione)));
        recensione.setContenuto(testo);
        recensione.setLunghezzaContenuto(testo.length());

        recensioneRepository.save(recensione);
        redirectAttributes.addFlashAttribute("successoReviews", "Recensione pubblicata correttamente.");

        return "redirect:/reviews";
    }

    private void aggiungiDatiSessione(Model model, HttpSession session) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        model.addAttribute("utenteLoggato", utenteId != null);
        model.addAttribute("accountUrl", utenteId == null ? "/login" : Boolean.TRUE.equals(isAdmin) ? "/staff" : "/dashboard");
    }

    private List<Esperienza> iterableToList(Iterable<Esperienza> esperienze) {
        List<Esperienza> risultato = new ArrayList<>();
        esperienze.forEach(risultato::add);
        return risultato;
    }
}
