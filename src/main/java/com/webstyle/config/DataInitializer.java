package com.webstyle.config;

import com.webstyle.model.User;
import com.webstyle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
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
    }
}