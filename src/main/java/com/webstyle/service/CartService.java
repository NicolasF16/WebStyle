package com.webstyle.service;

import com.webstyle.model.CartItem;
import com.webstyle.model.Product;
import com.webstyle.model.ProductImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar o carrinho de compras
 * Utiliza a sessão HTTP para armazenar os itens
 */
@Service
public class CartService {
    
    private static final String CART_SESSION_KEY = "SHOPPING_CART";
    
    @Autowired
    private ProductService productService;
    
    /**
     * Obtém a sessão HTTP atual
     */
    private HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }
    
    /**
     * Obtém o carrinho da sessão
     */
    @SuppressWarnings("unchecked")
    public List<CartItem> getCart() {
        HttpSession session = getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        
        return cart;
    }
    
    /**
     * Adiciona um produto ao carrinho
     * Se o produto já existir, soma a quantidade
     */
    public void addToCart(Long produtoId, Integer quantidade) {
        if (produtoId == null || quantidade == null || quantidade <= 0) {
            throw new RuntimeException("Produto inválido ou quantidade inválida");
        }
        
        // Busca o produto
        Product produto = productService.buscarPorId(produtoId);
        if (produto == null) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        // Verifica se o produto está ativo
        if (produto.getStatus() != Product.Status.ATIVO) {
            throw new RuntimeException("Produto não está disponível para compra");
        }
        
        // Verifica se há estoque suficiente
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getQuantidadeEstoque());
        }
        
        List<CartItem> cart = getCart();
        
        // Busca a imagem principal do produto
        List<ProductImage> imagens = productService.buscarImagensProduto(produtoId);
        String imagemPrincipal = null;
        if (imagens != null && !imagens.isEmpty()) {
            ProductImage imgPrincipal = imagens.stream()
                    .filter(ProductImage::isImagemPrincipal)
                    .findFirst()
                    .orElse(imagens.get(0));
            imagemPrincipal = imgPrincipal.getCaminhoArquivo();
        }
        
        // Verifica se o produto já está no carrinho
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getProdutoId().equals(produtoId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Produto já existe: SOMA a quantidade
            CartItem item = existingItem.get();
            int novaQuantidade = item.getQuantidade() + quantidade;
            
            // Verifica se a nova quantidade não excede o estoque
            if (novaQuantidade > produto.getQuantidadeEstoque()) {
                throw new RuntimeException("Quantidade total excede o estoque disponível. Máximo: " + produto.getQuantidadeEstoque());
            }
            
            item.setQuantidade(novaQuantidade);
        } else {
            // Produto novo: adiciona ao carrinho
            CartItem newItem = new CartItem(
                produtoId,
                produto.getCodigo(),
                produto.getNome(),
                produto.getPreco(),
                quantidade,
                imagemPrincipal
            );
            cart.add(newItem);
        }
        
        // Atualiza o carrinho na sessão
        HttpSession session = getSession();
        session.setAttribute(CART_SESSION_KEY, cart);
    }
    
    /**
     * Remove um produto do carrinho
     */
    public void removeFromCart(Long produtoId) {
        List<CartItem> cart = getCart();
        cart.removeIf(item -> item.getProdutoId().equals(produtoId));
        
        HttpSession session = getSession();
        session.setAttribute(CART_SESSION_KEY, cart);
    }
    
    /**
     * Atualiza a quantidade de um produto no carrinho
     */
    public void updateQuantity(Long produtoId, Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade <= 0) {
            removeFromCart(produtoId);
            return;
        }
        
        // Verifica se há estoque suficiente
        Product produto = productService.buscarPorId(produtoId);
        if (produto == null) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        if (novaQuantidade > produto.getQuantidadeEstoque()) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getQuantidadeEstoque());
        }
        
        List<CartItem> cart = getCart();
        Optional<CartItem> item = cart.stream()
                .filter(i -> i.getProdutoId().equals(produtoId))
                .findFirst();
        
        if (item.isPresent()) {
            item.get().setQuantidade(novaQuantidade);
            
            HttpSession session = getSession();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }
    
    /**
     * Limpa o carrinho
     */
    public void clearCart() {
        HttpSession session = getSession();
        session.setAttribute(CART_SESSION_KEY, new ArrayList<CartItem>());
    }
    
    /**
     * Calcula o total do carrinho
     */
    public BigDecimal getCartTotal() {
        List<CartItem> cart = getCart();
        return cart.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Retorna a quantidade total de itens no carrinho
     */
    public int getCartItemCount() {
        List<CartItem> cart = getCart();
        return cart.stream()
                .mapToInt(CartItem::getQuantidade)
                .sum();
    }
    
    /**
     * Verifica se o carrinho está vazio
     */
    public boolean isCartEmpty() {
        return getCart().isEmpty();
    }
}