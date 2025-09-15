package com.webstyle.repository;

import com.webstyle.model.ProductImage;
import com.webstyle.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProdutoId(Long produtoId);
    
    List<ProductImage> findByProdutoOrderByImagemPrincipalDescDataUploadAsc(Product produto);
    
    Optional<ProductImage> findByProdutoIdAndImagemPrincipalTrue(Long produtoId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.produto.id = :produtoId AND pi.imagemPrincipal = true")
    Optional<ProductImage> findImagemPrincipalByProdutoId(@Param("produtoId") Long produtoId);
    
    void deleteByProdutoId(Long produtoId);
    
    long countByProdutoId(Long produtoId);
}