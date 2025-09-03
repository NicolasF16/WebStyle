package com.webstyle.controller;

import com.webstyle.service.UserService;
import com.webstyle.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    // Página inicial redireciona para login
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        Model model,
                        HttpSession session) {
        Optional<User> userOpt = userService.autenticar(email, senha);
        if (userOpt.isPresent()) {
            // Usuário autenticado, salva na sessão
            User user = userOpt.get();
            session.setAttribute("usuarioLogado", user);
            session.setAttribute("grupo", user.getTipo());
            return "redirect:/main";
        } else {
            // Falha no login
            model.addAttribute("erro", "Usuário ou senha inválidos, usuário inativo ou não autorizado.");
            return "login";
        }
    }
}