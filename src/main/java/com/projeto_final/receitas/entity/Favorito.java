package com.projeto_final.receitas.entity;

import com.projeto_final.receitas.ChaveComposta.FavoritoId;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_favorito")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Favorito implements Serializable {

    @EmbeddedId
    private FavoritoId id;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @MapsId("receitaId")
    @JoinColumn(name = "receita_id")
    private Receita receita;

    @Column(name = "data_adicionado")
    private LocalDateTime dataAdicionado;

    public Favorito(Usuario usuario, Receita receita) {
        this.usuario = usuario;
        this.receita = receita;
        this.id = new FavoritoId(usuario.getId(), receita.getId());
        this.dataAdicionado = LocalDateTime.now();
    }
}
