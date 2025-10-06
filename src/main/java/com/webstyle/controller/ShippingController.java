package com.webstyle.controller;

import com.webstyle.service.CartService;
import com.webstyle.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gerenciar cálculo de frete
 */
@Controller
@RequestMapping("/frete")
public class ShippingController {
    
    @Autowired
    private ShippingService shippingService;
    
    @Autowired
    private CartService cartService;
    
    /**
     * Calcula opções de frete via AJAX
     * URL: POST /frete/calcular
     */
    @PostMapping("/calcular")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calcularFrete(@RequestParam String cep, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== CALCULANDO FRETE ===");
            System.out.println("CEP recebido: " + cep);
            
            // Remove formatação do CEP
            String cepLimpo = cep.replaceAll("[^0-9]", "");
            System.out.println("CEP limpo: " + cepLimpo);
            
            // Valida CEP
            if (cepLimpo.length() != 8) {
                response.put("success", false);
                response.put("message", "CEP inválido. Digite um CEP com 8 dígitos.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Busca informações do CEP
            System.out.println("Consultando ViaCEP...");
            ShippingService.CepInfo cepInfo = shippingService.consultarCep(cepLimpo);
            System.out.println("CEP encontrado: " + cepInfo.getLocalidade() + "/" + cepInfo.getUf());
            
            // Obtém valor do carrinho
            BigDecimal valorCarrinho = cartService.getCartTotal();
            System.out.println("Valor do carrinho: R$ " + valorCarrinho);
            
            if (valorCarrinho.compareTo(BigDecimal.ZERO) <= 0) {
                response.put("success", false);
                response.put("message", "Carrinho vazio. Adicione produtos antes de calcular o frete.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calcula opções de frete
            System.out.println("Calculando opções de frete...");
            List<ShippingService.ShippingOption> opcoes = shippingService.calcularFrete(cepLimpo, valorCarrinho);
            System.out.println("Opções calculadas: " + opcoes.size());
            
            // Salva CEP e opções na sessão
            session.setAttribute("cepDestino", cepInfo);
            session.setAttribute("opcoesFrete", opcoes);
            
            // Prepara resposta com todos os campos necessários
            response.put("success", true);
            response.put("message", "Frete calculado com sucesso!");
            
            // Informações do CEP
            Map<String, String> cepData = new HashMap<>();
            cepData.put("cep", cepInfo.getCep());
            cepData.put("logradouro", cepInfo.getLogradouro() != null ? cepInfo.getLogradouro() : "");
            cepData.put("bairro", cepInfo.getBairro() != null ? cepInfo.getBairro() : "");
            cepData.put("localidade", cepInfo.getLocalidade());
            cepData.put("uf", cepInfo.getUf());
            response.put("cepInfo", cepData);
            
            // Opções de frete formatadas
            List<Map<String, Object>> opcoesFormatadas = new ArrayList<>();
            for (ShippingService.ShippingOption opcao : opcoes) {
                Map<String, Object> opcaoMap = new HashMap<>();
                opcaoMap.put("nome", opcao.getNome());
                opcaoMap.put("descricao", opcao.getDescricao());
                opcaoMap.put("tipo", opcao.getTipo());
                opcaoMap.put("valor", opcao.getValor());
                opcaoMap.put("prazoMinimo", opcao.getPrazoMinimo());
                opcaoMap.put("prazoMaximo", opcao.getPrazoMaximo());
                opcaoMap.put("prazoFormatado", opcao.getPrazoFormatado());
                opcaoMap.put("freteGratis", opcao.isFreteGratis());
                opcoesFormatadas.add(opcaoMap);
            }
            response.put("opcoes", opcoesFormatadas);
            response.put("valorCarrinho", valorCarrinho);
            
            System.out.println("Resposta preparada com sucesso!");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Erro de validação: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            System.err.println("Erro ao calcular frete: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erro ao calcular frete: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Seleciona uma opção de frete
     * URL: POST /frete/selecionar
     */
    @PostMapping("/selecionar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selecionarFrete(
            @RequestParam String tipoFrete,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== SELECIONANDO FRETE ===");
            System.out.println("Tipo de frete recebido: " + tipoFrete);
            
            @SuppressWarnings("unchecked")
            List<ShippingService.ShippingOption> opcoes = 
                (List<ShippingService.ShippingOption>) session.getAttribute("opcoesFrete");
            
            System.out.println("Opções na sessão: " + (opcoes != null ? opcoes.size() : "null"));
            
            if (opcoes == null || opcoes.isEmpty()) {
                System.err.println("ERRO: Nenhuma opção de frete na sessão!");
                response.put("success", false);
                response.put("message", "Calcule o frete primeiro.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Debug: lista todas as opções disponíveis
            System.out.println("Opções disponíveis:");
            for (ShippingService.ShippingOption op : opcoes) {
                System.out.println("  - Tipo: " + op.getTipo() + ", Nome: " + op.getNome());
            }
            
            // Busca a opção selecionada
            ShippingService.ShippingOption opcaoSelecionada = opcoes.stream()
                .filter(op -> op.getTipo().equals(tipoFrete))
                .findFirst()
                .orElse(null);
            
            if (opcaoSelecionada == null) {
                System.err.println("ERRO: Opção de frete não encontrada: " + tipoFrete);
                response.put("success", false);
                response.put("message", "Opção de frete inválida.");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("Opção selecionada: " + opcaoSelecionada.getNome());
            
            // Salva opção selecionada na sessão
            session.setAttribute("freteSelecionado", opcaoSelecionada);
            
            // Calcula total com frete
            BigDecimal valorCarrinho = cartService.getCartTotal();
            BigDecimal valorFrete = opcaoSelecionada.getValor();
            BigDecimal valorTotal = valorCarrinho.add(valorFrete);
            
            System.out.println("Valor carrinho: R$ " + valorCarrinho);
            System.out.println("Valor frete: R$ " + valorFrete);
            System.out.println("Valor total: R$ " + valorTotal);
            
            // Prepara resposta
            Map<String, Object> freteData = new HashMap<>();
            freteData.put("nome", opcaoSelecionada.getNome());
            freteData.put("tipo", opcaoSelecionada.getTipo());
            freteData.put("valor", opcaoSelecionada.getValor());
            freteData.put("prazo", opcaoSelecionada.getPrazoFormatado());
            
            response.put("success", true);
            response.put("message", "Frete selecionado com sucesso!");
            response.put("freteSelecionado", freteData);
            response.put("valorCarrinho", valorCarrinho);
            response.put("valorFrete", valorFrete);
            response.put("valorTotal", valorTotal);
            
            System.out.println("Resposta enviada com sucesso!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERRO ao selecionar frete: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erro ao selecionar frete: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Limpa informações de frete da sessão
     * URL: POST /frete/limpar
     */
    @PostMapping("/limpar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> limparFrete(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            session.removeAttribute("cepDestino");
            session.removeAttribute("opcoesFrete");
            session.removeAttribute("freteSelecionado");
            
            response.put("success", true);
            response.put("message", "Informações de frete removidas.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao limpar frete: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}