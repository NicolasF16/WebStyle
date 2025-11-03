package com.webstyle.service;

import com.webstyle.model.*;
import com.webstyle.repository.EnderecoRepository;
import com.webstyle.repository.PedidoRepository;
import com.webstyle.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PedidoService {
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private EnderecoRepository enderecoRepository;
    
    /**
     * Cria um novo pedido
     */
    public Pedido criarPedido(
            Cliente cliente,
            List<CartItem> itensCarrinho,
            Long enderecoId,
            BigDecimal valorSubtotal,
            BigDecimal valorFrete,
            String tipoFrete,
            String nomeFrete,
            String prazoEntrega,
            String formaPagamento,
            Integer numeroParcelas) {
        
        // Validações
        if (cliente == null) {
            throw new RuntimeException("Cliente não informado");
        }
        
        if (itensCarrinho == null || itensCarrinho.isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }
        
        if (enderecoId == null) {
            throw new RuntimeException("Endereço de entrega não informado");
        }
        
        // Busca o endereço
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
        
        // Verifica se o endereço pertence ao cliente
        if (!endereco.getCliente().getId().equals(cliente.getId())) {
            throw new RuntimeException("Endereço não pertence ao cliente");
        }
        
        // Cria o pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(gerarNumeroPedido());
        pedido.setCliente(cliente);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setValorSubtotal(valorSubtotal);
        pedido.setValorFrete(valorFrete);
        pedido.setValorTotal(valorSubtotal.add(valorFrete));
        pedido.setStatus(Pedido.StatusPedido.AGUARDANDO_PAGAMENTO);
        
        // Forma de pagamento
        if ("cartao".equals(formaPagamento)) {
            pedido.setFormaPagamento(Pedido.FormaPagamento.CARTAO_CREDITO);
            pedido.setNumeroParcelas(numeroParcelas);
        } else {
            pedido.setFormaPagamento(Pedido.FormaPagamento.BOLETO);
            pedido.setNumeroParcelas(1);
        }
        
        // Snapshot do endereço de entrega
        pedido.setEnderecoEntregaCep(endereco.getCep());
        pedido.setEnderecoEntregaLogradouro(endereco.getLogradouro());
        pedido.setEnderecoEntregaNumero(endereco.getNumero());
        pedido.setEnderecoEntregaComplemento(endereco.getComplemento());
        pedido.setEnderecoEntregaBairro(endereco.getBairro());
        pedido.setEnderecoEntregaCidade(endereco.getCidade());
        pedido.setEnderecoEntregaEstado(endereco.getEstado());
        
        // Informações do frete
        pedido.setTipoFrete(tipoFrete);
        pedido.setNomeFrete(nomeFrete);
        pedido.setPrazoEntrega(prazoEntrega);
        
        // Adiciona os itens
        for (CartItem cartItem : itensCarrinho) {
            Product produto = productRepository.findById(cartItem.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + cartItem.getProdutoId()));
            
            // Verifica estoque
            if (produto.getQuantidadeEstoque() < cartItem.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }
            
            // Cria o item do pedido
            ItemPedido item = new ItemPedido(produto, cartItem.getQuantidade());
            pedido.addItem(item);
            
            // Atualiza o estoque
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - cartItem.getQuantidade());
            productRepository.save(produto);
        }
        
        // Salva o pedido
        return pedidoRepository.save(pedido);
    }
    
    /**
     * Gera número único do pedido no formato: ANO + MÊS + SEQUENCIAL (ex: 202411000001)
     */
    private String gerarNumeroPedido() {
        LocalDateTime agora = LocalDateTime.now();
        String anoMes = agora.format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        Long maxId = pedidoRepository.findMaxId();
        Long proximoId = (maxId == null) ? 1L : maxId + 1;
        
        String sequencial = String.format("%06d", proximoId);
        
        return anoMes + sequencial;
    }
    
    /**
     * Busca pedido por número
     */
    public Pedido buscarPorNumero(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido).orElse(null);
    }
    
    /**
     * Busca pedido por ID
     */
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }
    
    /**
     * Busca pedido por ID carregando todos os itens (EAGER)
     * Este método força o carregamento dos itens antes de fechar a transação
     */
    public Pedido buscarPorIdComItens(Long id) {
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        
        if (pedido != null) {
            // Força o carregamento dos itens do pedido
            pedido.getItens().size();
            
            // Também força o carregamento do cliente
            pedido.getCliente().getNomeCompleto();
        }
        
        return pedido;
    }
    
    /**
     * Lista pedidos do cliente
     */
    public List<Pedido> listarPedidosCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
    }
    
    /**
     * Atualiza status do pedido
     */
    public void atualizarStatus(Long pedidoId, Pedido.StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        pedido.setStatus(novoStatus);
        pedidoRepository.save(pedido);
    }
}
