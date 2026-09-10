package com.projeto_final.receitas.service;

import com.projeto_final.receitas.entity.Usuario;
import com.projeto_final.receitas.exception.businessException;
import com.projeto_final.receitas.exception.resourceNotFoundException;
import com.projeto_final.receitas.exception.unauthorizedException;
import com.projeto_final.receitas.repository.AdministradorRepository;
import com.projeto_final.receitas.repository.UsuarioRepository;
import com.projeto_final.receitas.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final AdministradorRepository administradorRepository;

    public UsuarioService(
            UsuarioRepository repository,
            AdministradorRepository administradorRepository) {
        this.repository = repository;
        this.administradorRepository = administradorRepository;
    }

    @Transactional
    public Usuario create(Usuario obj) {

        validar(obj);

        String email = obj.getEmail().trim().toLowerCase(Locale.ROOT);

        if (repository.findByEmail(email).isPresent()) {
            throw new businessException("Este e-mail já está cadastrado.");
        }

        obj.setId(null);
        obj.setName(obj.getName().trim());
        obj.setEmail(email);

        return repository.save(obj);
    }

    @Transactional(readOnly = true)
    public Usuario login(String email, String senha) {

        if (email == null || senha == null) {
            throw new unauthorizedException("Informe e-mail e senha.");
        }

        Optional<Usuario> usuario =
                repository.findByEmail(email.trim().toLowerCase(Locale.ROOT));

        if (usuario.isPresent() && senha.equals(usuario.get().getPassword())) {
            return usuario.get();
        }

        // Mensagem unica de proposito: nao revela se o e-mail existe.
        throw new unauthorizedException("E-mail ou senha incorretos.");
    }

    /**
     * Administrador herda de Usuario (JOINED), entao existir na tabela de
     * administradores com o mesmo id ja define o papel.
     */
    @Transactional(readOnly = true)
    public String papelDe(Usuario usuario) {
        return administradorRepository.existsById(usuario.getId())
                ? AuthenticatedUser.ROLE_ADMIN
                : AuthenticatedUser.ROLE_USER;
    }

    @Transactional
    public void delete(Long id) {
        getId(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Usuario getId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new resourceNotFoundException(
                        "Usuário não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Usuario> getAll() {
        return repository.findAll();
    }

    @Transactional
    public Usuario update(Usuario obj) {

        Usuario usuario = getId(obj.getId());

        if (obj.getName() != null && !obj.getName().isBlank()) {
            usuario.setName(obj.getName().trim());
        }

        return repository.save(usuario);
    }

    private void validar(Usuario obj) {

        if (obj.getName() == null || obj.getName().trim().isEmpty()) {
            throw new businessException("O nome é obrigatório.");
        }

        if (obj.getEmail() == null || !obj.getEmail().contains("@")) {
            throw new businessException("Informe um e-mail válido.");
        }

        if (obj.getPassword() == null || obj.getPassword().length() < 6) {
            throw new businessException("A senha precisa ter pelo menos 6 caracteres.");
        }
    }
}
