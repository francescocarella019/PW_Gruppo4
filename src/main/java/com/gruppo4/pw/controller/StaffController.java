package com.gruppo4.pw.controller;

import com.gruppo4.pw.model.Contatto;
import com.gruppo4.pw.repository.ContattoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class StaffController {

    @Autowired
    private ContattoRepository ContattoRepository;

    @GetMapping("/staff")
    public String mostraDashboardStaff(Model model) {
        // Recupera tutti i messaggi salvati nella tabella messaggi_contatto
        List<Contatto> messaggi = ContattoRepository.findAll();
        
        // Passa la lista e il conteggio totale al template HTML
        model.addAttribute("messaggi", messaggi);
        model.addAttribute("totaleMessaggi", messaggi.size());
        
        return "dashboard-staff"; // Nome del file HTML (dashboard-staff.html)
    }
}
