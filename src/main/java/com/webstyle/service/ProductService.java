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
import java.util.Arrays;
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
        // CORREÇÃO: Verifica se código já existe antes de validar outros campos
        if (produto.getCodigo() != null) {
            Optional<Product> existingProduct = productRepository.findByCodigo(produto.getCodigo());
            if (existingProduct.isPresent()) {
                throw new RuntimeException("Código '" + produto.getCodigo() + "' já existe no sistema");
            }
        }
        
        // Validações dos dados do produto
        validarDadosProduto(produto);
        
        // CORREÇÃO: Define valores padrão se não fornecidos
        if (produto.getStatus() == null) {
            produto.setStatus(Product.Status.ATIVO);
        }
        
        if (produto.getAvaliacao() == null) {
            produto.setAvaliacao(new BigDecimal("1.0"));
        }
        
        // Salva o produto primeiro
        Product produtoSalvo = productRepository.save(produto);
        
        // CORREÇÃO: Processa e salva as imagens apenas se foram fornecidas
        if (imagens != null && !imagens.isEmpty()) {
            // Filtra arquivos vazios
            List<MultipartFile> imagensValidas = imagens.stream()
                .filter(img -> img != null && !img.isEmpty())
                .toList();
                
            if (!imagensValidas.isEmpty()) {
                salvarImagensProduto(produtoSalvo, imagensValidas, imagemPrincipalIndex);
            }
        }
        
        return produtoSalvo;
    }

    public void alterarEstoque(Long id, Integer novaQuantidade) {
        if (id == null) {
            throw new RuntimeException("ID do produto é obrigatório");
        }
        
        if (novaQuantidade == null || novaQuantidade < 0) {
            throw new RuntimeException("Quantidade deve ser um número não negativo");
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
        
        Product produto = productOpt.get();
        produto.setQuantidadeEstoque(novaQuantidade);
        productRepository.save(produto);
    }

    // CORREÇÃO: Validação mais robusta dos dados do produto
    private void validarDadosProduto(Product produto) {
        // Validação do código
        if (produto.getCodigo() == null || produto.getCodigo().trim().isEmpty()) {
            throw new RuntimeException("Código do produto é obrigatório");
        }
        
        if (produto.getCodigo().trim().length() > 50) {
            throw new RuntimeException("Código do produto deve ter no máximo 50 caracteres");
        }
        
        // Remove espaços extras do código
        produto.setCodigo(produto.getCodigo().trim().toUpperCase());
        
        // Validação do nome
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome do produto é obrigatório");
        }
        
        if (produto.getNome().trim().length() > 200) {
            throw new RuntimeException("Nome do produto deve ter no máximo 200 caracteres");
        }
        
        // Remove espaços extras do nome
        produto.setNome(produto.getNome().trim());
        
        // Validação da descrição
        if (produto.getDescricao() != null) {
            if (produto.getDescricao().length() > 2000) {
                throw new RuntimeException("Descrição deve ter no máximo 2000 caracteres");
            }
            // Remove espaços extras da descrição se não estiver vazia
            if (!produto.getDescricao().trim().isEmpty()) {
                produto.setDescricao(produto.getDescricao().trim());
            } else {
                produto.setDescricao(null);
            }
        }
        
        // Validação do preço
        if (produto.getPreco() == null) {
            throw new RuntimeException("Preço é obrigatório");
        }
        
        if (produto.getPreco().compareTo(new BigDecimal("0.01")) < 0) {
            throw new RuntimeException("Preço deve ser maior que R$ 0,00");
        }
        
        // Validação da quantidade em estoque
        if (produto.getQuantidadeEstoque() == null) {
            throw new RuntimeException("Quantidade em estoque é obrigatória");
        }
        
        if (produto.getQuantidadeEstoque() < 0) {
            throw new RuntimeException("Quantidade em estoque não pode ser negativa");
        }
        
        // Validação da avaliação
        if (produto.getAvaliacao() != null) {
            BigDecimal avaliacao = produto.getAvaliacao();
            if (avaliacao.compareTo(new BigDecimal("1.0")) < 0 || 
                avaliacao.compareTo(new BigDecimal("5.0")) > 0) {
                throw new RuntimeException("Avaliação deve estar entre 1.0 e 5.0");
            }
            
            // CORREÇÃO: Verifica se é múltipla de 0.5 de forma mais robusta
            BigDecimal multiplicado = avaliacao.multiply(new BigDecimal("2"));
            if (multiplicado.stripTrailingZeros().scale() > 0) {
                throw new RuntimeException("Avaliação deve variar de 0.5 em 0.5 (ex: 1.0, 1.5, 2.0, etc.)");
            }
        }
        
        // Validação do status
        if (produto.getStatus() == null) {
            produto.setStatus(Product.Status.ATIVO);
        }
    }

    // CORREÇÃO: Melhor tratamento de imagens
    private void salvarImagensProduto(Product produto, List<MultipartFile> imagens, Long imagemPrincipalIndex) throws IOException {
        int index = 0;
        boolean temImagemPrincipal = false;
        
        for (MultipartFile arquivo : imagens) {
            if (arquivo != null && !arquivo.isEmpty()) {
                try {
                    // Salva o arquivo fisicamente
                    String caminhoArquivo = fileUploadService.salvarArquivo(arquivo);
                    
                    // Cria o registro da imagem
                    ProductImage imagem = new ProductImage();
                    imagem.setNomeOriginal(arquivo.getOriginalFilename());
                    imagem.setNomeArquivo(caminhoArquivo.substring(caminhoArquivo.lastIndexOf("/") + 1));
                    imagem.setCaminhoArquivo(caminhoArquivo);
                    imagem.setProduto(produto);
                    
                    // CORREÇÃO: Define se é a imagem principal de forma mais robusta
                    boolean isPrincipal = false;
                    if (imagemPrincipalIndex != null) {
                        isPrincipal = imagemPrincipalIndex.equals((long) index);
                    } else if (index == 0) {
                        // Se não foi especificada, a primeira é a principal
                        isPrincipal = true;
                    }
                    
                    if (isPrincipal) {
                        imagem.setImagemPrincipal(true);
                        temImagemPrincipal = true;
                    }
                    
                    produto.addImagem(imagem);
                    productImageRepository.save(imagem);
                    index++;
                    
                } catch (IOException e) {
                    // Em caso de erro, tenta continuar com as outras imagens
                    System.err.println("Erro ao salvar imagem " + arquivo.getOriginalFilename() + ": " + e.getMessage());
                    throw e; // Re-lança para tratar no controller
                }
            }
        }
        
        // CORREÇÃO: Garante que sempre há uma imagem principal se há imagens
        if (!temImagemPrincipal && !produto.getImagens().isEmpty()) {
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
        if (id == null) {
            return null;
        }
        return productRepository.findById(id).orElse(null);
    }

    public Product buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return productRepository.findByCodigo(codigo.trim().toUpperCase()).orElse(null);
    }

    public void alterarStatus(Long id, Product.Status status) {
        if (id == null || status == null) {
            throw new RuntimeException("ID do produto e status são obrigatórios");
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product produto = productOpt.get();
            produto.setStatus(status);
            productRepository.save(produto);
        } else {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
    }

    // CORREÇÃO: Melhor tratamento de alteração de produto
    public Product alterarProduto(Long id, Product produtoAlterado, List<MultipartFile> novasImagens, 
                                 Long imagemPrincipalIndex, List<Long> imagensParaRemover) throws IOException {
        
        if (id == null) {
            throw new RuntimeException("ID do produto é obrigatório");
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
        
        Product produto = productOpt.get();
        
        // Verifica se o código foi alterado e se já existe outro produto com esse código
        if (produtoAlterado.getCodigo() != null && 
            !produto.getCodigo().equals(produtoAlterado.getCodigo().trim().toUpperCase())) {
            
            Optional<Product> existingProduct = productRepository.findByCodigo(produtoAlterado.getCodigo().trim().toUpperCase());
            if (existingProduct.isPresent()) {
                throw new RuntimeException("Código '" + produtoAlterado.getCodigo() + "' já existe no sistema");
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
        if (imagensParaRemover != null && !imagensParaRemover.isEmpty()) {
            for (Long imagemId : imagensParaRemover) {
                try {
                    removerImagem(produto, imagemId);
                } catch (Exception e) {
                    System.err.println("Erro ao remover imagem ID " + imagemId + ": " + e.getMessage());
                    // Continua removendo as outras imagens
                }
            }
        }
        
        // Adiciona novas imagens
        if (novasImagens != null && !novasImagens.isEmpty()) {
            List<MultipartFile> imagensValidas = novasImagens.stream()
                .filter(img -> img != null && !img.isEmpty())
                .toList();
                
            if (!imagensValidas.isEmpty()) {
                salvarImagensProduto(produto, imagensValidas, imagemPrincipalIndex);
            }
        }
        
        return productRepository.save(produto);
    }

    private void removerImagem(Product produto, Long imagemId) {
        if (imagemId == null) {
            return;
        }
        
        ProductImage imagem = productImageRepository.findById(imagemId).orElse(null);
        if (imagem != null && imagem.getProduto().getId().equals(produto.getId())) {
            // Remove o arquivo físico
            try {
                fileUploadService.deletarArquivo(imagem.getCaminhoArquivo());
            } catch (Exception e) {
                System.err.println("Erro ao deletar arquivo físico: " + e.getMessage());
            }
            
            // Remove da lista do produto
            produto.removeImagem(imagem);
            
            // Remove do banco
            productImageRepository.delete(imagem);
        }
    }

    public void definirImagemPrincipal(Long produtoId, Long imagemId) {
        if (produtoId == null || imagemId == null) {
            throw new RuntimeException("ID do produto e ID da imagem são obrigatórios");
        }
        
        Product produto = buscarPorId(produtoId);
        if (produto == null) {
            throw new RuntimeException("Produto não encontrado com ID: " + produtoId);
        }
        
        // Remove marcação principal de todas as imagens do produto
        produto.getImagens().forEach(img -> {
            img.setImagemPrincipal(false);
            productImageRepository.save(img);
        });
        
        // Marca a nova imagem como principal
        ProductImage novaImagemPrincipal = productImageRepository.findById(imagemId).orElse(null);
        if (novaImagemPrincipal != null && novaImagemPrincipal.getProduto().getId().equals(produtoId)) {
            novaImagemPrincipal.setImagemPrincipal(true);
            productImageRepository.save(novaImagemPrincipal);
        } else {
            throw new RuntimeException("Imagem não encontrada ou não pertence ao produto especificado");
        }
    }

    public List<ProductImage> buscarImagensProduto(Long produtoId) {
        if (produtoId == null) {
            return List.of();
        }
        return productImageRepository.findByProdutoId(produtoId);
    }

    public void excluirProduto(Long id) {
        if (id == null) {
            throw new RuntimeException("ID do produto é obrigatório");
        }
        
        Product produto = buscarPorId(id);
        if (produto != null) {
            // Remove todas as imagens físicas
            produto.getImagens().forEach(imagem -> {
                try {
                    fileUploadService.deletarArquivo(imagem.getCaminhoArquivo());
                } catch (Exception e) {
                    System.err.println("Erro ao deletar arquivo: " + e.getMessage());
                }
            });
            
            // Remove do banco (cascade remove as imagens)
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
    }

    // ===== NOVO MÉTODO - Sprint 3 =====
    /**
     * Lista apenas produtos ATIVOS para exibição pública (Sprint 3)
     * Usado na página home.html
     * 
     * @return Lista de produtos com status ATIVO, ordenados por data de criação (mais recentes primeiro)
     */
    public List<Product> listarProdutosAtivos() {
        return productRepository.findByStatusOrderByDataCriacaoDesc(Product.Status.ATIVO);
    }
}