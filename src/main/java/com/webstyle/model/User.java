package com.webstyle.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String cpf;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha; 

    @Enumerated(EnumType.STRING)
    private Status status; // ATIVO ou INATIVO

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo; // BACKOFFICE 

    public enum Status {
        ATIVO, INATIVO
    }

    public enum TipoUsuario {
        BACKOFFICE, EXTERNO
    }

    
    public User() {}

   
    public User(String nome, String cpf, String email, String senha, TipoUsuario tipo) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.status = Status.ATIVO; 
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public TipoUsuario getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    // Método para verificar se é administrador
    public boolean isAdmin() {
        return this.tipo == TipoUsuario.BACKOFFICE;
    }

    // Método para verificar se está ativo
    public boolean isAtivo() {
        return this.status == Status.ATIVO;
    }
}