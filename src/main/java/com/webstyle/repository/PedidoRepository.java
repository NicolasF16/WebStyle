package com.webstyle.repository;

import com.webstyle.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);
    
    @Query("SELECT MAX(p.id) FROM Pedido p")
    Long findMaxId();
}