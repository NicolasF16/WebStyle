package com.webstyle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;
    
    @Column(unique = true, nullable = false, length = 11)
    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    private String cpf;
    
    @Column(nullable = false)
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
    
    @Column(nullable = false)
    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;
    
    @Column(nullable = false)
    @NotBlank(message = "Gênero é obrigatório")
    private String genero; // MASCULINO, FEMININO, OUTRO
    
    @Column(nullable = false)
    private LocalDateTime dataCadastro;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    // CORREÇÃO PRINCIPAL: Mudei de LAZY para EAGER para garantir que os endereços sejam sempre carregados
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Endereco> enderecos = new ArrayList<>();
    
    public enum Status {
        ATIVO, INATIVO
    }
    
    public Cliente() {
        this.dataCadastro = LocalDateTime.now();
        this.status = Status.ATIVO;
    }
    
    // Método para adicionar endereço
    public void addEndereco(Endereco endereco) {
        enderecos.add(endereco);
        endereco.setCliente(this);
    }
    
    // Método para remover endereço
    public void removeEndereco(Endereco endereco) {
        enderecos.remove(endereco);
        endereco.setCliente(null);
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNomeCompleto() {
        return nomeCompleto;
    }
    
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public List<Endereco> getEnderecos() {
        return enderecos;
    }
    
    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }
    
    // Métodos auxiliares
    public Endereco getEnderecoFaturamento() {
        return enderecos.stream()
                .filter(Endereco::isFaturamento)
                .findFirst()
                .orElse(null);
    }
    
    public List<Endereco> getEnderecosEntrega() {
        return enderecos.stream()
                .filter(e -> !e.isFaturamento())
                .toList();
    }
}