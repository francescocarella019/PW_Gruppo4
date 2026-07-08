package com.gruppo4.pw.controller;

import com.gruppo4.pw.model.*;
import com.gruppo4.pw.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @Autowired
    private AcquistoRepository acquistoRepository;

    @Autowired
    private ImmagineRepository immagineRepository;

    @Autowired
    private RispostaCommentoRepository rispostaCommentoRepository;

    @GetMapping("/chi-siamo")
public String chiSiamo(Model model, HttpSession session) {
    aggiungiDatiSessione(model, session);
    return "chi-siamo"; // Deve corrispondere ESATTAMENTE al nome del file .html senza estensione
}
@GetMapping("/contattaci")
public String contattaci(Model model, HttpSession session){
    aggiungiDatiSessione(model, session);
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
            Map<Long, List<Immagine>> immaginiMappa = new HashMap<>();
            Map<Long, List<RispostaCommento>> risposteCommentiMappa = new HashMap<>();

            for (Recensione rec : recensioni) {
                List<Commento> commenti = commentoRepository.findByRecensioneId(rec.getId());
                commentiMappa.put(rec.getId(), commenti);
                for (Commento commento : commenti) {
                    risposteCommentiMappa.put(commento.getId(), rispostaCommentoRepository.findByCommentoId(commento.getId()));
                }

                List<Domanda> domande = domandaRepository.findByRecensioneId(rec.getId());
                domandeMappa.put(rec.getId(), domande);
                immaginiMappa.put(rec.getId(), immagineRepository.findByRecensioneId(rec.getId()));

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
            model.addAttribute("risposteCommentiMappa", risposteCommentiMappa);
            model.addAttribute("immaginiMappa", immaginiMappa);
            model.addAttribute("domandeEsperienza", domandaRepository.findByEsperienzaId(esperienzaId));
            model.addAttribute("puoRecensireEsperienza", puoRecensire(session, esperienza));
            model.addAttribute("accountUrl", accountUrl(session));
            model.addAttribute("utenteLoggato", session.getAttribute("utenteId") != null);

            return "esperienza-singola";
        }

        // ================= CASO 2: DETTAGLIO DI UNA SINGOLA RECENSIONE SPECIFICA =================
        if (recensioneId != null) {
            Recensione recensione = recensioneRepository.findById(recensioneId)
                    .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata: " + recensioneId));

            List<Commento> commenti = commentoRepository.findByRecensioneId(recensioneId);
            List<Domanda> domande = domandaRepository.findByRecensioneId(recensioneId);

            Map<Long, List<RispostaCommento>> risposteCommentiMappa = new HashMap<>();
            for (Commento commento : commenti) {
                risposteCommentiMappa.put(commento.getId(), rispostaCommentoRepository.findByCommentoId(commento.getId()));
            }

            Map<Long, List<Risposta>> rispostePerDomanda = new HashMap<>();
            for (Domanda domanda : domande) {
                List<Risposta> risposte = rispostaRepository.findByDomandaId(domanda.getId());
                rispostePerDomanda.put(domanda.getId(), risposte);
            }

            model.addAttribute("recensione", recensione);
            model.addAttribute("commenti", commenti);
            model.addAttribute("domande", domande);
            model.addAttribute("risposteMappa", rispostePerDomanda);
            if (recensione.getEsperienza() != null) {
                model.addAttribute("esperienza", recensione.getEsperienza());
                model.addAttribute("recensioni", List.of(recensione));
                model.addAttribute("commentiMappa", Map.of(recensione.getId(), commenti));
                model.addAttribute("risposteCommentiMappa", risposteCommentiMappa);
                model.addAttribute("domandeMappa", Map.of(recensione.getId(), domande));
                model.addAttribute("immaginiMappa", Map.of(recensione.getId(), immagineRepository.findByRecensioneId(recensione.getId())));
                model.addAttribute("domandeEsperienza", domandaRepository.findByEsperienzaId(recensione.getEsperienza().getId()));
                model.addAttribute("puoRecensireEsperienza", puoRecensire(session, recensione.getEsperienza()));
                model.addAttribute("accountUrl", accountUrl(session));
                model.addAttribute("utenteLoggato", session.getAttribute("utenteId") != null);
            }

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
        model.addAttribute("esperienzeRecensibili", esperienzeRecensibili(session));
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
                                 @RequestParam(required = false) List<MultipartFile> foto,
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

        if (!puoRecensire(session, esperienza)) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Puoi recensire solo esperienze acquistate e gia' svolte.");
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
        salvaImmaginiRecensione(foto, recensione);
        redirectAttributes.addFlashAttribute("successoReviews", "Recensione pubblicata correttamente.");

        return "redirect:/reviews?esperienzaId=" + esperienzaId;
    }

    @PostMapping("/recensioni/{recensioneId}/commenti")
    public String creaCommentoRecensione(@PathVariable Long recensioneId,
                                         @RequestParam String commento,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Accedi per commentare una recensione.");
            return "redirect:/login";
        }

        Recensione recensione = recensioneRepository.findById(recensioneId).orElse(null);
        if (recensione == null || recensione.getEsperienza() == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Recensione non trovata.");
            return "redirect:/reviews";
        }

        String testo = commento == null ? "" : commento.trim();
        if (testo.isBlank()) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Scrivi un commento prima di pubblicarlo.");
            return "redirect:/reviews?esperienzaId=" + recensione.getEsperienza().getId();
        }

        Commento nuovoCommento = new Commento();
        nuovoCommento.setCommento(testo);
        nuovoCommento.setLunghezzaCommento(testo.length());
        nuovoCommento.setIdUtente(utenteId);
        nuovoCommento.setDataCommento(LocalDate.now());
        nuovoCommento.setRecensione(recensione);
        commentoRepository.save(nuovoCommento);

        redirectAttributes.addFlashAttribute("successoReviews", "Commento pubblicato correttamente.");
        return "redirect:/reviews?esperienzaId=" + recensione.getEsperienza().getId();
    }

    @PostMapping("/commenti/{commentoId}/risposte")
    public String creaRispostaCommento(@PathVariable Long commentoId,
                                       @RequestParam String contenuto,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Accedi per rispondere a un commento.");
            return "redirect:/login";
        }

        Commento commento = commentoRepository.findById(commentoId).orElse(null);
        if (commento == null || commento.getRecensione() == null || commento.getRecensione().getEsperienza() == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Commento non trovato.");
            return "redirect:/reviews";
        }

        Utente utente = utenteRepository.findById(utenteId).orElse(null);
        if (utente == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("errore", "Sessione non valida. Effettua di nuovo l'accesso.");
            return "redirect:/login";
        }

        String testo = contenuto == null ? "" : contenuto.trim();
        if (testo.isBlank()) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Scrivi una risposta prima di pubblicarla.");
            return "redirect:/reviews?esperienzaId=" + commento.getRecensione().getEsperienza().getId();
        }

        RispostaCommento risposta = new RispostaCommento();
        risposta.setContenuto(testo);
        risposta.setLunghezzaRisposta(testo.length());
        risposta.setDataRisposta(LocalDateTime.now());
        risposta.setUtente(utente);
        risposta.setCommento(commento);
        rispostaCommentoRepository.save(risposta);

        redirectAttributes.addFlashAttribute("successoReviews", "Risposta pubblicata correttamente.");
        return "redirect:/reviews?esperienzaId=" + commento.getRecensione().getEsperienza().getId();
    }

    @PostMapping("/esperienze/domanda")
    public String creaDomandaEsperienza(@RequestParam Long esperienzaId,
                                        @RequestParam String contenuto,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            redirectAttributes.addFlashAttribute("erroreReviews", "Accedi per fare una domanda.");
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
            redirectAttributes.addFlashAttribute("erroreReviews", "Scrivi una domanda prima di pubblicarla.");
            return "redirect:/reviews?esperienzaId=" + esperienzaId;
        }

        Domanda domanda = new Domanda();
        domanda.setUtente(utente);
        domanda.setEsperienza(esperienza);
        domanda.setContenuto(testo);
        domanda.setLunghezzaContenuto(testo.length());
        domanda.setDataDomanda(LocalDateTime.now());

        domandaRepository.save(domanda);
        redirectAttributes.addFlashAttribute("successoReviews", "Domanda pubblicata nel FAQ dell'esperienza.");

        return "redirect:/reviews?esperienzaId=" + esperienzaId;
    }

    private void aggiungiDatiSessione(Model model, HttpSession session) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        model.addAttribute("utenteLoggato", utenteId != null);
        model.addAttribute("accountUrl", accountUrl(session));
    }

    private List<Esperienza> iterableToList(Iterable<Esperienza> esperienze) {
        List<Esperienza> risultato = new ArrayList<>();
        esperienze.forEach(risultato::add);
        return risultato;
    }

    private List<Esperienza> esperienzeRecensibili(HttpSession session) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        if (utenteId == null) {
            return List.of();
        }

        List<Esperienza> risultato = new ArrayList<>();
        for (Acquisto acquisto : acquistoRepository.findByUtenteIdOrderByDataAcquistoDesc(utenteId)) {
            Esperienza esperienza = acquisto.getEsperienza();
            if (esperienza != null && esperienzaGiaSvolta(esperienza)) {
                risultato.add(esperienza);
            }
        }

        return risultato;
    }

    private boolean puoRecensire(HttpSession session, Esperienza esperienza) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        return utenteId != null
                && esperienza != null
                && acquistoRepository.existsByUtenteIdAndEsperienzaId(utenteId, esperienza.getId())
                && esperienzaGiaSvolta(esperienza);
    }

    private boolean esperienzaGiaSvolta(Esperienza esperienza) {
        return esperienza.getData() != null && !esperienza.getData().isAfter(LocalDate.now());
    }

    private String accountUrl(HttpSession session) {
        Long utenteId = (Long) session.getAttribute("utenteId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return utenteId == null ? "/login" : Boolean.TRUE.equals(isAdmin) ? "/staff" : "/dashboard";
    }

    private void salvaImmaginiRecensione(List<MultipartFile> foto, Recensione recensione) {
        if (foto == null || foto.isEmpty()) {
            return;
        }

        for (MultipartFile file : foto) {
            salvaImmagineRecensione(file, recensione);
        }
    }

    private void salvaImmagineRecensione(MultipartFile foto, Recensione recensione) {
        if (foto == null || foto.isEmpty()) {
            return;
        }

        String contentType = foto.getContentType();
        if (contentType == null || !Set.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            return;
        }

        String estensione = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        String nomeFile = UUID.randomUUID() + estensione;
        Path cartellaUpload = Path.of("uploads", "reviews");
        Path fileDestinazione = cartellaUpload.resolve(nomeFile);

        try {
            Files.createDirectories(cartellaUpload);
            Files.copy(foto.getInputStream(), fileDestinazione, StandardCopyOption.REPLACE_EXISTING);

            Immagine immagine = new Immagine();
            immagine.setRecensione(recensione);
            immagine.setUrl("/uploads/reviews/" + nomeFile);
            immagine.setDescrizione("Foto caricata per la recensione");
            immagineRepository.save(immagine);
        } catch (IOException ignored) {
            // Se il file non viene salvato, la recensione resta comunque pubblicata.
        }
    }
}
