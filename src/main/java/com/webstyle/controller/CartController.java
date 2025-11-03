package com.webstyle.controller;

import com.webstyle.model.CartItem;
import com.webstyle.model.Cliente;
import com.webstyle.model.Endereco;
import com.webstyle.model.Pedido;
import com.webstyle.service.CartService;
import com.webstyle.service.ClienteService;
import com.webstyle.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gerenciar o carrinho de compras
 * Área pública - não requer autenticação
 * Sprint 4 - Sistema de Carrinho
 */
@Controller
@RequestMapping("/carrinho")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private PedidoService pedidoService;
    
    /**
     * Exibe a página do carrinho
     * URL: GET /carrinho
     */
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> cartItems = cartService.getCart();
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartService.getCartTotal());
        model.addAttribute("cartItemCount", cartService.getCartItemCount());
        model.addAttribute("isEmpty", cartService.isCartEmpty());
        
        // Pré-carrega endereço padrão se cliente estiver logado
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        if (clienteLogado != null) {
            // Recarrega cliente do banco para ter endereços atualizados
            Cliente cliente = clienteService.buscarPorId(clienteLogado.getId());
            if (cliente != null) {
                Endereco enderecoPadrao = clienteService.obterEnderecoPadrao(clienteLogado.getId());
                if (enderecoPadrao != null) {
                    model.addAttribute("enderecoPadrao", enderecoPadrao);
                    model.addAttribute("cepPadrao", enderecoPadrao.getCep());
                }
            }
        }
        
        return "cart";
    }
    
    /**
     * Adiciona produto ao carrinho via AJAX
     * REQUISITO: Soma quantidade se produto já estiver no carrinho
     * Retorna JSON com status da operação
     * URL: POST /carrinho/adicionar-ajax
     */
    @PostMapping("/adicionar-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adicionarAjax(
            @RequestParam Long produtoId,
            @RequestParam(defaultValue = "1") Integer quantidade) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // O CartService já implementa a lógica de SOMAR quantidade
            cartService.addToCart(produtoId, quantidade);
            
            response.put("success", true);
            response.put("message", "Produto adicionado ao carrinho com sucesso!");
            response.put("cartItemCount", cartService.getCartItemCount());
            response.put("cartTotal", cartService.getCartTotal());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Adiciona produto ao carrinho via POST normal (redirecionamento)
     * Usado em formulários tradicionais
     * URL: POST /carrinho/adicionar
     */
    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Long produtoId,
                           @RequestParam(defaultValue = "1") Integer quantidade,
                           RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(produtoId, quantidade);
            redirectAttributes.addFlashAttribute("sucesso", "Produto adicionado ao carrinho!");
            return "redirect:/carrinho";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/produto/" + produtoId;
        }
    }
    
    /**
     * Remove produto do carrinho
     * URL: POST /carrinho/remover
     */
    @PostMapping("/remover")
    public String remover(@RequestParam Long produtoId,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(produtoId);
            redirectAttributes.addFlashAttribute("sucesso", "Produto removido do carrinho!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover produto: " + e.getMessage());
        }
        
        return "redirect:/carrinho";
    }
    
    /**
     * Atualiza quantidade de um produto no carrinho
     * URL: POST /carrinho/atualizar
     */
    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long produtoId,
                           @RequestParam Integer quantidade,
                           RedirectAttributes redirectAttributes) {
        try {
            if (quantidade <= 0) {
                cartService.removeFromCart(produtoId);
                redirectAttributes.addFlashAttribute("sucesso", "Produto removido do carrinho!");
            } else {
                cartService.updateQuantity(produtoId, quantidade);
                redirectAttributes.addFlashAttribute("sucesso", "Quantidade atualizada!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        
        return "redirect:/carrinho";
    }
    
    /**
     * Limpa todo o carrinho
     * URL: POST /carrinho/limpar
     */
    @PostMapping("/limpar")
    public String limpar(RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart();
            redirectAttributes.addFlashAttribute("sucesso", "Carrinho limpo com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao limpar carrinho: " + e.getMessage());
        }
        
        return "redirect:/carrinho";
    }
    
    /**
     * Retorna informações do carrinho em JSON
     * Usado para atualizar badge do carrinho
     * URL: GET /carrinho/info
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("itemCount", cartService.getCartItemCount());
        info.put("total", cartService.getCartTotal());
        info.put("isEmpty", cartService.isCartEmpty());
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Finaliza a compra (placeholder para futuras implementações)
     * URL: POST /carrinho/finalizar
     */
    @PostMapping("/finalizar")
    public String finalizar(RedirectAttributes redirectAttributes) {
        // TODO: Implementar lógica de checkout/pedido
        
        if (cartService.isCartEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Carrinho vazio! Adicione produtos antes de finalizar.");
            return "redirect:/carrinho";
        }
        
        // Por enquanto apenas mostra mensagem
        redirectAttributes.addFlashAttribute("sucesso", 
            "Funcionalidade de checkout em desenvolvimento! " +
            "Total: R$ " + cartService.getCartTotal());
        
        return "redirect:/carrinho";
    }
    
    /**
     * Finaliza a compra criando um pedido
     * URL: POST /carrinho/finalizar-pedido
     */
    @PostMapping("/finalizar-pedido")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> finalizarPedido(
            @RequestParam Long enderecoId,
            @RequestParam String tipoFrete,
            @RequestParam String nomeFrete,
            @RequestParam String prazoEntrega,
            @RequestParam BigDecimal valorFrete,
            @RequestParam String formaPagamento,
            @RequestParam(required = false) Integer numeroParcelas,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== FINALIZANDO PEDIDO ===");
            
            // Verifica se cliente está logado
            Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
            if (clienteLogado == null) {
                response.put("success", false);
                response.put("message", "Você precisa estar logado para finalizar a compra");
                return ResponseEntity.status(401).body(response);
            }
            
            // Obtém itens do carrinho
            List<CartItem> cartItems = cartService.getCart();
            if (cartItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Carrinho vazio");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calcula valores
            BigDecimal valorSubtotal = cartService.getCartTotal();
            
            System.out.println("Endereço ID: " + enderecoId);
            System.out.println("Tipo Frete: " + tipoFrete);
            System.out.println("Valor Subtotal: " + valorSubtotal);
            System.out.println("Valor Frete: " + valorFrete);
            System.out.println("Forma Pagamento: " + formaPagamento);
            
            // Cria o pedido
            Pedido pedido = pedidoService.criarPedido(
                clienteLogado,
                cartItems,
                enderecoId,
                valorSubtotal,
                valorFrete,
                tipoFrete,
                nomeFrete,
                prazoEntrega,
                formaPagamento,
                numeroParcelas
            );
            
            System.out.println("Pedido criado: " + pedido.getNumeroPedido());
            
            // Limpa o carrinho
            cartService.clearCart();
            
            // Limpa informações de frete da sessão
            session.removeAttribute("cepDestino");
            session.removeAttribute("opcoesFrete");
            session.removeAttribute("freteSelecionado");
            
            // Prepara resposta
            response.put("success", true);
            response.put("message", "Pedido realizado com sucesso!");
            response.put("numeroPedido", pedido.getNumeroPedido());
            response.put("valorTotal", pedido.getValorTotal());
            response.put("dataPedido", pedido.getDataPedido().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERRO ao finalizar pedido: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erro ao finalizar pedido: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}