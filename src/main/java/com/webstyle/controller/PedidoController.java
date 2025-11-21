package com.webstyle.controller;

import com.webstyle.model.Cliente;
import com.webstyle.model.Pedido;
import com.webstyle.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller para gerenciar pedidos do cliente
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {
    
    @Autowired
    private PedidoService pedidoService;
    
    /**
     * Lista todos os pedidos do cliente logado
     * URL: GET /pedidos
     */
    @GetMapping
    public String listarPedidos(HttpSession session, Model model) {
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        List<Pedido> pedidos = pedidoService.listarPedidosCliente(clienteLogado.getId());
        
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("cliente", clienteLogado);
        
        return "pedido-lista";
    }
    
    /**
     * Exibe resumo de um pedido específico
     * URL: GET /pedidos/{id}
     */
    @GetMapping("/{id}")
    public String detalhesPedido(@PathVariable Long id, HttpSession session, Model model) {
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        Pedido pedido = pedidoService.buscarPorIdComItens(id);
        
        if (pedido == null) {
            return "redirect:/pedidos";
        }
        
        // Verifica se o pedido pertence ao cliente logado
        if (!pedido.getCliente().getId().equals(clienteLogado.getId())) {
            return "redirect:/pedidos";
        }
        
        // Debug: Log para verificar os itens
        System.out.println("=== DETALHES DO PEDIDO ===");
        System.out.println("Pedido ID: " + pedido.getId());
        System.out.println("Pedido Numero: " + pedido.getNumeroPedido());
        System.out.println("Total de itens: " + (pedido.getItens() != null ? pedido.getItens().size() : "NULL"));
        System.out.println("Status: " + pedido.getStatus());
        System.out.println("Valor Total: " + pedido.getValorTotal());
        System.out.println("Cliente: " + pedido.getCliente().getNomeCompleto());
        System.out.println("Endereco Completo: " + pedido.getEnderecoEntregaCompleto());
        
        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            System.out.println("\n--- ITENS DO PEDIDO ---");
            for (int i = 0; i < pedido.getItens().size(); i++) {
                var item = pedido.getItens().get(i);
                System.out.println("Item " + (i+1) + ":");
                System.out.println("  - Nome: " + item.getNomeProduto());
                System.out.println("  - Codigo: " + item.getCodigoProduto());
                System.out.println("  - Quantidade: " + item.getQuantidade());
                System.out.println("  - Preco Unit: " + item.getPrecoUnitario());
                System.out.println("  - Subtotal: " + item.getSubtotal());
                System.out.println("  - Imagem: " + item.getImagemProduto());
            }
        } else {
            System.out.println("\n!!! AVISO: Nenhum item encontrado no pedido !!!");
        }
        System.out.println("=========================\n");
        
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", clienteLogado);
        
        return "pedido-detalhes";
    }
    
    /**
     * Exibe detalhes completos de um pedido (nova página)
     * URL: GET /pedidos/{id}/detalhes
     */
    @GetMapping("/{id}/detalhes")
    public String detalhesCompletosPedido(@PathVariable Long id, HttpSession session, Model model) {
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        Pedido pedido = pedidoService.buscarPorIdComItens(id);
        
        if (pedido == null) {
            return "redirect:/pedidos";
        }
        
        // Verifica se o pedido pertence ao cliente logado
        if (!pedido.getCliente().getId().equals(clienteLogado.getId())) {
            return "redirect:/pedidos";
        }
        
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", clienteLogado);
        
        return "pedido-detalhes-completo";
    }
}