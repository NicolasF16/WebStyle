package com.webstyle.controller;

import com.webstyle.model.Product;
import com.webstyle.model.ProductImage;
import com.webstyle.model.User;
import com.webstyle.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        return "product-form-enhanced";
    }

    // Cadastra novo produto com imagens
    @PostMapping("/cadastro")
    public String cadastrarProduto(@Valid @ModelAttribute Product produto, 
                                  BindingResult result,
                                  @RequestParam("imagens") List<MultipartFile> imagens,
                                  @RequestParam(value = "imagemPrincipal", required = false) Long imagemPrincipalIndex,
                                  Model model, 
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("produto", produto);
            model.addAttribute("erro", "Dados inválidos. Verifique os campos obrigatórios.");
            return "product-form-enhanced";
        }
        
        try {
            productService.cadastrarProduto(produto, imagens, imagemPrincipalIndex);
            redirectAttributes.addFlashAttribute("sucesso", "Produto cadastrado com sucesso!");
            return "redirect:/produtos";
        } catch (IOException e) {
            model.addAttribute("erro", "Erro ao processar imagens: " + e.getMessage());
            model.addAttribute("produto", produto);
            return "product-form-enhanced";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar produto: " + e.getMessage());
            model.addAttribute("produto", produto);
            return "product-form-enhanced";
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
        
        List<ProductImage> imagens = productService.buscarImagensProduto(id);
        
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        return "product-view-enhanced";
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
        
        List<ProductImage> imagens = productService.buscarImagensProduto(id);
        
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        return "product-edit-enhanced";
    }

    // Salva alterações do produto
    @PostMapping("/alterar/{id}")
    public String alterarProduto(@PathVariable Long id, 
                                @Valid @ModelAttribute Product produto,
                                BindingResult result,
                                @RequestParam(value = "novasImagens", required = false) List<MultipartFile> novasImagens,
                                @RequestParam(value = "imagemPrincipal", required = false) Long imagemPrincipalIndex,
                                @RequestParam(value = "imagensParaRemover", required = false) String imagensParaRemover,
                                Model model, 
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            List<ProductImage> imagens = productService.buscarImagensProduto(id);
            model.addAttribute("produto", produto);
            model.addAttribute("imagens", imagens);
            model.addAttribute("erro", "Dados inválidos. Verifique os campos obrigatórios.");
            return "product-edit-enhanced";
        }
        
        try {
            // Processa IDs das imagens para remover
            List<Long> idsParaRemover = null;
            if (imagensParaRemover != null && !imagensParaRemover.trim().isEmpty()) {
                idsParaRemover = Arrays.stream(imagensParaRemover.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .toList();
            }
            
            productService.alterarProduto(id, produto, novasImagens, imagemPrincipalIndex, idsParaRemover);
            redirectAttributes.addFlashAttribute("sucesso", "Produto alterado com sucesso!");
            return "redirect:/produtos";
        } catch (IOException e) {
            List<ProductImage> imagens = productService.buscarImagensProduto(id);
            model.addAttribute("erro", "Erro ao processar imagens: " + e.getMessage());
            model.addAttribute("produto", produto);
            model.addAttribute("imagens", imagens);
            return "product-edit-enhanced";
        } catch (Exception e) {
            List<ProductImage> imagens = productService.buscarImagensProduto(id);
            model.addAttribute("erro", "Erro ao alterar produto: " + e.getMessage());
            model.addAttribute("produto", produto);
            model.addAttribute("imagens", imagens);
            return "product-edit-enhanced";
        }
    }

    // Altera status do produto (ativo/inativo)
    @PostMapping("/status/{id}")
    public String alterarStatus(@PathVariable Long id, @RequestParam Product.Status status, 
                               HttpSession session, RedirectAttributes redirectAttributes) {
        // Verifica se usuário está logado e é administrador
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return "redirect:/login";
        }
        
        try {
            productService.alterarStatus(id, status);
            redirectAttributes.addFlashAttribute("sucesso", "Status do produto alterado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao alterar status: " + e.getMessage());
        }
        
        return "redirect:/produtos";
    }

    // Endpoint AJAX para definir imagem principal
    @PostMapping("/imagem-principal/{produtoId}/{imagemId}")
    @ResponseBody
    public ResponseEntity<String> definirImagemPrincipal(@PathVariable Long produtoId, 
                                                        @PathVariable Long imagemId,
                                                        HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            return ResponseEntity.status(403).body("Não autorizado");
        }
        
        try {
            productService.definirImagemPrincipal(produtoId, imagemId);
            return ResponseEntity.ok("Imagem principal definida com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }
}