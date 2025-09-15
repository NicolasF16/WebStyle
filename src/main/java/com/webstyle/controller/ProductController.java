package com.webstyle.controller;

import com.webstyle.model.Product;
import com.webstyle.model.User;
import com.webstyle.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/produtos")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    // Lista produtos com paginação e busca
    @GetMapping
    public String listarProdutos(@RequestParam(defaultValue = "0") int pagina,
                                @RequestParam(required = false) String busca,
                                Model model, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        Page<Product> produtos;
        if (busca != null && !busca.trim().isEmpty()) {
            produtos = productService.buscarProdutos(busca, pagina);
            model.addAttribute("busca", busca);
        } else {
            produtos = productService.listarProdutos(pagina);
        }
        
        model.addAttribute("produtos", produtos);
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", produtos.getTotalPages());
        model.addAttribute("totalElementos", produtos.getTotalElements());
        
        return "product-list";
    }

    // Formulário para cadastrar novo produto
    @GetMapping("/novo")
    public String novoProdutoForm(Model model, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        model.addAttribute("produto", new Product());
        return "product-form";
    }

    // Cadastra novo produto
    @PostMapping("/cadastro")
    public String cadastrarProduto(@ModelAttribute Product produto, Model model, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        try {
            productService.cadastrarProduto(produto);
            return "redirect:/produtos";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar produto: " + e.getMessage());
            model.addAttribute("produto", produto);
            return "product-form";
        }
    }

    // Visualizar detalhes do produto
    @GetMapping("/visualizar/{id}")
    public String visualizarProduto(@PathVariable Long id, Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        Product produto = productService.buscarPorId(id);
        if (produto == null) {
            return "redirect:/produtos";
        }
        
        model.addAttribute("produto", produto);
        return "product-view";
    }

    // Formulário para editar produto
    @GetMapping("/editar/{id}")
    public String editarProdutoForm(@PathVariable Long id, Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        Product produto = productService.buscarPorId(id);
        if (produto == null) {
            return "redirect:/produtos";
        }
        
        model.addAttribute("produto", produto);
        return "product-edit";
    }

    // Salva alterações do produto
    @PostMapping("/alterar/{id}")
    public String alterarProduto(@PathVariable Long id, @ModelAttribute Product produto, 
                                Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        try {
            productService.alterarProduto(id, produto);
            return "redirect:/produtos";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao alterar produto: " + e.getMessage());
            model.addAttribute("produto", produto);
            return "product-edit";
        }
    }

    // Altera status do produto (ativo/inativo)
    @PostMapping("/status/{id}")
    public String alterarStatus(@PathVariable Long id, @RequestParam Product.Status status, HttpSession session) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        try {
            productService.alterarStatus(id, status);
        } catch (Exception e) {
            // Log do erro, mas não quebra o fluxo
            System.err.println("Erro ao alterar status do produto: " + e.getMessage());
        }
        
        return "redirect:/produtos";
    }
}