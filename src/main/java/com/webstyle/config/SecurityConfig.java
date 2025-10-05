package com.webstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de Segurança - Sprint 3
 * Spring Security DESABILITADO - usando autenticação manual via HttpSession
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF (necessário para formulários funcionarem)
            .csrf(csrf -> csrf.disable())
            
            // Permite frames (necessário para H2 Console)
            .headers(headers -> headers.frameOptions().disable())
            
            // LIBERA TODAS AS ROTAS - autenticação é feita manualmente nos controllers
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            
            // Desabilita o formulário de login padrão do Spring Security
            .formLogin(form -> form.disable())
            
            // Desabilita autenticação HTTP Basic
            .httpBasic(basic -> basic.disable())
            
            // Desabilita logout padrão
            .logout(logout -> logout.disable());
        
        return http.build();
    }
}