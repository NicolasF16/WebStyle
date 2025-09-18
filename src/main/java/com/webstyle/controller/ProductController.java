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

    // Formulário para cadastrar novo produto
    @GetMapping("/novo")
    public String novoProdutoForm(Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
        }
        
        model.addAttribute("produto", new Product());
        return "product-form-enhanced";
    }

    // CORREÇÃO: Cadastra novo produto com imagens - separando os parâmetros corretamente
    @PostMapping("/cadastro")
    public String cadastrarProduto(@RequestParam("codigo") String codigo,
                                  @RequestParam("nome") String nome,
                                  @RequestParam(value = "descricao", required = false) String descricao,
                                  @RequestParam("preco") BigDecimal preco,
                                  @RequestParam("quantidadeEstoque") Integer quantidadeEstoque,
                                  @RequestParam("avaliacao") BigDecimal avaliacao,
                                  @RequestParam("status") String status,
                                  @RequestParam(value = "imagens", required = false) List<MultipartFile> imagens,
                                  @RequestParam(value = "imagemPrincipal", required = false) String imagemPrincipalStr,
                                  Model model, 
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
        }
        
        try {
            // Cria o produto manualmente em vez de usar @ModelAttribute
            Product produto = new Product();
            produto.setCodigo(codigo);
            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(preco);
            produto.setQuantidadeEstoque(quantidadeEstoque);
            produto.setAvaliacao(avaliacao);
            produto.setStatus(Product.Status.valueOf(status));
            
            // Validações manuais
            String validationError = validarProduto(produto);
            if (validationError != null) {
                model.addAttribute("erro", validationError);
                model.addAttribute("produto", produto);
                return "product-form-enhanced";
            }
            
            // Conversão do índice da imagem principal
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
            return "product-form-enhanced";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar produto: " + e.getMessage());
            return "product-form-enhanced";
        }
    }

    // Método de validação manual
    private String validarProduto(Product produto) {
        StringBuilder erros = new StringBuilder();
        
        // Validação do código
        if (produto.getCodigo() == null || produto.getCodigo().trim().isEmpty()) {
            erros.append("Código é obrigatório. ");
        } else if (produto.getCodigo().length() > 50) {
            erros.append("Código deve ter no máximo 50 caracteres. ");
        }
        
        // Validação do nome
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            erros.append("Nome é obrigatório. ");
        } else if (produto.getNome().length() > 200) {
            erros.append("Nome deve ter no máximo 200 caracteres. ");
        }
        
        // Validação da descrição
        if (produto.getDescricao() != null && produto.getDescricao().length() > 2000) {
            erros.append("Descrição deve ter no máximo 2000 caracteres. ");
        }
        
        // Validação do preço
        if (produto.getPreco() == null) {
            erros.append("Preço é obrigatório. ");
        } else if (produto.getPreco().compareTo(new BigDecimal("0.01")) < 0) {
            erros.append("Preço deve ser maior que zero. ");
        }
        
        // Validação do estoque
        if (produto.getQuantidadeEstoque() == null) {
            erros.append("Quantidade em estoque é obrigatória. ");
        } else if (produto.getQuantidadeEstoque() < 0) {
            erros.append("Quantidade não pode ser negativa. ");
        }
        
        // Validação da avaliação
        if (produto.getAvaliacao() == null) {
            erros.append("Avaliação é obrigatória. ");
        } else {
            BigDecimal avaliacao = produto.getAvaliacao();
            if (avaliacao.compareTo(new BigDecimal("1.0")) < 0 || 
                avaliacao.compareTo(new BigDecimal("5.0")) > 0) {
                erros.append("Avaliação deve estar entre 1.0 e 5.0. ");
            }
        }
        
        // Validação do status
        if (produto.getStatus() == null) {
            erros.append("Status é obrigatório. ");
        }
        
        return erros.length() > 0 ? erros.toString() : null;
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

    // Formulário para editar produto
    @GetMapping("/editar/{id}")
    public String editarProdutoForm(@PathVariable Long id, Model model, HttpSession session) {
        User usuarioLogado = (User) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
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

    // CORREÇÃO: Salva alterações do produto - usando parâmetros individuais
    @PostMapping("/alterar/{id}")
    public String alterarProduto(@PathVariable Long id,
                                @RequestParam("codigo") String codigo,
                                @RequestParam("nome") String nome,
                                @RequestParam(value = "descricao", required = false) String descricao,
                                @RequestParam("preco") BigDecimal preco,
                                @RequestParam("quantidadeEstoque") Integer quantidadeEstoque,
                                @RequestParam("avaliacao") BigDecimal avaliacao,
                                @RequestParam("status") String status,
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
        
        if (usuarioLogado.getTipo() != User.TipoUsuario.BACKOFFICE && 
            usuarioLogado.getTipo() != User.TipoUsuario.EXTERNO) {
            return "redirect:/produtos";
        }
        
        try {
            // Cria o produto com os novos dados
            Product produto = new Product();
            produto.setCodigo(codigo);
            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(preco);
            produto.setQuantidadeEstoque(quantidadeEstoque);
            produto.setAvaliacao(avaliacao);
            produto.setStatus(Product.Status.valueOf(status));
            
            // Validações
            String validationError = validarProduto(produto);
            if (validationError != null) {
                List<ProductImage> imagens = productService.buscarImagensProduto(id);
                model.addAttribute("erro", validationError);
                model.addAttribute("produto", produto);
                model.addAttribute("imagens", imagens);
                return "product-edit-enhanced";
            }
            
            // Conversão do índice da imagem principal
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
            model.addAttribute("produto", criarProdutoComDados(codigo, nome, descricao, preco, quantidadeEstoque, avaliacao, status));
            model.addAttribute("imagens", imagens);
            return "product-edit-enhanced";
        } catch (Exception e) {
            List<ProductImage> imagens = productService.buscarImagensProduto(id);
            model.addAttribute("erro", "Erro ao alterar produto: " + e.getMessage());
            model.addAttribute("produto", criarProdutoComDados(codigo, nome, descricao, preco, quantidadeEstoque, avaliacao, status));
            model.addAttribute("imagens", imagens);
            return "product-edit-enhanced";
        }
    }
    
    // Método auxiliar para criar produto com dados
    private Product criarProdutoComDados(String codigo, String nome, String descricao, 
                                        BigDecimal preco, Integer quantidadeEstoque, 
                                        BigDecimal avaliacao, String status) {
        Product produto = new Product();
        produto.setCodigo(codigo);
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setQuantidadeEstoque(quantidadeEstoque);
        produto.setAvaliacao(avaliacao);
        try {
            produto.setStatus(Product.Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            produto.setStatus(Product.Status.ATIVO);
        }
        return produto;
    }

    // Altera status do produto (ativo/inativo)
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