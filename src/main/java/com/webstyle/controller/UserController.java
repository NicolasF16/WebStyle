package com.webstyle.controller;

import com.webstyle.model.User;
import com.webstyle.model.User.Status;
import com.webstyle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String listarUsuarios(Model model) {
        List<User> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "user-list";
    }

    @GetMapping("/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new User());
        return "user-form";
    }

    @PostMapping
    public String salvarUsuario(@ModelAttribute User usuario) {
        userService.cadastrarUsuario(usuario);
        return "redirect:/usuarios";
    }

    @PostMapping("/status/{id}")
    public String alterarStatus(@PathVariable Long id, @RequestParam Status status) {
        userService.alterarStatus(id, status);
        return "redirect:/usuarios";
    }
}
