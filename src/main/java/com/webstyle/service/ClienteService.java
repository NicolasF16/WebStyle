package com.webstyle.service;

import com.webstyle.model.Cliente;
import com.webstyle.model.Endereco;
import com.webstyle.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
@Transactional
public class ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Cadastra um novo cliente com validações
     */
    public Cliente cadastrarCliente(Cliente cliente) {
        // Validação de nome completo (2 palavras, mínimo 3 letras cada)
        validarNomeCompleto(cliente.getNomeCompleto());
        
        // Validação de email único
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("Email já cadastrado no sistema");
        }
        
        // Validação de CPF único
        String cpfLimpo = cliente.getCpf().replaceAll("[^0-9]", "");
        if (clienteRepository.existsByCpf(cpfLimpo)) {
            throw new RuntimeException("CPF já cadastrado no sistema");
        }
        
        // Validação de CPF
        if (!validarCPF(cpfLimpo)) {
            throw new RuntimeException("CPF inválido");
        }
        
        // Validação de idade (maior de 18 anos)
        validarIdade(cliente.getDataNascimento());
        
        // Validação de senha
        if (cliente.getSenha() == null || cliente.getSenha().length() < 6) {
            throw new RuntimeException("Senha deve ter no mínimo 6 caracteres");
        }
        
        // Validação de endereços
        if (cliente.getEnderecos() == null || cliente.getEnderecos().isEmpty()) {
            throw new RuntimeException("É necessário cadastrar pelo menos um endereço de faturamento");
        }
        
        // Verifica se há endereço de faturamento
        boolean temFaturamento = cliente.getEnderecos().stream()
                .anyMatch(Endereco::isFaturamento);
        
        if (!temFaturamento) {
            throw new RuntimeException("É necessário cadastrar um endereço de faturamento");
        }
        
        // Encripta a senha
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        
        // Armazena CPF sem formatação
        cliente.setCpf(cpfLimpo);
        
        // Define status como ativo
        cliente.setStatus(Cliente.Status.ATIVO);
        
        // Salva o cliente (cascade salva os endereços)
        return clienteRepository.save(cliente);
    }
    
    /**
     * Autentica um cliente
     */
    public Optional<Cliente> autenticar(String email, String senha) {
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
        
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            if (cliente.getStatus() == Cliente.Status.ATIVO &&
                passwordEncoder.matches(senha, cliente.getSenha())) {
                return Optional.of(cliente);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Valida nome completo (2 palavras, mínimo 3 letras cada)
     */
    private void validarNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            throw new RuntimeException("Nome completo é obrigatório");
        }
        
        String[] palavras = nomeCompleto.trim().split("\\s+");
        
        if (palavras.length < 2) {
            throw new RuntimeException("Nome completo deve conter pelo menos 2 palavras");
        }
        
        for (String palavra : palavras) {
            if (palavra.length() < 3) {
                throw new RuntimeException("Cada palavra do nome deve ter no mínimo 3 letras");
            }
        }
    }
    
    /**
     * Valida CPF
     */
    private boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        
        if (cpf.length() != 11) return false;
        
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;
        
        // Validação do primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int resto = 11 - (soma % 11);
        int digitoVerificador1 = (resto >= 10) ? 0 : resto;
        
        if (Character.getNumericValue(cpf.charAt(9)) != digitoVerificador1) {
            return false;
        }
        
        // Validação do segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        resto = 11 - (soma % 11);
        int digitoVerificador2 = (resto >= 10) ? 0 : resto;
        
        return Character.getNumericValue(cpf.charAt(10)) == digitoVerificador2;
    }
    
    /**
     * Valida se o cliente tem pelo menos 18 anos
     */
    private void validarIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new RuntimeException("Data de nascimento é obrigatória");
        }
        
        LocalDate hoje = LocalDate.now();
        Period periodo = Period.between(dataNascimento, hoje);
        
        if (periodo.getYears() < 18) {
            throw new RuntimeException("É necessário ter pelo menos 18 anos para se cadastrar");
        }
        
        if (periodo.getYears() > 120) {
            throw new RuntimeException("Data de nascimento inválida");
        }
    }
    
    /**
     * Busca cliente por ID
     */
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }
    
    /**
     * Busca cliente por email
     */
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
}