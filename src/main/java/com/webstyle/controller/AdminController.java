package com.webstyle.controller;

import com.webstyle.model.User;
import com.webstyle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AdminController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/admin-form")
    public String adminForm(Model model) {
        model.addAttribute("usuario", new User());
        return "admin-form";
    }

    @PostMapping("/cadastro-admin")
    public String cadastrarAdmin(@ModelAttribute User usuario, Model model) {
        try {
            // Define como usuário BACKOFFICE por padrão
            usuario.setTipo(User.TipoUsuario.BACKOFFICE);
            userService.cadastrarUsuario(usuario);
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar administrador: " + e.getMessage());
            return "admin-form";
        }
    }
}