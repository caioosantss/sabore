package com.projeto_final.receitas.service;

import com.projeto_final.receitas.entity.Favorito;
import com.projeto_final.receitas.entity.Receita;
import com.projeto_final.receitas.entity.Usuario;
import com.projeto_final.receitas.exception.businessException;
import com.projeto_final.receitas.exception.resourceNotFoundException;
import com.projeto_final.receitas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final receitaRepository receitaRepository;

    public FavoritoService(
            FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            receitaRepository receitaRepository) {

        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
    }

    @Transactional
    public Favorito favoritar(Long usuarioId, Long receitaId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new resourceNotFoundException(
                        "Usuário não encontrado com ID: " + usuarioId));

        Receita receita = receitaRepository.findById(receitaId)
                .orElseThrow(() -> new resourceNotFoundException(
                        "Receita não encontrada com o ID: " + receitaId));

        if (favoritoRepository.existsByUsuarioIdAndReceitaId(usuarioId, receitaId)) {
            throw new businessException("Esta receita já está nos seus favoritos.");
        }

        return favoritoRepository.save(new Favorito(usuario, receita));
    }

    @Transactional
    public void desfavoritar(Long usuarioId, Long receitaId) {

        if (!favoritoRepository.existsByUsuarioIdAndReceitaId(usuarioId, receitaId)) {
            throw new resourceNotFoundException("Esta receita não está nos seus favoritos.");
        }

        favoritoRepository.deleteByUsuarioIdAndReceitaId(usuarioId, receitaId);
    }

    @Transactional(readOnly = true)
    public List<Favorito> listarFavoritoDoUsuario(Long usuarioId) {

        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new resourceNotFoundException(
                        "Usuário não encontrado com ID: " + usuarioId));

        return favoritoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * IDs das receitas favoritadas, usado para marcar o coracao na listagem.
     * Usuario anonimo (id nulo) simplesmente nao tem favoritos.
     */
    @Transactional(readOnly = true)
    public Set<Long> idsFavoritosDoUsuario(Long usuarioId) {

        if (usuarioId == null) {
            return Set.of();
        }

        return favoritoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(favorito -> favorito.getReceita().getId())
                .collect(Collectors.toSet());
    }
}
