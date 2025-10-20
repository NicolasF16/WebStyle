package com.webstyle.controller;

import com.webstyle.model.Cliente;
import com.webstyle.model.Endereco;
import com.webstyle.service.ClienteService;
import com.webstyle.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller para gerenciar clientes da loja (não é backoffice)
 */
@Controller
@RequestMapping("/cliente")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ShippingService shippingService;
    
    /**
     * Exibe página de login do cliente
     * URL: GET /cliente/login
     */
    @GetMapping("/login")
    public String loginForm() {
        return "cliente-login";
    }
    
    /**
     * Processa login do cliente
     * URL: POST /cliente/login
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                       @RequestParam String senha,
                       Model model,
                       HttpSession session) {
        
        Optional<Cliente> clienteOpt = clienteService.autenticar(email, senha);
        
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            // Salva cliente na sessão
            session.setAttribute("clienteLogado", cliente);
            
            // Redireciona para a home
            return "redirect:/home";
        } else {
            model.addAttribute("erro", "E-mail ou senha inválidos, ou conta inativa.");
            return "cliente-login";
        }
    }
    
    /**
     * Exibe página de cadastro do cliente
     * URL: GET /cliente/cadastro
     */
    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }
    
    /**
     * Processa cadastro do cliente
     * URL: POST /cliente/cadastro
     */
    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nomeCompleto,
                           @RequestParam String email,
                           @RequestParam String cpf,
                           @RequestParam String senha,
                           @RequestParam String confirmarSenha,
                           @RequestParam String dataNascimento,
                           @RequestParam String genero,
                           // Endereço de Faturamento
                           @RequestParam String cepFaturamento,
                           @RequestParam String logradouroFaturamento,
                           @RequestParam String numeroFaturamento,
                           @RequestParam(required = false) String complementoFaturamento,
                           @RequestParam String bairroFaturamento,
                           @RequestParam String cidadeFaturamento,
                           @RequestParam String estadoFaturamento,
                           // Endereço de Entrega
                           @RequestParam(required = false, defaultValue = "false") boolean copiarEndereco,
                           @RequestParam(required = false) String cepEntrega,
                           @RequestParam(required = false) String logradouroEntrega,
                           @RequestParam(required = false) String numeroEntrega,
                           @RequestParam(required = false) String complementoEntrega,
                           @RequestParam(required = false) String bairroEntrega,
                           @RequestParam(required = false) String cidadeEntrega,
                           @RequestParam(required = false) String estadoEntrega,
                           @RequestParam(required = false) String apelidoEntrega,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        try {
            // Validação de senhas
            if (!senha.equals(confirmarSenha)) {
                throw new RuntimeException("As senhas não coincidem");
            }
            
            // Cria o cliente
            Cliente cliente = new Cliente();
            cliente.setNomeCompleto(nomeCompleto.trim());
            cliente.setEmail(email.trim().toLowerCase());
            cliente.setCpf(cpf.replaceAll("[^0-9]", ""));
            cliente.setSenha(senha);
            cliente.setDataNascimento(LocalDate.parse(dataNascimento));
            cliente.setGenero(genero);
            
            // Cria endereço de faturamento
            Endereco enderecoFaturamento = new Endereco();
            enderecoFaturamento.setCep(cepFaturamento.replaceAll("[^0-9]", ""));
            enderecoFaturamento.setLogradouro(logradouroFaturamento.trim());
            enderecoFaturamento.setNumero(numeroFaturamento.trim());
            enderecoFaturamento.setComplemento(complementoFaturamento != null ? complementoFaturamento.trim() : null);
            enderecoFaturamento.setBairro(bairroFaturamento.trim());
            enderecoFaturamento.setCidade(cidadeFaturamento.trim());
            enderecoFaturamento.setEstado(estadoFaturamento.toUpperCase());
            enderecoFaturamento.setFaturamento(true);
            
            cliente.addEndereco(enderecoFaturamento);
            
            // Cria endereço de entrega
            Endereco enderecoEntrega = new Endereco();
            
            if (copiarEndereco) {
                // Copia do endereço de faturamento
                enderecoEntrega.setCep(enderecoFaturamento.getCep());
                enderecoEntrega.setLogradouro(enderecoFaturamento.getLogradouro());
                enderecoEntrega.setNumero(enderecoFaturamento.getNumero());
                enderecoEntrega.setComplemento(enderecoFaturamento.getComplemento());
                enderecoEntrega.setBairro(enderecoFaturamento.getBairro());
                enderecoEntrega.setCidade(enderecoFaturamento.getCidade());
                enderecoEntrega.setEstado(enderecoFaturamento.getEstado());
            } else {
                // Validação de campos obrigatórios para endereço de entrega
                if (cepEntrega == null || cepEntrega.trim().isEmpty()) {
                    throw new RuntimeException("CEP de entrega é obrigatório quando não copiar do faturamento");
                }
                
                enderecoEntrega.setCep(cepEntrega.replaceAll("[^0-9]", ""));
                enderecoEntrega.setLogradouro(logradouroEntrega.trim());
                enderecoEntrega.setNumero(numeroEntrega.trim());
                enderecoEntrega.setComplemento(complementoEntrega != null ? complementoEntrega.trim() : null);
                enderecoEntrega.setBairro(bairroEntrega.trim());
                enderecoEntrega.setCidade(cidadeEntrega.trim());
                enderecoEntrega.setEstado(estadoEntrega.toUpperCase());
            }
            
            enderecoEntrega.setFaturamento(false);
            enderecoEntrega.setApelido(apelidoEntrega != null && !apelidoEntrega.trim().isEmpty() ? apelidoEntrega.trim() : "Principal");
            
            cliente.addEndereco(enderecoEntrega);
            
            // Cadastra o cliente
            clienteService.cadastrarCliente(cliente);
            
            // Redireciona para login com mensagem de sucesso
            redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/cliente/login";
            
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            
            // Mantém os dados preenchidos no formulário
            Cliente cliente = new Cliente();
            cliente.setNomeCompleto(nomeCompleto);
            cliente.setEmail(email);
            cliente.setCpf(cpf);
            cliente.setDataNascimento(dataNascimento != null && !dataNascimento.isEmpty() ? LocalDate.parse(dataNascimento) : null);
            cliente.setGenero(genero);
            
            model.addAttribute("cliente", cliente);
            model.addAttribute("cepFaturamento", cepFaturamento);
            model.addAttribute("logradouroFaturamento", logradouroFaturamento);
            model.addAttribute("numeroFaturamento", numeroFaturamento);
            model.addAttribute("complementoFaturamento", complementoFaturamento);
            model.addAttribute("bairroFaturamento", bairroFaturamento);
            model.addAttribute("cidadeFaturamento", cidadeFaturamento);
            model.addAttribute("estadoFaturamento", estadoFaturamento);
            
            return "cadastro-cliente";
        }
    }
    
    /**
     * Endpoint AJAX para buscar CEP
     * URL: GET /cliente/buscar-cep
     */
    @GetMapping("/buscar-cep")
    @ResponseBody
    public Object buscarCep(@RequestParam String cep) {
        try {
            String cepLimpo = cep.replaceAll("[^0-9]", "");
            return shippingService.consultarCep(cepLimpo);
        } catch (IOException e) {
            return "Erro ao consultar CEP: " + e.getMessage();
        }
    }
    
    /**
     * Logout do cliente
     * URL: GET /cliente/logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("clienteLogado");
        redirectAttributes.addFlashAttribute("sucesso", "Você saiu da sua conta com sucesso!");
        return "redirect:/home";
    }
}