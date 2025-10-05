package com.webstyle.controller;

import com.webstyle.model.Product;
import com.webstyle.model.ProductImage;
import com.webstyle.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Controller público para a área de visualização de produtos (Sprint 3)
 * Não requer autenticação - acessível para todos os visitantes
 */
@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * Página inicial pública - Exibe cards de produtos ativos
     * Requisitos Sprint 3:
     * - Logo da loja
     * - Link "Faça login/Crie seu login"
     * - Cards com: imagem principal, nome, preço, botão "Ver Detalhes"
     * 
     * URL: /home ou /
     */
    @GetMapping("/home")
    public String home(Model model) {
        // Busca apenas produtos ATIVOS para exibir na página pública
        List<Product> produtosAtivos = productService.listarProdutosAtivos();
        model.addAttribute("produtos", produtosAtivos);
        return "home";
    }
    
    /**
     * Página de detalhes do produto (visão pública)
     * Exibe informações completas do produto para visitantes não autenticados
     * 
     * URL: /produto/{id}
     */
    @GetMapping("/produto/{id}")
    public String produtoDetalhe(@PathVariable Long id, Model model) {
        // Busca o produto
        Product produto = productService.buscarPorId(id);
        
        // Verifica se o produto existe e está ativo
        if (produto == null || produto.getStatus() != Product.Status.ATIVO) {
            // Redireciona para home se produto não existir ou estiver inativo
            return "redirect:/home";
        }
        
        // Busca as imagens do produto
        List<ProductImage> imagens = productService.buscarImagensProduto(id);
        
        // Adiciona ao modelo
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        
        return "product-detail";
    }
}