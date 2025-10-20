package com.webstyle.repository;

import com.webstyle.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    
    List<Endereco> findByClienteId(Long clienteId);
    
    List<Endereco> findByClienteIdAndFaturamento(Long clienteId, boolean faturamento);
}