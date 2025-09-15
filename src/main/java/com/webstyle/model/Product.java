package com.webstyle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false, length = 200)
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Column(columnDefinition = "TEXT", length = 2000)
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @NotNull(message = "Preço é obrigatório")
    private BigDecimal preco;

    @Column(nullable = false)
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @NotNull(message = "Quantidade em estoque é obrigatória")
    private Integer quantidadeEstoque;

    @Column(nullable = false, precision = 2, scale = 1)
    @DecimalMin(value = "1.0", message = "Avaliação deve ser no mínimo 1.0")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.0")
    private BigDecimal avaliacao;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataAtualizacao;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> imagens = new ArrayList<>();

    public enum Status {
        ATIVO, INATIVO
    }

    // Construtor padrão
    public Product() {
        this.dataCriacao = LocalDateTime.now();
        this.status = Status.ATIVO;
        this.avaliacao = new BigDecimal("1.0");
    }

    // Construtor com parâmetros
    public Product(String codigo, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, BigDecimal avaliacao) {
        this();
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.avaliacao = avaliacao;
    }

    // Métodos helper para gerenciar imagens
    public void addImagem(ProductImage imagem) {
        imagens.add(imagem);
        imagem.setProduto(this);
    }

    public void removeImagem(ProductImage imagem) {
        imagens.remove(imagem);
        imagem.setProduto(null);
    }

    public ProductImage getImagemPrincipal() {
        return imagens.stream()
                .filter(ProductImage::isImagemPrincipal)
                .findFirst()
                .orElse(imagens.isEmpty() ? null : imagens.get(0));
    }

    public void definirImagemPrincipal(Long imagemId) {
        imagens.forEach(img -> img.setImagemPrincipal(false));
        imagens.stream()
                .filter(img -> img.getId().equals(imagemId))
                .findFirst()
                .ifPresent(img -> img.setImagemPrincipal(true));
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public BigDecimal getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(BigDecimal avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public List<ProductImage> getImagens() {
        return imagens;
    }

    public void setImagens(List<ProductImage> imagens) {
        this.imagens = imagens;
    }

    // Método helper
    public boolean isAtivo() {
        return this.status == Status.ATIVO;
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}