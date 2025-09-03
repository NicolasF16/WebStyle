package com.webstyle.controller;

import com.webstyle.model.User;
import com.webstyle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {
    
    @Autowired
    private UserService userService;

    // Formulário para cadastro de admin
    @GetMapping("/admin-form")
    public String adminForm(Model model) {
        model.addAttribute("usuario", new User());
        return "admin-form";
    }

    // Processa cadastro de admin
    @PostMapping("/cadastro-admin")
    public String cadastrarAdmin(@ModelAttribute User usuario, 
                                @RequestParam String senha2, 
                                Model model) {
        try {
            // Valida se as senhas coincidem
            if (!usuario.getSenha().equals(senha2)) {
                model.addAttribute("erro", "As senhas não coincidem.");
                return "admin-form";
            }
            
            // Cadastra o usuário com o tipo especificado no formulário
            userService.cadastrarUsuario(usuario);
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar administrador: " + e.getMessage());
            return "admin-form";
        }
    }
}