package com.projeto_final.receitas.ChaveComposta;

import jakarta.persistence.Embeddable;

import java.io.*;
import java.util.Objects;

@Embeddable
public class FavoritoId implements Serializable {
    private Long usuarioId;
    private Long receitaId;

    public FavoritoId() {}

    public FavoritoId(Long usuarioId, Long receitaId) {
        this.usuarioId = usuarioId;
        this.receitaId = receitaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getReceitaId() {
        return receitaId;
    }

    public void setReceitaId(Long receitaId) {
        this.receitaId = receitaId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FavoritoId that = (FavoritoId) obj;
        return Objects.equals(usuarioId, that.usuarioId) &&
                Objects.equals(receitaId, that.receitaId);
    }

    @Override
    public int hashCode() {
        return  Objects.hash(usuarioId, receitaId);
    }

}
