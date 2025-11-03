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
        
        return "pedidos-lista";
    }
    
    /**
     * Exibe detalhes de um pedido específico
     * URL: GET /pedidos/{id}
     */
    @GetMapping("/{id}")
    public String detalhesPedido(@PathVariable Long id, HttpSession session, Model model) {
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        Pedido pedido = pedidoService.buscarPorId(id);
        
        if (pedido == null) {
            return "redirect:/pedidos";
        }
        
        // Verifica se o pedido pertence ao cliente logado
        if (!pedido.getCliente().getId().equals(clienteLogado.getId())) {
            return "redirect:/pedidos";
        }
        
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", clienteLogado);
        
        return "pedido-detalhes";
    }
}