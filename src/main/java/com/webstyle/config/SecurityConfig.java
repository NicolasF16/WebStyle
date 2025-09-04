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
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF
            .headers(headers -> headers.frameOptions().disable()) // Permite frames para H2 Console
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/style.css").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Permite acesso ao H2 Console
                .anyRequest().permitAll() // Permite acesso a todas as outras URLs
            )
            .formLogin(form -> form.disable()) // Desabilita o formulário de login padrão
            .httpBasic(basic -> basic.disable()) // Desabilita autenticação HTTP Basic
            .logout(logout -> logout.disable()); // Desabilita o logout padrão
        
        return http.build();
    }
}