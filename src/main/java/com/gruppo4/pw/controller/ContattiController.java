package com.gruppo4.pw.controller;


import com.gruppo4.pw.model.Contatto;
import com.gruppo4.pw.repository.ContattoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContattiController {

    @Autowired
    private ContattoRepository contattoRepository;

    @PostMapping("/contattaci")
    public String gestisciFormDiContatto(
            @RequestParam("nome") String nome,
            @RequestParam("cognome") String cognome,
            @RequestParam("email") String email,
            @RequestParam("messaggio") String messaggio) {
        
        // Creiamo il nuovo oggetto Contatto con i dati del form
        Contatto nuovoMessaggio = new Contatto(nome, cognome, email, messaggio);
        
        // Salviamo nel database tramite la repository
        contattoRepository.save(nuovoMessaggio);

        // Reindirizziamo alla pagina contatti con un parametro di successo
        return "redirect:/contattaci?successo=true"; 
    }
}