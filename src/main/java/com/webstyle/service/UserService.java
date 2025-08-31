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
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        user.setStatus(Status.ATIVO);
        user.setTipo(TipoUsuario.BACKOFFICE); // padrão para exemplo
        return userRepository.save(user);
    }

    public Optional<User> autenticar(String email, String senha) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Verifica se é usuário BACKOFFICE, está ativo e senha confere
            if (user.getTipo() == TipoUsuario.BACKOFFICE &&
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
        }
    }
}
