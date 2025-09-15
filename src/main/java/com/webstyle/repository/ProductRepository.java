package com.webstyle.repository;

import com.webstyle.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByCodigo(String codigo);
    
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :busca, '%'))")
    Page<Product> findByNomeContainingIgnoreCaseOrCodigoContainingIgnoreCase(
            @Param("busca") String busca, Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.dataCriacao DESC")
    Page<Product> findAllOrderByDataCriacaoDesc(Pageable pageable);
}