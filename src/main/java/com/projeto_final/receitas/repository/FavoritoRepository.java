package com.projeto_final.receitas.repository;

import com.projeto_final.receitas.ChaveComposta.FavoritoId;
import com.projeto_final.receitas.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FavoritoRepository
        extends JpaRepository<Favorito, FavoritoId> {

    List<Favorito> findByUsuarioId(Long usuario_id);

    boolean existsByUsuarioIdAndReceitaId(
            Long usuario_id,
            Long receita_id);

    void deleteByUsuarioIdAndReceitaId(
            Long usuario_id,
            Long receita_id);
}
