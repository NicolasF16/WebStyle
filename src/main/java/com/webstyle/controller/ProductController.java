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
import java.math.BigDecimal;
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
        // Verifica se usuário está logado (BACKOFFICE e EXTERNO podem acessar)
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
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
        model.addAttribute("isAdmin", usuarioLogado.getTipo() == User.TipoUsuario.BACKOFFICE);
        
        return "product-list";
    }

    // Formulário para cadastrar novo produto - CORRIGIDO: Ambos tipos podem cadastrar
    @GetMapping("/novo")
    public String novoProdutoForm(Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // CORREÇÃO: Permite tanto BACKOFFICE quanto EXTERNO cadastrar produtos
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
        }
        
        model.addAttribute("produto", new Product());
        return "product-form-enhanced";
    }

    // Cadastra novo produto com imagens - CORRIGIDO
    @PostMapping("/cadastro")
    public String cadastrarProduto(@Valid @ModelAttribute Product produto, 
                                  BindingResult result,
                                  @RequestParam(value = "imagens", required = false) List<MultipartFile> imagens,
                                  @RequestParam(value = "imagemPrincipal", required = false) String imagemPrincipalStr,
                                  Model model, 
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // CORREÇÃO: Permite tanto BACKOFFICE quanto EXTERNO cadastrar
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
        }
        
        // CORREÇÃO: Validações manuais adicionais
        try {
            validarCamposProduto(produto, result);
            
            if (result.hasErrors()) {
                StringBuilder erros = new StringBuilder("Campos inválidos: ");
                result.getFieldErrors().forEach(error -> 
                    erros.append(error.getField()).append(" (").append(error.getDefaultMessage()).append("), ")
                );
                model.addAttribute("produto", produto);
                model.addAttribute("erro", erros.toString());
                return "product-form-enhanced";
            }
            
            // CORREÇÃO: Conversão correta do índice da imagem principal
            Long imagemPrincipalIndex = null;
            if (imagemPrincipalStr != null && !imagemPrincipalStr.trim().isEmpty()) {
                try {
                    imagemPrincipalIndex = Long.parseLong(imagemPrincipalStr);
                } catch (NumberFormatException e) {
                    // Ignora se não for um número válido
                }
            }
            
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

    // NOVO: Método de validação manual
    private void validarCamposProduto(Product produto, BindingResult result) {
        // Validação do código
        if (produto.getCodigo() == null || produto.getCodigo().trim().isEmpty()) {
            result.rejectValue("codigo", "error.codigo", "Código é obrigatório");
        } else if (produto.getCodigo().length() > 50) {
            result.rejectValue("codigo", "error.codigo", "Código deve ter no máximo 50 caracteres");
        }
        
        // Validação do nome
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            result.rejectValue("nome", "error.nome", "Nome é obrigatório");
        } else if (produto.getNome().length() > 200) {
            result.rejectValue("nome", "error.nome", "Nome deve ter no máximo 200 caracteres");
        }
        
        // Validação da descrição
        if (produto.getDescricao() != null && produto.getDescricao().length() > 2000) {
            result.rejectValue("descricao", "error.descricao", "Descrição deve ter no máximo 2000 caracteres");
        }
        
        // Validação do preço
        if (produto.getPreco() == null) {
            result.rejectValue("preco", "error.preco", "Preço é obrigatório");
        } else if (produto.getPreco().compareTo(new BigDecimal("0.01")) < 0) {
            result.rejectValue("preco", "error.preco", "Preço deve ser maior que zero");
        }
        
        // Validação do estoque
        if (produto.getQuantidadeEstoque() == null) {
            result.rejectValue("quantidadeEstoque", "error.quantidadeEstoque", "Quantidade em estoque é obrigatória");
        } else if (produto.getQuantidadeEstoque() < 0) {
            result.rejectValue("quantidadeEstoque", "error.quantidadeEstoque", "Quantidade não pode ser negativa");
        }
        
        // Validação da avaliação
        if (produto.getAvaliacao() == null) {
            produto.setAvaliacao(new BigDecimal("1.0")); // Valor padrão
        } else {
            BigDecimal avaliacao = produto.getAvaliacao();
            if (avaliacao.compareTo(new BigDecimal("1.0")) < 0 || 
                avaliacao.compareTo(new BigDecimal("5.0")) > 0) {
                result.rejectValue("avaliacao", "error.avaliacao", "Avaliação deve estar entre 1.0 e 5.0");
            }
        }
        
        // Validação do status
        if (produto.getStatus() == null) {
            produto.setStatus(Product.Status.ATIVO); // Valor padrão
        }
    }

    // Visualizar detalhes do produto
    @GetMapping("/visualizar/{id}")
    public String visualizarProduto(@PathVariable Long id, Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        Product produto = productService.buscarPorId(id);
        if (produto == null) {
            return "redirect:/produtos";
        }
        
        List<ProductImage> imagens = productService.buscarImagensProduto(id);
        
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        model.addAttribute("isAdmin", usuarioLogado.getTipo() == User.TipoUsuario.BACKOFFICE);
        return "product-view-enhanced";
    }

    // Formulário para editar produto - CORRIGIDO: Ambos tipos podem editar
    @GetMapping("/editar/{id}")
    public String editarProdutoForm(@PathVariable Long id, Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // CORREÇÃO: Permite tanto BACKOFFICE quanto EXTERNO editar
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
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

    // Salva alterações do produto - CORRIGIDO
    @PostMapping("/alterar/{id}")
    public String alterarProduto(@PathVariable Long id, 
                                @Valid @ModelAttribute Product produto,
                                BindingResult result,
                                @RequestParam(value = "novasImagens", required = false) List<MultipartFile> novasImagens,
                                @RequestParam(value = "imagemPrincipal", required = false) String imagemPrincipalStr,
                                @RequestParam(value = "imagensParaRemover", required = false) String imagensParaRemover,
                                Model model, 
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // CORREÇÃO: Permite tanto BACKOFFICE quanto EXTERNO editar
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
        }
        
        try {
            validarCamposProduto(produto, result);
            
            if (result.hasErrors()) {
                List<ProductImage> imagens = productService.buscarImagensProduto(id);
                StringBuilder erros = new StringBuilder("Campos inválidos: ");
                result.getFieldErrors().forEach(error -> 
                    erros.append(error.getField()).append(" (").append(error.getDefaultMessage()).append("), ")
                );
                model.addAttribute("produto", produto);
                model.addAttribute("imagens", imagens);
                model.addAttribute("erro", erros.toString());
                return "product-edit-enhanced";
            }
            
            // CORREÇÃO: Conversão do índice da imagem principal
            Long imagemPrincipalIndex = null;
            if (imagemPrincipalStr != null && !imagemPrincipalStr.trim().isEmpty()) {
                try {
                    imagemPrincipalIndex = Long.parseLong(imagemPrincipalStr);
                } catch (NumberFormatException e) {
                    // Ignora se não for um número válido
                }
            }
            
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

    // Altera status do produto (ativo/inativo) - CORRIGIDO: Apenas BACKOFFICE
    @PostMapping("/status/{id}")
    public String alterarStatus(@PathVariable Long id, @RequestParam Product.Status status, 
                               HttpSession session, RedirectAttributes redirectAttributes) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE) {
            redirectAttributes.addFlashAttribute("erro", "Apenas administradores podem alterar o status dos produtos");
            return "redirect:/produtos";
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
        if (usuarioLogado == null) {
            return ResponseEntity.status(403).body("Não autorizado");
        }
        
        // CORREÇÃO: Permite tanto BACKOFFICE quanto EXTERNO definir imagem principal
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
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