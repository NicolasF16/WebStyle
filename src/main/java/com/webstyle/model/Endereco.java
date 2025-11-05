package com.webstyle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "enderecos")
public class Endereco {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 8)
    @NotBlank(message = "CEP é obrigatório")
    @Size(min = 8, max = 8, message = "CEP deve ter 8 dígitos")
    private String cep;
    
    @Column(nullable = false)
    @NotBlank(message = "Logradouro é obrigatório")
    private String logradouro;
    
    @Column(nullable = false)
    @NotBlank(message = "Número é obrigatório")
    private String numero;
    
    @Column
    private String complemento;
    
    @Column(nullable = false)
    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;
    
    @Column(nullable = false)
    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;
    
    @Column(nullable = false, length = 2)
    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
    private String estado;
    
    @Column(nullable = false)
    private boolean faturamento; // true = endereço de faturamento, false = endereço de entrega
    
    @Column
    private String apelido; // Ex: "Casa", "Trabalho", etc. (opcional, só para entrega)
    
    @Column(nullable = false)
    private boolean enderecoPadrao = false; // true = este é o endereço padrão para entrega
    
    @Column(nullable = false)
    private boolean ativo = true; // true = endereço ativo, false = endereço inativo
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    public Endereco() {
        this.faturamento = false;
        this.enderecoPadrao = false;
        this.ativo = true;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCep() {
        return cep;
    }
    
    public void setCep(String cep) {
        this.cep = cep;
    }
    
    public String getLogradouro() {
        return logradouro;
    }
    
    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public String getComplemento() {
        return complemento;
    }
    
    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
    
    public String getBairro() {
        return bairro;
    }
    
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
    
    public String getCidade() {
        return cidade;
    }
    
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public boolean isFaturamento() {
        return faturamento;
    }
    
    public void setFaturamento(boolean faturamento) {
        this.faturamento = faturamento;
    }
    
    public String getApelido() {
        return apelido;
    }
    
    public void setApelido(String apelido) {
        this.apelido = apelido;
    }
    
    public boolean isEnderecoPadrao() {
        return enderecoPadrao;
    }
    
    public void setEnderecoPadrao(boolean enderecoPadrao) {
        this.enderecoPadrao = enderecoPadrao;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    // Método auxiliar para obter endereço completo formatado
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        if (complemento != null && !complemento.trim().isEmpty()) {
            sb.append(" - ").append(complemento);
        }
        sb.append(" - ").append(bairro);
        sb.append(" - ").append(cidade).append("/").append(estado);
        sb.append(" - CEP: ").append(formatarCep(cep));
        return sb.toString();
    }
    
    private String formatarCep(String cep) {
        if (cep.length() == 8) {
            return cep.substring(0, 5) + "-" + cep.substring(5);
        }
        return cep;
    }
}