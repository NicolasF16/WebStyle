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
    public String loginForm(HttpSession session, Model model) {
        // Se já estiver logado, redireciona para home
        if (session.getAttribute("clienteLogado") != null) {
            return "redirect:/home";
        }
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
            // Mensagem de erro específica
            model.addAttribute("erro", "Usuário ou senha incorretos. Verifique seus dados e tente novamente.");
            model.addAttribute("email", email);
            return "cliente-login";
        }
    }
    
    /**
     * Exibe página de cadastro do cliente
     * URL: GET /cliente/cadastro
     */
    @GetMapping("/cadastro")
    public String cadastroForm(Model model, HttpSession session) {
        // Se já estiver logado, redireciona para home
        if (session.getAttribute("clienteLogado") != null) {
            return "redirect:/home";
        }
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }
    
    /**
     * Processa cadastro do cliente
     * URL: POST /cliente/cadastro
     * 
     * ALTERAÇÃO: Agora redireciona para a tela de login após cadastro bem-sucedido
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
                           HttpSession session,
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
            enderecoFaturamento.setEnderecoPadrao(false);
            
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
            enderecoEntrega.setEnderecoPadrao(true); // Primeiro endereço de entrega é padrão
            
            cliente.addEndereco(enderecoEntrega);
            
            // Cadastra o cliente
            Cliente clienteCadastrado = clienteService.cadastrarCliente(cliente);
            
            // ===== ALTERAÇÃO PRINCIPAL =====
            // Redireciona para a tela de login com mensagem de sucesso
            redirectAttributes.addFlashAttribute("sucesso", 
                "Cadastro realizado com sucesso, " + clienteCadastrado.getNomeCompleto() + "! " +
                "Agora você pode fazer login com seu e-mail e senha.");
            
            return "redirect:/cliente/login";
            // ===== FIM DA ALTERAÇÃO =====
            
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
     * Exibe página de edição de perfil
     * URL: GET /cliente/perfil
     */
    @GetMapping("/perfil")
    public String perfilForm(HttpSession session, Model model) {
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        // Recarrega os dados do banco para garantir que estão atualizados
        Cliente cliente = clienteService.buscarPorId(clienteLogado.getId());
        if (cliente == null) {
            session.removeAttribute("clienteLogado");
            return "redirect:/cliente/login";
        }
        
        model.addAttribute("cliente", cliente);
        return "cliente-perfil";
    }
    
    /**
     * Atualiza dados do perfil
     * URL: POST /cliente/perfil/atualizar
     */
    @PostMapping("/perfil/atualizar")
    public String atualizarPerfil(@RequestParam String nomeCompleto,
                                  @RequestParam String dataNascimento,
                                  @RequestParam String genero,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        try {
            clienteService.atualizarPerfil(
                clienteLogado.getId(),
                nomeCompleto,
                LocalDate.parse(dataNascimento),
                genero
            );
            
            // Atualiza a sessão com os novos dados
            Cliente clienteAtualizado = clienteService.buscarPorId(clienteLogado.getId());
            session.setAttribute("clienteLogado", clienteAtualizado);
            
            redirectAttributes.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
            return "redirect:/cliente/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar perfil: " + e.getMessage());
            return "redirect:/cliente/perfil";
        }
    }
    
    /**
     * Altera senha do cliente
     * URL: POST /cliente/perfil/alterar-senha
     */
    @PostMapping("/perfil/alterar-senha")
    public String alterarSenha(@RequestParam String senhaAtual,
                              @RequestParam String novaSenha,
                              @RequestParam String confirmarNovaSenha,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        try {
            if (!novaSenha.equals(confirmarNovaSenha)) {
                throw new RuntimeException("As senhas não coincidem");
            }
            
            clienteService.alterarSenha(clienteLogado.getId(), senhaAtual, novaSenha);
            
            redirectAttributes.addFlashAttribute("sucesso", "Senha alterada com sucesso!");
            return "redirect:/cliente/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/cliente/perfil";
        }
    }
    
    /**
     * Adiciona novo endereço de entrega
     * URL: POST /cliente/perfil/adicionar-endereco
     */
    @PostMapping("/perfil/adicionar-endereco")
    public String adicionarEndereco(@RequestParam String cep,
                                    @RequestParam String logradouro,
                                    @RequestParam String numero,
                                    @RequestParam(required = false) String complemento,
                                    @RequestParam String bairro,
                                    @RequestParam String cidade,
                                    @RequestParam String estado,
                                    @RequestParam String apelido,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        try {
            Endereco novoEndereco = new Endereco();
            novoEndereco.setCep(cep.replaceAll("[^0-9]", ""));
            novoEndereco.setLogradouro(logradouro.trim());
            novoEndereco.setNumero(numero.trim());
            novoEndereco.setComplemento(complemento != null ? complemento.trim() : null);
            novoEndereco.setBairro(bairro.trim());
            novoEndereco.setCidade(cidade.trim());
            novoEndereco.setEstado(estado.toUpperCase());
            novoEndereco.setApelido(apelido.trim());
            novoEndereco.setFaturamento(false);
            
            clienteService.adicionarEndereco(clienteLogado.getId(), novoEndereco);
            
            // Atualiza a sessão
            Cliente clienteAtualizado = clienteService.buscarPorId(clienteLogado.getId());
            session.setAttribute("clienteLogado", clienteAtualizado);
            
            redirectAttributes.addFlashAttribute("sucesso", "Endereço adicionado com sucesso!");
            return "redirect:/cliente/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao adicionar endereço: " + e.getMessage());
            return "redirect:/cliente/perfil";
        }
    }
    
    /**
     * Remove endereço de entrega
     * URL: POST /cliente/perfil/remover-endereco/{id}
     */
    @PostMapping("/perfil/remover-endereco/{enderecoId}")
    public String removerEndereco(@PathVariable Long enderecoId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        try {
            clienteService.removerEndereco(clienteLogado.getId(), enderecoId);
            
            // Atualiza a sessão
            Cliente clienteAtualizado = clienteService.buscarPorId(clienteLogado.getId());
            session.setAttribute("clienteLogado", clienteAtualizado);
            
            redirectAttributes.addFlashAttribute("sucesso", "Endereço removido com sucesso!");
            return "redirect:/cliente/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover endereço: " + e.getMessage());
            return "redirect:/cliente/perfil";
        }
    }
    
    /**
     * Define endereço como padrão
     * URL: POST /cliente/perfil/definir-endereco-padrao/{enderecoId}
     */
    @PostMapping("/perfil/definir-endereco-padrao/{enderecoId}")
    public String definirEnderecoPadrao(@PathVariable Long enderecoId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        
        Cliente clienteLogado = (Cliente) session.getAttribute("clienteLogado");
        
        if (clienteLogado == null) {
            return "redirect:/cliente/login";
        }
        
        try {
            clienteService.definirEnderecoPadrao(clienteLogado.getId(), enderecoId);
            
            // Atualiza a sessão
            Cliente clienteAtualizado = clienteService.buscarPorId(clienteLogado.getId());
            session.setAttribute("clienteLogado", clienteAtualizado);
            
            redirectAttributes.addFlashAttribute("sucesso", "Endereço definido como padrão!");
            return "redirect:/cliente/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao definir endereço padrão: " + e.getMessage());
            return "redirect:/cliente/perfil";
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