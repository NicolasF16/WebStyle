package com.webstyle.controller;

import com.webstyle.model.User;
import com.webstyle.model.User.Status;
import com.webstyle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    // Lista todos os usuários cadastrados no sistema (para administrador)
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        List<User> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "user-list";
    }

    // Formulário para cadastrar novo usuário (apenas para admins logados)
    @GetMapping("/usuarios/novo")
    public String novoUsuarioForm(Model model, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", new User());
        return "user-form";
    }

    // Cadastra novo usuário (apenas para admins logados)
    @PostMapping("/cadastro")
    public String cadastrarUsuario(@ModelAttribute User usuario, 
                                  @RequestParam String senha2,
                                  Model model, 
                                  HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        try {
            // Valida se as senhas coincidem
            if (!usuario.getSenha().equals(senha2)) {
                model.addAttribute("erro", "As senhas não coincidem.");
                return "user-form";
            }
            
            userService.cadastrarUsuario(usuario);
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar usuário: " + e.getMessage());
            return "user-form";
        }
    }

    // Altera status do usuário (ativo/inativo)
    @PostMapping("/usuarios/status/{id}")
    public String alterarStatus(@PathVariable Long id, @RequestParam Status status, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        userService.alterarStatus(id, status);
        return "redirect:/usuarios";
    }

    // Formulário para alterar dados do usuário
    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable Long id, Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        User usuario = userService.buscarPorId(id);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        
        model.addAttribute("usuario", usuario);
        return "user-edit";
    }

    // Salva alterações do usuário
    @PostMapping("/usuarios/alterar/{id}")
    public String alterarUsuario(@PathVariable Long id, @ModelAttribute User usuario, 
                                Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        try {
            userService.alterarUsuario(id, usuario);
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao alterar usuário: " + e.getMessage());
            model.addAttribute("usuario", usuario);
            return "user-edit";
        }
    }
}