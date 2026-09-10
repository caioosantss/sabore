package com.projeto_final.receitas.repository;

import com.projeto_final.receitas.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    /**
     * Promove um usuario que ja existe a administrador.
     *
     * Administrador herda de Usuario com estrategia JOINED: as duas tabelas
     * compartilham o mesmo id, e ser admin e apenas ter a linha em
     * tb_administrador. Por isso basta o insert — e por isso a conta mantem
     * id, senha e favoritos.
     *
     * E SQL nativo porque o JPA nao troca a classe de uma entidade ja
     * persistida; pela API de objetos seria preciso apagar e recriar o
     * usuario, o que derrubaria os favoritos dele.
     */
    @Modifying
    @Query(value = "insert into tb_administrador (id) values (:id)", nativeQuery = true)
    void promover(@Param("id") Long id);
}
