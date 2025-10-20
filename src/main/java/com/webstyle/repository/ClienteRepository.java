package com.webstyle.repository;

import com.webstyle.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByCpf(String cpf);
    
    boolean existsByEmail(String email);
    
    boolean existsByCpf(String cpf);
}