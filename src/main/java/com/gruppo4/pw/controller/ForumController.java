package com.gruppo4.pw.controller;

import com.gruppo4.pw.model.*;
import com.gruppo4.pw.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/reviews")
    public String mostraForum(Model model, 
                              @RequestParam(required = false) Long categoriaId,
                              @RequestParam(required = false) Long esperienzaId,
                              @RequestParam(required = false) Long recensioneId) {
        
        // ================= CASO DETTAGLIO SINGOLA RECENSIONE / DISCUSSIONE =================
        if (recensioneId != null) {
            Recensione recensione = recensioneRepository.findById(recensioneId)
                    .orElseThrow(() -> new IllegalArgumentException("Recensione non trouvata: " + recensioneId));

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

        // ================= LOGICA DI FILTRAGGIO MAIN PAGE (REVIEWS) =================
        model.addAttribute("categorie", tipologiaRepository.findAll());
        
        List<Recensione> filtrate;
        
        if (esperienzaId != null) {
            filtrate = recensioneRepository.findByEsperienzaId(esperienzaId);
            model.addAttribute("filtroAttivo", "Esperienza ID: " + esperienzaId);
        } else if (categoriaId != null) {
            filtrate = recensioneRepository.findByCategoriaIdPersonalizzato(categoriaId);
            model.addAttribute("filtroAttivo", "Categoria ID: " + categoriaId);
            model.addAttribute("selectedCategoria", categoriaId);
        } else {
            filtrate = (List<Recensione>) recensioneRepository.findAll();
            model.addAttribute("filtroAttivo", "Tutte le recensioni");
        }
        
        // --- COSTRUZIONE DELLA MAPPA DELLE CATEGORIE PER LE CARD ---
        Map<Long, List<TipologiaEsperienza>> categoriePerEsperienza = new HashMap<>();
        for (Recensione rec : filtrate) {
            if (rec.getEsperienza() != null) {
                Long espId = rec.getEsperienza().getId();
                // Ottimizzazione: eseguiamo la query solo se l'esperienza non è già stata mappata
                if (!categoriePerEsperienza.containsKey(espId)) {
                    List<TipologiaEsperienza> teList = tipologiaEsperienzaRepository.findByEsperienzaId(espId);
                    categoriePerEsperienza.put(espId, teList);
                }
            }
        }
        
        model.addAttribute("recensioni", filtrate);
        model.addAttribute("categorieMappa", categoriePerEsperienza);
        
        return "reviews";
    }
}