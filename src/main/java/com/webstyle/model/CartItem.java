package com.webstyle.model;

import java.math.BigDecimal;

/**
 * Representa um item no carrinho de compras
 * Armazenado na sessão HTTP do usuário
 */
public class CartItem {
    private Long produtoId;
    private String codigo;
    private String nome;
    private BigDecimal preco;
    private Integer quantidade;
    private String imagemPrincipal; // Caminho da imagem

    public CartItem() {
    }

    public CartItem(Long produtoId, String codigo, String nome, BigDecimal preco, Integer quantidade, String imagemPrincipal) {
        this.produtoId = produtoId;
        this.codigo = codigo;
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
        this.imagemPrincipal = imagemPrincipal;
    }

    // Método auxiliar para calcular subtotal
    public BigDecimal getSubtotal() {
        return preco.multiply(new BigDecimal(quantidade));
    }

    // Getters e Setters
    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getImagemPrincipal() {
        return imagemPrincipal;
    }

    public void setImagemPrincipal(String imagemPrincipal) {
        this.imagemPrincipal = imagemPrincipal;
    }
}