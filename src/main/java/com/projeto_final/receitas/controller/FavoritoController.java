package com.projeto_final.receitas.controller;

import com.projeto_final.receitas.dto.RecipeResponse;
import com.projeto_final.receitas.security.AuthenticatedUser;
import com.projeto_final.receitas.security.CurrentUser;
import com.projeto_final.receitas.service.FavoritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Favoritos sao sempre da conta logada: o usuario vem do JWT, nunca da URL.
 * Assim ninguem consegue mexer na lista de outra pessoa trocando o id.
 */
@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final CurrentUser currentUser;

    public FavoritoController(FavoritoService favoritoService, CurrentUser currentUser) {
        this.favoritoService = favoritoService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> listar() {

        AuthenticatedUser usuario = currentUser.require();

        List<RecipeResponse> favoritas = favoritoService
                .listarFavoritoDoUsuario(usuario.id())
                .stream()
                .map(favorito -> RecipeResponse.from(favorito.getReceita(), true))
                .toList();

        return ResponseEntity.ok(favoritas);
    }

    @PostMapping("/{receitaId}")
    public ResponseEntity<RecipeResponse> favoritar(@PathVariable Long receitaId) {

        AuthenticatedUser usuario = currentUser.require();

        var favorito = favoritoService.favoritar(usuario.id(), receitaId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RecipeResponse.from(favorito.getReceita(), true));
    }

    @DeleteMapping("/{receitaId}")
    public ResponseEntity<Void> desfavoritar(@PathVariable Long receitaId) {

        AuthenticatedUser usuario = currentUser.require();

        favoritoService.desfavoritar(usuario.id(), receitaId);

        return ResponseEntity.noContent().build();
    }
}
