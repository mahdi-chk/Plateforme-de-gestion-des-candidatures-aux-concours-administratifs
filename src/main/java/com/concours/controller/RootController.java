package com.concours.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    /**
     * Redirige la racine vers la page d'accueil publique
     */
    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/public/";
    }
}