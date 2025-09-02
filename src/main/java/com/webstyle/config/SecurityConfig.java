package com.webstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para permitir seus formulários
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll() // Permite acesso ao login e recursos estáticos
                .anyRequest().permitAll() // Permite acesso a todas as outras URLs
            )
            .formLogin(form -> form.disable()) // Desabilita o formulário de login padrão do Spring Security
            .httpBasic(basic -> basic.disable()) // Desabilita autenticação HTTP Basic
            .logout(logout -> logout.disable()); // Desabilita o logout padrão do Spring Security
        
        return http.build();
    }
}