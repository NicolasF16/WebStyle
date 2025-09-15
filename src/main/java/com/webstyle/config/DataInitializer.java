package com.webstyle.config;

import com.webstyle.model.User;
import com.webstyle.model.Product;
import com.webstyle.repository.UserRepository;
import com.webstyle.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe o administrador padrão
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setNome("Administrador");
            admin.setCpf("00000000000");
            admin.setEmail("admin@gmail.com");
            admin.setSenha(passwordEncoder.encode("Admin123"));
            admin.setTipo(User.TipoUsuario.BACKOFFICE);
            admin.setStatus(User.Status.ATIVO);
            
            userRepository.save(admin);
            System.out.println("Administrador padrão criado: admin@gmail.com / Admin123");
        }

        // Verifica se já existe o estoquista padrão
        if (userRepository.findByEmail("estoquista@gmail.com").isEmpty()) {
            User estoquista = new User();
            estoquista.setNome("Estoquista");
            estoquista.setCpf("11111111111");
            estoquista.setEmail("estoquista@gmail.com");
            estoquista.setSenha(passwordEncoder.encode("Estoque123"));
            estoquista.setTipo(User.TipoUsuario.EXTERNO);
            estoquista.setStatus(User.Status.ATIVO);
            
            userRepository.save(estoquista);
            System.out.println("Estoquista padrão criado: estoquista@gmail.com / Estoque123");
        }

        // Cria produtos de roupas de exemplo se não existirem
        if (productRepository.count() == 0) {
            // Roupas Femininas
            Product produto1 = new Product();
            produto1.setCodigo("VEST001");
            produto1.setNome("Vestido Floral Midi");
            produto1.setDescricao("Vestido midi com estampa floral delicada, ideal para ocasiões especiais. Tecido fluido e confortável, com modelagem que valoriza a silhueta feminina. Possui forro interno e fechamento lateral com zíper invisível.");
            produto1.setPreco(new BigDecimal("159.90"));
            produto1.setQuantidadeEstoque(25);
            produto1.setAvaliacao(new BigDecimal("4.5"));
            produto1.setStatus(Product.Status.ATIVO);
            productRepository.save(produto1);

            Product produto2 = new Product();
            produto2.setCodigo("BLUSA001");
            produto2.setNome("Blusa Manga Longa Básica");
            produto2.setDescricao("Blusa básica de manga longa em algodão 100%. Corte reto e confortável, perfeita para o dia a dia. Disponível em várias cores. Ideal para compor looks casuais ou profissionais.");
            produto2.setPreco(new BigDecimal("79.90"));
            produto2.setQuantidadeEstoque(50);
            produto2.setAvaliacao(new BigDecimal("4.0"));
            produto2.setStatus(Product.Status.ATIVO);
            productRepository.save(produto2);

            Product produto3 = new Product();
            produto3.setCodigo("CALCA001");
            produto3.setNome("Calça Jeans Skinny Feminina");
            produto3.setDescricao("Calça jeans skinny de cintura alta, confeccionada em denim de alta qualidade. Modelagem que valoriza as curvas e proporciona conforto durante todo o dia. Possui elastano na composição para maior flexibilidade.");
            produto3.setPreco(new BigDecimal("129.90"));
            produto3.setQuantidadeEstoque(35);
            produto3.setAvaliacao(new BigDecimal("4.5"));
            produto3.setStatus(Product.Status.ATIVO);
            productRepository.save(produto3);

            // Roupas Masculinas
            Product produto4 = new Product();
            produto4.setCodigo("CAMI001");
            produto4.setNome("Camisa Social Masculina");
            produto4.setDescricao("Camisa social masculina em algodão premium, corte slim fit. Ideal para ambientes corporativos e eventos sociais. Possui colarinho italiano e punhos com abotoadura dupla. Fácil de passar e manter.");
            produto4.setPreco(new BigDecimal("189.90"));
            produto4.setQuantidadeEstoque(20);
            produto4.setAvaliacao(new BigDecimal("4.0"));
            produto4.setStatus(Product.Status.ATIVO);
            productRepository.save(produto4);

            Product produto5 = new Product();
            produto5.setCodigo("POLO001");
            produto5.setNome("Polo Masculina Premium");
            produto5.setDescricao("Camisa polo masculina em piquet de algodão egípcio. Modelagem confortável com gola e punhos em ribana. Possui bordado discreto no peito. Ideal para ocasiões casuais e esporte fino.");
            produto5.setPreco(new BigDecimal("119.90"));
            produto5.setQuantidadeEstoque(40);
            produto5.setAvaliacao(new BigDecimal("4.5"));
            produto5.setStatus(Product.Status.ATIVO);
            productRepository.save(produto5);

            Product produto6 = new Product();
            produto6.setCodigo("BERMUDA001");
            produto6.setNome("Bermuda Jeans Masculina");
            produto6.setDescricao("Bermuda jeans masculina com lavação moderna. Modelagem regular fit com cinco bolsos tradicionais. Confeccionada em denim resistente com acabamento de qualidade. Perfeita para o verão.");
            produto6.setPreco(new BigDecimal("89.90"));
            produto6.setQuantidadeEstoque(30);
            produto6.setAvaliacao(new BigDecimal("3.5"));
            produto6.setStatus(Product.Status.ATIVO);
            productRepository.save(produto6);

            // Acessórios e Calçados
            Product produto7 = new Product();
            produto7.setCodigo("TENIS001");
            produto7.setNome("Tênis Casual Unissex");
            produto7.setDescricao("Tênis casual unissex em couro sintético com solado de borracha. Design moderno e versátil, combina com diversos estilos. Palmilha anatômica para maior conforto. Disponível em várias cores.");
            produto7.setPreco(new BigDecimal("199.90"));
            produto7.setQuantidadeEstoque(15);
            produto7.setAvaliacao(new BigDecimal("4.0"));
            produto7.setStatus(Product.Status.ATIVO);
            productRepository.save(produto7);

            Product produto8 = new Product();
            produto8.setCodigo("BOLSA001");
            produto8.setNome("Bolsa Feminina Transversal");
            produto8.setDescricao("Bolsa feminina transversal em couro sintético de alta qualidade. Design minimalista e elegante, possui compartimentos internos organizadores. Alça ajustável e removível. Ideal para o dia a dia.");
            produto8.setPreco(new BigDecimal("149.90"));
            produto8.setQuantidadeEstoque(25);
            produto8.setAvaliacao(new BigDecimal("4.5"));
            produto8.setStatus(Product.Status.ATIVO);
            productRepository.save(produto8);

            Product produto9 = new Product();
            produto9.setCodigo("CINTO001");
            produto9.setNome("Cinto de Couro Masculino");
            produto9.setDescricao("Cinto masculino em couro legítimo com fivela em metal escovado. Largura de 3,5cm, ideal para uso social e casual. Acabamento impecável e durabilidade garantida. Disponível em preto e marrom.");
            produto9.setPreco(new BigDecimal("79.90"));
            produto9.setQuantidadeEstoque(45);
            produto9.setAvaliacao(new BigDecimal("4.0"));
            produto9.setStatus(Product.Status.ATIVO);
            productRepository.save(produto9);

            // Roupas Infantis
            Product produto10 = new Product();
            produto10.setCodigo("CONJ001");
            produto10.setNome("Conjunto Infantil Menino");
            produto10.setDescricao("Conjunto infantil composto por camiseta e bermuda em malha de algodão. Estampa divertida e cores vibrantes. Confortável e prático para brincadeiras. Tamanhos de 2 a 10 anos.");
            produto10.setPreco(new BigDecimal("69.90"));
            produto10.setQuantidadeEstoque(35);
            produto10.setAvaliacao(new BigDecimal("4.5"));
            produto10.setStatus(Product.Status.ATIVO);
            productRepository.save(produto10);

            Product produto11 = new Product();
            produto11.setCodigo("SAIA001");
            produto11.setNome("Saia Rodada Infantil");
            produto11.setDescricao("Saia rodada infantil em tecido de algodão com elastano. Estampa floral delicada e cores suaves. Cós alto com elástico para maior conforto. Ideal para ocasiões especiais e festas.");
            produto11.setPreco(new BigDecimal("59.90"));
            produto11.setQuantidadeEstoque(28);
            produto11.setAvaliacao(new BigDecimal("4.0"));
            produto11.setStatus(Product.Status.ATIVO);
            productRepository.save(produto11);

            // Produtos com estoque zerado (inativos)
            Product produto12 = new Product();
            produto12.setCodigo("JAQUETA001");
            produto12.setNome("Jaqueta Jeans Feminina");
            produto12.setDescricao("Jaqueta jeans feminina oversized com lavação desgastada. Possui bolsos frontais e fechamento com botões metálicos. Modelagem confortável e versátil para diversas combinações. Tecido de alta qualidade.");
            produto12.setPreco(new BigDecimal("179.90"));
            produto12.setQuantidadeEstoque(0);
            produto12.setAvaliacao(new BigDecimal("3.5"));
            produto12.setStatus(Product.Status.INATIVO);
            productRepository.save(produto12);

            System.out.println("12 produtos de roupas de exemplo criados no sistema");
        }
    }
}