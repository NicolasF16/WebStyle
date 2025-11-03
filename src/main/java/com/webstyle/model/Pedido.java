package com.webstyle.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String numeroPedido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @Column(nullable = false)
    private LocalDateTime dataPedido;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSubtotal;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;
    
    @Column
    private Integer numeroParcelas;
    
    // Endereço de entrega (snapshot do endereço no momento do pedido)
    @Column(nullable = false)
    private String enderecoEntregaCep;
    
    @Column(nullable = false)
    private String enderecoEntregaLogradouro;
    
    @Column(nullable = false)
    private String enderecoEntregaNumero;
    
    @Column
    private String enderecoEntregaComplemento;
    
    @Column(nullable = false)
    private String enderecoEntregaBairro;
    
    @Column(nullable = false)
    private String enderecoEntregaCidade;
    
    @Column(nullable = false, length = 2)
    private String enderecoEntregaEstado;
    
    // Informações do frete
    @Column(nullable = false)
    private String tipoFrete;
    
    @Column(nullable = false)
    private String nomeFrete;
    
    @Column(nullable = false)
    private String prazoEntrega;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
    
    public enum StatusPedido {
        AGUARDANDO_PAGAMENTO("Aguardando Pagamento"),
        PAGAMENTO_CONFIRMADO("Pagamento Confirmado"),
        EM_SEPARACAO("Em Separação"),
        EM_TRANSPORTE("Em Transporte"),
        ENTREGUE("Entregue"),
        CANCELADO("Cancelado");
        
        private final String descricao;
        
        StatusPedido(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public enum FormaPagamento {
        CARTAO_CREDITO("Cartão de Crédito"),
        BOLETO("Boleto Bancário");
        
        private final String descricao;
        
        FormaPagamento(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public Pedido() {
        this.dataPedido = LocalDateTime.now();
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
    }
    
    // Método helper para adicionar item
    public void addItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }
    
    // Método helper para obter endereço completo
    public String getEnderecoEntregaCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(enderecoEntregaLogradouro).append(", ").append(enderecoEntregaNumero);
        if (enderecoEntregaComplemento != null && !enderecoEntregaComplemento.isEmpty()) {
            sb.append(" - ").append(enderecoEntregaComplemento);
        }
        sb.append(" - ").append(enderecoEntregaBairro);
        sb.append(" - ").append(enderecoEntregaCidade).append("/").append(enderecoEntregaEstado);
        sb.append(" - CEP: ").append(formatarCep(enderecoEntregaCep));
        return sb.toString();
    }
    
    private String formatarCep(String cep) {
        if (cep.length() == 8) {
            return cep.substring(0, 5) + "-" + cep.substring(5);
        }
        return cep;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNumeroPedido() {
        return numeroPedido;
    }
    
    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public LocalDateTime getDataPedido() {
        return dataPedido;
    }
    
    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }
    
    public BigDecimal getValorSubtotal() {
        return valorSubtotal;
    }
    
    public void setValorSubtotal(BigDecimal valorSubtotal) {
        this.valorSubtotal = valorSubtotal;
    }
    
    public BigDecimal getValorFrete() {
        return valorFrete;
    }
    
    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }
    
    public BigDecimal getValorTotal() {
        return valorTotal;
    }
    
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
    
    public StatusPedido getStatus() {
        return status;
    }
    
    public void setStatus(StatusPedido status) {
        this.status = status;
    }
    
    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }
    
    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public Integer getNumeroParcelas() {
        return numeroParcelas;
    }
    
    public void setNumeroParcelas(Integer numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }
    
    public String getEnderecoEntregaCep() {
        return enderecoEntregaCep;
    }
    
    public void setEnderecoEntregaCep(String enderecoEntregaCep) {
        this.enderecoEntregaCep = enderecoEntregaCep;
    }
    
    public String getEnderecoEntregaLogradouro() {
        return enderecoEntregaLogradouro;
    }
    
    public void setEnderecoEntregaLogradouro(String enderecoEntregaLogradouro) {
        this.enderecoEntregaLogradouro = enderecoEntregaLogradouro;
    }
    
    public String getEnderecoEntregaNumero() {
        return enderecoEntregaNumero;
    }
    
    public void setEnderecoEntregaNumero(String enderecoEntregaNumero) {
        this.enderecoEntregaNumero = enderecoEntregaNumero;
    }
    
    public String getEnderecoEntregaComplemento() {
        return enderecoEntregaComplemento;
    }
    
    public void setEnderecoEntregaComplemento(String enderecoEntregaComplemento) {
        this.enderecoEntregaComplemento = enderecoEntregaComplemento;
    }
    
    public String getEnderecoEntregaBairro() {
        return enderecoEntregaBairro;
    }
    
    public void setEnderecoEntregaBairro(String enderecoEntregaBairro) {
        this.enderecoEntregaBairro = enderecoEntregaBairro;
    }
    
    public String getEnderecoEntregaCidade() {
        return enderecoEntregaCidade;
    }
    
    public void setEnderecoEntregaCidade(String enderecoEntregaCidade) {
        this.enderecoEntregaCidade = enderecoEntregaCidade;
    }
    
    public String getEnderecoEntregaEstado() {
        return enderecoEntregaEstado;
    }
    
    public void setEnderecoEntregaEstado(String enderecoEntregaEstado) {
        this.enderecoEntregaEstado = enderecoEntregaEstado;
    }
    
    public String getTipoFrete() {
        return tipoFrete;
    }
    
    public void setTipoFrete(String tipoFrete) {
        this.tipoFrete = tipoFrete;
    }
    
    public String getNomeFrete() {
        return nomeFrete;
    }
    
    public void setNomeFrete(String nomeFrete) {
        this.nomeFrete = nomeFrete;
    }
    
    public String getPrazoEntrega() {
        return prazoEntrega;
    }
    
    public void setPrazoEntrega(String prazoEntrega) {
        this.prazoEntrega = prazoEntrega;
    }
    
    public List<ItemPedido> getItens() {
        return itens;
    }
    
    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }
}