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
        
        // Cria alguns produtos de exemplo se não existirem
        if (productRepository.count() == 0) {
            Product produto1 = new Product();
            produto1.setCodigo("SMART001");
            produto1.setNome("Smartphone Galaxy A54");
            produto1.setDescricao("Smartphone Samsung Galaxy A54 128GB 6GB RAM Câmera Tripla");
            produto1.setPreco(new BigDecimal("1299.99"));
            produto1.setQuantidadeEstoque(25);
            produto1.setStatus(Product.Status.ATIVO);
            productRepository.save(produto1);

            Product produto2 = new Product();
            produto2.setCodigo("NOTE001");
            produto2.setNome("Notebook Dell Inspiron 15");
            produto2.setDescricao("Notebook Dell Inspiron 15 3000 Intel Core i5 8GB 256GB SSD");
            produto2.setPreco(new BigDecimal("2899.90"));
            produto2.setQuantidadeEstoque(10);
            produto2.setStatus(Product.Status.ATIVO);
            productRepository.save(produto2);

            Product produto3 = new Product();
            produto3.setCodigo("MOUSE001");
            produto3.setNome("Mouse Gamer Logitech");
            produto3.setDescricao("Mouse Gamer Logitech G203 RGB 6 Botões 8000 DPI");
            produto3.setPreco(new BigDecimal("129.99"));
            produto3.setQuantidadeEstoque(50);
            produto3.setStatus(Product.Status.ATIVO);
            productRepository.save(produto3);

            Product produto4 = new Product();
            produto4.setCodigo("TABLET001");
            produto4.setNome("Tablet Samsung Galaxy Tab A8");
            produto4.setDescricao("Tablet Samsung Galaxy Tab A8 64GB 4GB RAM Tela 10.5");
            produto4.setPreco(new BigDecimal("899.99"));
            produto4.setQuantidadeEstoque(0);
            produto4.setStatus(Product.Status.INATIVO);
            productRepository.save(produto4);

            Product produto5 = new Product();
            produto5.setCodigo("SMART002");
            produto5.setNome("Smartwatch Apple Watch SE");
            produto5.setDescricao("Apple Watch SE GPS 44mm Caixa de Alumínio Prata");
            produto5.setPreco(new BigDecimal("1899.00"));
            produto5.setQuantidadeEstoque(15);
            produto5.setStatus(Product.Status.ATIVO);
            productRepository.save(produto5);

            Product produto6 = new Product();
            produto6.setCodigo("HEAD001");
            produto6.setNome("Headset Gamer HyperX");
            produto6.setDescricao("Headset Gamer HyperX Cloud Stinger Core 7.1 Surround");
            produto6.setPreco(new BigDecimal("299.99"));
            produto6.setQuantidadeEstoque(30);
            produto6.setStatus(Product.Status.ATIVO);
            productRepository.save(produto6);

            Product produto7 = new Product();
            produto7.setCodigo("CAM001");
            produto7.setNome("Câmera Canon EOS Rebel T7");
            produto7.setDescricao("Câmera Canon EOS Rebel T7 24.1MP Kit com Lente 18-55mm");
            produto7.setPreco(new BigDecimal("2299.99"));
            produto7.setQuantidadeEstoque(5);
            produto7.setStatus(Product.Status.ATIVO);
            productRepository.save(produto7);

            Product produto8 = new Product();
            produto8.setCodigo("SMART003");
            produto8.setNome("Smart TV LG 55");
            produto8.setDescricao("Smart TV LG 55 4K UHD ThinQ AI HDR Ativo DTS Virtual");
            produto8.setPreco(new BigDecimal("2199.90"));
            produto8.setQuantidadeEstoque(8);
            produto8.setStatus(Product.Status.ATIVO);
            productRepository.save(produto8);

            Product produto9 = new Product();
            produto9.setCodigo("GAME001");
            produto9.setNome("Console PlayStation 5");
            produto9.setDescricao("Console Sony PlayStation 5 825GB SSD 4K HDR");
            produto9.setPreco(new BigDecimal("4299.99"));
            produto9.setQuantidadeEstoque(3);
            produto9.setStatus(Product.Status.ATIVO);
            productRepository.save(produto9);

            Product produto10 = new Product();
            produto10.setCodigo("AIR001");
            produto10.setNome("AirPods Pro Apple");
            produto10.setDescricao("Apple AirPods Pro 2ª Geração com Cancelamento Ativo de Ruído");
            produto10.setPreco(new BigDecimal("1699.00"));
            produto10.setQuantidadeEstoque(20);
            produto10.setStatus(Product.Status.ATIVO);
            productRepository.save(produto10);

            Product produto11 = new Product();
            produto11.setCodigo("KEY001");
            produto11.setNome("Teclado Mecânico Corsair");
            produto11.setDescricao("Teclado Mecânico Gamer Corsair K70 RGB MK.2 Cherry MX");
            produto11.setPreco(new BigDecimal("599.99"));
            produto11.setQuantidadeEstoque(12);
            produto11.setStatus(Product.Status.ATIVO);
            productRepository.save(produto11);

            Product produto12 = new Product();
            produto12.setCodigo("PRINT001");
            produto12.setNome("Impressora HP DeskJet");
            produto12.setDescricao("Impressora Multifuncional HP DeskJet 2774 Jato de Tinta Wi-Fi");
            produto12.setPreco(new BigDecimal("399.99"));
            produto12.setQuantidadeEstoque(18);
            produto12.setStatus(Product.Status.ATIVO);
            productRepository.save(produto12);

            System.out.println("12 produtos de exemplo criados no sistema");
        }
    }
}