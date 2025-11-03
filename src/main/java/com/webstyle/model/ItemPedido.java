package com.webstyle.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Product produto;
    
    // Snapshot dos dados do produto no momento da compra
    @Column(nullable = false)
    private String codigoProduto;
    
    @Column(nullable = false)
    private String nomeProduto;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;
    
    @Column(nullable = false)
    private Integer quantidade;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column
    private String imagemProduto;
    
    public ItemPedido() {
    }
    
    public ItemPedido(Product produto, Integer quantidade) {
        this.produto = produto;
        this.codigoProduto = produto.getCodigo();
        this.nomeProduto = produto.getNome();
        this.precoUnitario = produto.getPreco();
        this.quantidade = quantidade;
        this.subtotal = produto.getPreco().multiply(new BigDecimal(quantidade));
        
        if (produto.getImagemPrincipal() != null) {
            this.imagemProduto = produto.getImagemPrincipal().getCaminhoArquivo();
        }
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Pedido getPedido() {
        return pedido;
    }
    
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
    
    public Product getProduto() {
        return produto;
    }
    
    public void setProduto(Product produto) {
        this.produto = produto;
    }
    
    public String getCodigoProduto() {
        return codigoProduto;
    }
    
    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }
    
    public String getNomeProduto() {
        return nomeProduto;
    }
    
    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }
    
    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
    
    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
    
    public Integer getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public String getImagemProduto() {
        return imagemProduto;
    }
    
    public void setImagemProduto(String imagemProduto) {
        this.imagemProduto = imagemProduto;
    }
}