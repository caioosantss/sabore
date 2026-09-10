package com.projeto_final.receitas.controller;

import com.projeto_final.receitas.dto.RecipeResponse;
import com.projeto_final.receitas.entity.Receita;
import com.projeto_final.receitas.security.AuthenticatedUser;
import com.projeto_final.receitas.security.CurrentUser;
import com.projeto_final.receitas.service.FavoritoService;
import com.projeto_final.receitas.service.receitasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * Leitura e livre; criar, editar e excluir exigem perfil de administrador.
 */
@RestController
@RequestMapping("/recipes")
public class receitaController {

    private final receitasService service;
    private final FavoritoService favoritoService;
    private final CurrentUser currentUser;

    public receitaController(
            receitasService service,
            FavoritoService favoritoService,
            CurrentUser currentUser) {
        this.service = service;
        this.favoritoService = favoritoService;
        this.currentUser = currentUser;
    }

    // ---------------------------------------------------------------- leitura

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAll(
            @RequestParam(value = "filter", required = false) String filter) {

        AuthenticatedUser usuario = currentUser.get();
        Long usuarioId = usuario == null ? null : usuario.id();

        Set<Long> favoritas = favoritoService.idsFavoritosDoUsuario(usuarioId);

        List<RecipeResponse> receitas = service.listar(filter, usuarioId)
                .stream()
                .map(receita -> RecipeResponse.from(receita, favoritas))
                .toList();

        return ResponseEntity.ok(receitas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getById(@PathVariable Long id) {

        AuthenticatedUser usuario = currentUser.get();
        Long usuarioId = usuario == null ? null : usuario.id();

        Set<Long> favoritas = favoritoService.idsFavoritosDoUsuario(usuarioId);

        return ResponseEntity.ok(RecipeResponse.from(service.getById(id), favoritas));
    }

    // ------------------------------------------------- escrita (somente admin)

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> create(
            @RequestParam("nome") String nome,
            @RequestParam("desc") String desc,
            @RequestParam("tempo") Integer tempo,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem) {

        currentUser.requireAdmin();

        Receita receita = service.create(nome, desc, tempo, categoria, imagem);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RecipeResponse.from(receita));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> update(
            @PathVariable Long id,
            @RequestParam("nome") String nome,
            @RequestParam("desc") String desc,
            @RequestParam("tempo") Integer tempo,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem) {

        currentUser.requireAdmin();

        Receita receita = service.update(id, nome, desc, tempo, categoria, imagem);

        return ResponseEntity.ok(RecipeResponse.from(receita));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        currentUser.requireAdmin();

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
