package com.webstyle.service;

import com.webstyle.model.Cliente;
import com.webstyle.model.Endereco;
import com.webstyle.repository.ClienteRepository;
import com.webstyle.repository.EnderecoRepository;
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
    
    @Autowired
    private EnderecoRepository enderecoRepository;
    
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
     * NOVO: Atualiza dados do perfil do cliente
     */
    public void atualizarPerfil(Long clienteId, String nomeCompleto, LocalDate dataNascimento, String genero) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        // Validações
        validarNomeCompleto(nomeCompleto);
        validarIdade(dataNascimento);
        
        cliente.setNomeCompleto(nomeCompleto.trim());
        cliente.setDataNascimento(dataNascimento);
        cliente.setGenero(genero);
        
        clienteRepository.save(cliente);
    }
    
    /**
     * NOVO: Altera a senha do cliente
     */
    public void alterarSenha(Long clienteId, String senhaAtual, String novaSenha) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        // Verifica se a senha atual está correta
        if (!passwordEncoder.matches(senhaAtual, cliente.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        
        // Valida nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new RuntimeException("Nova senha deve ter no mínimo 6 caracteres");
        }
        
        // Atualiza a senha
        cliente.setSenha(passwordEncoder.encode(novaSenha));
        clienteRepository.save(cliente);
    }
    
    /**
     * NOVO: Adiciona novo endereço de entrega
     */
    public void adicionarEndereco(Long clienteId, Endereco endereco) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        // Validações do endereço
        if (endereco.getCep() == null || endereco.getCep().length() != 8) {
            throw new RuntimeException("CEP inválido");
        }
        
        if (endereco.getLogradouro() == null || endereco.getLogradouro().trim().isEmpty()) {
            throw new RuntimeException("Logradouro é obrigatório");
        }
        
        if (endereco.getNumero() == null || endereco.getNumero().trim().isEmpty()) {
            throw new RuntimeException("Número é obrigatório");
        }
        
        if (endereco.getBairro() == null || endereco.getBairro().trim().isEmpty()) {
            throw new RuntimeException("Bairro é obrigatório");
        }
        
        if (endereco.getCidade() == null || endereco.getCidade().trim().isEmpty()) {
            throw new RuntimeException("Cidade é obrigatória");
        }
        
        if (endereco.getEstado() == null || endereco.getEstado().length() != 2) {
            throw new RuntimeException("Estado inválido");
        }
        
        if (endereco.getApelido() == null || endereco.getApelido().trim().isEmpty()) {
            throw new RuntimeException("Apelido do endereço é obrigatório");
        }
        
        // Não permite adicionar endereço de faturamento por aqui
        endereco.setFaturamento(false);
        
        cliente.addEndereco(endereco);
        clienteRepository.save(cliente);
    }
    
    /**
     * NOVO: Remove endereço de entrega
     */
    public void removerEndereco(Long clienteId, Long enderecoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
        
        // Verifica se o endereço pertence ao cliente
        if (!endereco.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("Endereço não pertence a este cliente");
        }
        
        // Não permite remover endereço de faturamento
        if (endereco.isFaturamento()) {
            throw new RuntimeException("Não é possível remover o endereço de faturamento");
        }
        
        // Não permite remover o último endereço de entrega
        long qtdEnderecosEntrega = cliente.getEnderecos().stream()
                .filter(e -> !e.isFaturamento())
                .count();
        
        if (qtdEnderecosEntrega <= 1) {
            throw new RuntimeException("É necessário ter pelo menos um endereço de entrega cadastrado");
        }
        
        cliente.removeEndereco(endereco);
        enderecoRepository.delete(endereco);
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