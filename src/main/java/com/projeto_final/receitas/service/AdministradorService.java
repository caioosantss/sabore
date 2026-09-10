package com.projeto_final.receitas.service;

import java.util.List;
import com.projeto_final.receitas.entity.Administrador;
import com.projeto_final.receitas.exception.businessException;
import com.projeto_final.receitas.exception.resourceNotFoundException;
import com.projeto_final.receitas.repository.AdministradorRepository;
import com.projeto_final.receitas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AdministradorService {

    private final AdministradorRepository repository;
    private final UsuarioRepository usuarioRepository;

    public AdministradorService(
            AdministradorRepository repository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public Administrador create(Administrador obj) {

        if (obj.getName() == null || obj.getName().trim().isEmpty()) {
            throw new businessException("O nome é obrigatório.");
        }

        if (obj.getEmail() == null || !obj.getEmail().contains("@")) {
            throw new businessException("Informe um e-mail válido.");
        }

        if (obj.getPassword() == null || obj.getPassword().length() < 6) {
            throw new businessException("A senha precisa ter pelo menos 6 caracteres.");
        }

        String email = obj.getEmail().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new businessException("Este e-mail já está cadastrado.");
        }

        obj.setId(null);
        obj.setName(obj.getName().trim());
        obj.setEmail(email);

        return repository.save(obj);
    }

    public void delete(Long id) {

        repository.findById(id)
                .orElseThrow(() ->
                        new resourceNotFoundException(
                                "Administrador não encontrado com ID: " + id));

        repository.deleteById(id);
    }

    public Administrador getId(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new resourceNotFoundException(
                                "Administrador não encontrado com ID: " + id));
    }

    public List<Administrador> getAll() {
        return repository.findAll();
    }

    public Administrador update(Administrador obj) {

        Administrador administrador = repository.findById(obj.getId())
                .orElseThrow(() ->
                        new resourceNotFoundException(
                                "Administrador não encontrado com ID: " + obj.getId()));

        updateAdministrador(administrador, obj);

        return repository.save(administrador);
    }

    private void updateAdministrador(
            Administrador administrador,
            Administrador obj) {

        administrador.setName(obj.getName());
        administrador.setToken(obj.getToken());
        administrador.setEmail(obj.getEmail());
        administrador.setPassword(obj.getPassword());
    }
}
