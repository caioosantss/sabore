package com.projeto_final.receitas.dto;

import com.projeto_final.receitas.entity.Receita;

import java.util.Set;

/**
 * Traduz a entidade Receita para os nomes de campo que o front consome
 * (types/recipe.ts). Serve tambem para nao serializar a lista
 * favoritadaPor, que causaria recursao infinita Receita -> Favorito.
 */
public record RecipeResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        Integer prepTime,
        String category,
        boolean favorite
) {
    public static RecipeResponse from(Receita receita, boolean favorita) {
        return new RecipeResponse(
                receita.getId(),
                receita.getNome(),
                receita.getDesc(),
                receita.getImg(),
                receita.getTempo(),
                rotulo(receita.getCategoria()),
                favorita);
    }

    /** Sem usuario logado nada aparece como favorito. */
    public static RecipeResponse from(Receita receita) {
        return from(receita, false);
    }

    public static RecipeResponse from(Receita receita, Set<Long> favoritas) {
        return from(receita, favoritas.contains(receita.getId()));
    }

    private static String rotulo(String categoria) {
        if (categoria == null) return null;

        return switch (categoria) {
            case "doce" -> "Doce";
            case "salgada" -> "Salgada";
            default -> categoria;
        };
    }
}
