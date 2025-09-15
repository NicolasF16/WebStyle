package com.webstyle.controller;

import com.webstyle.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
    
    @GetMapping("/main")
    public String main(HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // Redireciona com base no tipo de usuário
        if (usuarioLogado.getTipo() == User.TipoUsuario.BACKOFFICE) {
            return "main-admin"; // Página principal para administradores
        } else if (usuarioLogado.getTipo() == User.TipoUsuario.EXTERNO) {
            return "main-estoquista"; // Página principal para estoquistas
        }
        
        return "redirect:/login";
    }
}