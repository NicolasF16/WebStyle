package com.webstyle.service;

import com.webstyle.model.User;
import com.webstyle.model.User.Status;
import com.webstyle.model.User.TipoUsuario;
import com.webstyle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User cadastrarUsuario(User user) {
        // Verifica se email já existe
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email já cadastrado no sistema");
        }
        
        // Valida se senhas são iguais 
        if (user.getSenha() == null || user.getSenha().trim().isEmpty()) {
            throw new RuntimeException("Senha é obrigatória");
        }
        
        // Criptografa a senha
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        
        // Define status como ATIVO por padrão
        user.setStatus(Status.ATIVO);
        
        // Define tipo como EXTERNO por padrão se não especificado
        if (user.getTipo() == null) {
            user.setTipo(TipoUsuario.EXTERNO);
        }
        
        return userRepository.save(user);
    }

    public Optional<User> autenticar(String email, String senha) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Agora permite tanto BACKOFFICE quanto EXTERNO
            if ((user.getTipo() == TipoUsuario.BACKOFFICE || user.getTipo() == TipoUsuario.EXTERNO) &&
                user.getStatus() == Status.ATIVO &&
                passwordEncoder.matches(senha, user.getSenha())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public void alterarStatus(Long id, Status status) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Usuário não encontrado");
        }
    }

    public User buscarPorId(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User alterarUsuario(Long id, User usuarioAlterado) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Permite alterar apenas nome, CPF e grupo (não email)
            user.setNome(usuarioAlterado.getNome());
            user.setCpf(usuarioAlterado.getCpf());
            
            // Só altera grupo se especificado
            if (usuarioAlterado.getTipo() != null) {
                user.setTipo(usuarioAlterado.getTipo());
            }
            
            // Só altera senha se foi fornecida nova senha
            if (usuarioAlterado.getSenha() != null && !usuarioAlterado.getSenha().trim().isEmpty()) {
                // Valida se as duas senhas são iguais 
                user.setSenha(passwordEncoder.encode(usuarioAlterado.getSenha()));
            }
            
            return userRepository.save(user);
        } else {
            throw new RuntimeException("Usuário não encontrado");
        }
    }

    public void validarSenhas(String senha1, String senha2) {
        if (senha1 == null || senha2 == null || !senha1.equals(senha2)) {
            throw new RuntimeException("As senhas não coincidem");
        }
    }
}