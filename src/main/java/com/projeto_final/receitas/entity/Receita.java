package com.projeto_final.receitas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "receita")
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", unique = true, nullable = false)
    private String nome;

    @Column(name = "descricao", nullable = false, length = 1000)
    private String desc;

    @Column(name = "tempo", nullable = false)
    private Integer tempo;

    @Column(name = "url")
    private String img;

    /**
     * "doce" ou "salgada". Alimenta os filtros da barra lateral do front.
     * Aceita nulo porque receitas antigas foram cadastradas sem categoria.
     */
    @Column(name = "categoria")
    private String categoria;

    // @JsonIgnore evita a recursao infinita Receita -> Favorito -> Receita
    // caso a entidade acabe sendo serializada direto, sem passar pelo DTO.
    @JsonIgnore
    @OneToMany(mappedBy = "receita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorito> favoritadaPor = new ArrayList<>();

    public Receita() {}

    public List<Favorito> getFavoritadaPor() {
        return favoritadaPor;
    }

    public void setFavoritadaPor(List<Favorito> favoritadaPor) {
        this.favoritadaPor = favoritadaPor;
    }
}
