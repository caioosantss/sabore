package com.projeto_final.receitas.repository;

import com.projeto_final.receitas.entity.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface receitaRepository extends JpaRepository<Receita, Long> {

    Optional<Receita> findByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    List<Receita> findByCategoriaIgnoreCaseOrderByNomeAsc(String categoria);

    List<Receita> findByTempoLessThanEqualOrderByTempoAsc(Integer tempo);

    /** Receitas favoritadas por um usuario especifico. */
    @Query("""
            select f.receita from Favorito f
            where f.usuario.id = :usuarioId
            order by f.dataAdicionado desc
            """)
    List<Receita> findFavoritasDoUsuario(Long usuarioId);

    /** "Queridinhos": as mais favoritadas primeiro. */
    @Query("""
            select r from Receita r
            left join r.favoritadaPor f
            group by r
            order by count(f) desc, r.nome asc
            """)
    List<Receita> findMaisFavoritadas();
}
