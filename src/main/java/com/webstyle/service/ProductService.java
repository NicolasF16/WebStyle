package com.webstyle.service;

import com.webstyle.model.Product;
import com.webstyle.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    private static final int PRODUTOS_POR_PAGINA = 10;

    public Product cadastrarProduto(Product produto) {
        // Verifica se código já existe
        Optional<Product> existingProduct = productRepository.findByCodigo(produto.getCodigo());
        if (existingProduct.isPresent()) {
            throw new RuntimeException("Código do produto já existe no sistema");
        }
        
        // Valida campos obrigatórios
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome do produto é obrigatório");
        }
        
        if (produto.getPreco() == null || produto.getPreco().doubleValue() <= 0) {
            throw new RuntimeException("Preço deve ser maior que zero");
        }
        
        if (produto.getQuantidadeEstoque() == null || produto.getQuantidadeEstoque() < 0) {
            throw new RuntimeException("Quantidade em estoque não pode ser negativa");
        }
        
        // Define status como ATIVO por padrão
        if (produto.getStatus() == null) {
            produto.setStatus(Product.Status.ATIVO);
        }
        
        return productRepository.save(produto);
    }

    public Page<Product> listarProdutos(int pagina) {
        Pageable pageable = PageRequest.of(pagina, PRODUTOS_POR_PAGINA, 
                Sort.by(Sort.Direction.DESC, "dataCriacao"));
        return productRepository.findAllOrderByDataCriacaoDesc(pageable);
    }

    public Page<Product> buscarProdutos(String busca, int pagina) {
        Pageable pageable = PageRequest.of(pagina, PRODUTOS_POR_PAGINA, 
                Sort.by(Sort.Direction.DESC, "dataCriacao"));
        
        if (busca == null || busca.trim().isEmpty()) {
            return listarProdutos(pagina);
        }
        
        return productRepository.findByNomeContainingIgnoreCaseOrCodigoContainingIgnoreCase(
                busca.trim(), pageable);
    }

    public Product buscarPorId(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product buscarPorCodigo(String codigo) {
        return productRepository.findByCodigo(codigo).orElse(null);
    }

    public void alterarStatus(Long id, Product.Status status) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product produto = productOpt.get();
            produto.setStatus(status);
            productRepository.save(produto);
        } else {
            throw new RuntimeException("Produto não encontrado");
        }
    }

    public Product alterarProduto(Long id, Product produtoAlterado) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product produto = productOpt.get();
            
            // Verifica se o código foi alterado e se já existe outro produto com esse código
            if (!produto.getCodigo().equals(produtoAlterado.getCodigo())) {
                Optional<Product> existingProduct = productRepository.findByCodigo(produtoAlterado.getCodigo());
                if (existingProduct.isPresent()) {
                    throw new RuntimeException("Código do produto já existe no sistema");
                }
            }
            
            // Atualiza os campos
            produto.setCodigo(produtoAlterado.getCodigo());
            produto.setNome(produtoAlterado.getNome());
            produto.setDescricao(produtoAlterado.getDescricao());
            produto.setPreco(produtoAlterado.getPreco());
            produto.setQuantidadeEstoque(produtoAlterado.getQuantidadeEstoque());
            produto.setStatus(produtoAlterado.getStatus());
            
            return productRepository.save(produto);
        } else {
            throw new RuntimeException("Produto não encontrado");
        }
    }

    public void excluirProduto(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Produto não encontrado");
        }
    }
}