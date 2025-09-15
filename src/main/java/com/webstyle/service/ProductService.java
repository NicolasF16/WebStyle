package com.webstyle.service;

import com.webstyle.model.Product;
import com.webstyle.model.ProductImage;
import com.webstyle.repository.ProductRepository;
import com.webstyle.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    private static final int PRODUTOS_POR_PAGINA = 10;

    public Product cadastrarProduto(Product produto, List<MultipartFile> imagens, Long imagemPrincipalIndex) throws IOException {
        // Verifica se código já existe
        Optional<Product> existingProduct = productRepository.findByCodigo(produto.getCodigo());
        if (existingProduct.isPresent()) {
            throw new RuntimeException("Código do produto já existe no sistema");
        }
        
        // Validações básicas do produto
        validarDadosProduto(produto);
        
        // Salva o produto primeiro
        Product produtoSalvo = productRepository.save(produto);
        
        // Processa e salva as imagens
        if (imagens != null && !imagens.isEmpty()) {
            salvarImagensProduto(produtoSalvo, imagens, imagemPrincipalIndex);
        }
        
        return produtoSalvo;
    }

    private void validarDadosProduto(Product produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome do produto é obrigatório");
        }
        
        if (produto.getNome().length() > 200) {
            throw new RuntimeException("Nome do produto deve ter no máximo 200 caracteres");
        }
        
        if (produto.getDescricao() != null && produto.getDescricao().length() > 2000) {
            throw new RuntimeException("Descrição deve ter no máximo 2000 caracteres");
        }
        
        if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Preço deve ser maior que zero");
        }
        
        if (produto.getQuantidadeEstoque() == null || produto.getQuantidadeEstoque() < 0) {
            throw new RuntimeException("Quantidade em estoque não pode ser negativa");
        }
        
        if (produto.getAvaliacao() == null) {
            produto.setAvaliacao(new BigDecimal("1.0"));
        } else {
            // Valida se a avaliação está entre 1.0 e 5.0 e é múltipla de 0.5
            BigDecimal avaliacao = produto.getAvaliacao();
            if (avaliacao.compareTo(new BigDecimal("1.0")) < 0 || 
                avaliacao.compareTo(new BigDecimal("5.0")) > 0) {
                throw new RuntimeException("Avaliação deve estar entre 1.0 e 5.0");
            }
            
            // Verifica se é múltipla de 0.5
            BigDecimal resto = avaliacao.remainder(new BigDecimal("0.5"));
            if (resto.compareTo(BigDecimal.ZERO) != 0) {
                throw new RuntimeException("Avaliação deve variar de 0.5 em 0.5");
            }
        }
        
        // Define status como ATIVO por padrão
        if (produto.getStatus() == null) {
            produto.setStatus(Product.Status.ATIVO);
        }
    }

    private void salvarImagensProduto(Product produto, List<MultipartFile> imagens, Long imagemPrincipalIndex) throws IOException {
        int index = 0;
        for (MultipartFile arquivo : imagens) {
            if (!arquivo.isEmpty()) {
                // Salva o arquivo fisicamente
                String caminhoArquivo = fileUploadService.salvarArquivo(arquivo);
                
                // Cria o registro da imagem
                ProductImage imagem = new ProductImage();
                imagem.setNomeOriginal(arquivo.getOriginalFilename());
                imagem.setNomeArquivo(caminhoArquivo.substring(caminhoArquivo.lastIndexOf("/") + 1));
                imagem.setCaminhoArquivo(caminhoArquivo);
                imagem.setProduto(produto);
                
                // Define se é a imagem principal
                if (imagemPrincipalIndex != null && imagemPrincipalIndex.equals((long) index)) {
                    imagem.setImagemPrincipal(true);
                }
                
                produto.addImagem(imagem);
                productImageRepository.save(imagem);
                index++;
            }
        }
        
        // Se nenhuma imagem foi marcada como principal, marca a primeira
        if (imagemPrincipalIndex == null && !produto.getImagens().isEmpty()) {
            produto.getImagens().get(0).setImagemPrincipal(true);
            productImageRepository.save(produto.getImagens().get(0));
        }
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

    public Product alterarProduto(Long id, Product produtoAlterado, List<MultipartFile> novasImagens, 
                                 Long imagemPrincipalIndex, List<Long> imagensParaRemover) throws IOException {
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
            
            // Valida dados do produto alterado
            validarDadosProduto(produtoAlterado);
            
            // Atualiza os campos do produto
            produto.setCodigo(produtoAlterado.getCodigo());
            produto.setNome(produtoAlterado.getNome());
            produto.setDescricao(produtoAlterado.getDescricao());
            produto.setPreco(produtoAlterado.getPreco());
            produto.setQuantidadeEstoque(produtoAlterado.getQuantidadeEstoque());
            produto.setAvaliacao(produtoAlterado.getAvaliacao());
            produto.setStatus(produtoAlterado.getStatus());
            
            // Remove imagens marcadas para remoção
            if (imagensParaRemover != null) {
                for (Long imagemId : imagensParaRemover) {
                    removerImagem(produto, imagemId);
                }
            }
            
            // Adiciona novas imagens
            if (novasImagens != null && !novasImagens.isEmpty()) {
                salvarImagensProduto(produto, novasImagens, imagemPrincipalIndex);
            }
            
            return productRepository.save(produto);
        } else {
            throw new RuntimeException("Produto não encontrado");
        }
    }

    private void removerImagem(Product produto, Long imagemId) {
        ProductImage imagem = productImageRepository.findById(imagemId).orElse(null);
        if (imagem != null && imagem.getProduto().getId().equals(produto.getId())) {
            // Remove o arquivo físico
            fileUploadService.deletarArquivo(imagem.getCaminhoArquivo());
            
            // Remove da lista do produto
            produto.removeImagem(imagem);
            
            // Remove do banco
            productImageRepository.delete(imagem);
        }
    }

    public void definirImagemPrincipal(Long produtoId, Long imagemId) {
        Product produto = buscarPorId(produtoId);
        if (produto != null) {
            // Remove marcação principal de todas as imagens
            produto.getImagens().forEach(img -> {
                img.setImagemPrincipal(false);
                productImageRepository.save(img);
            });
            
            // Marca a nova imagem como principal
            ProductImage novaImagemPrincipal = productImageRepository.findById(imagemId).orElse(null);
            if (novaImagemPrincipal != null && novaImagemPrincipal.getProduto().getId().equals(produtoId)) {
                novaImagemPrincipal.setImagemPrincipal(true);
                productImageRepository.save(novaImagemPrincipal);
            }
        }
    }

    public List<ProductImage> buscarImagensProduto(Long produtoId) {
        return productImageRepository.findByProdutoId(produtoId);
    }

    public void excluirProduto(Long id) {
        Product produto = buscarPorId(id);
        if (produto != null) {
            // Remove todas as imagens físicas
            produto.getImagens().forEach(imagem -> 
                fileUploadService.deletarArquivo(imagem.getCaminhoArquivo())
            );
            
            // Remove do banco (cascade remove as imagens)
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Produto não encontrado");
        }
    }
}