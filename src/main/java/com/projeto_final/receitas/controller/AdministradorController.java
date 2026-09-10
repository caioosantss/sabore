package com.projeto_final.receitas.controller;

import com.projeto_final.receitas.dto.UserResponse;
import com.projeto_final.receitas.entity.Administrador;
import com.projeto_final.receitas.security.AuthenticatedUser;
import com.projeto_final.receitas.security.CurrentUser;
import com.projeto_final.receitas.service.AdministradorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Todas as rotas exigem que quem chama ja seja administrador — caso contrario
 * qualquer pessoa poderia se promover. O primeiro admin nasce do seeder
 * (AdminSeeder), a partir de variaveis de ambiente.
 */
@RestController
@RequestMapping("/administrador")
public class AdministradorController {

    private final AdministradorService service;
    private final CurrentUser currentUser;

    public AdministradorController(AdministradorService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody Administrador obj) {

        currentUser.requireAdmin();

        Administrador criado = service.create(obj);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(criado, AuthenticatedUser.ROLE_ADMIN));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {

        currentUser.requireAdmin();

        List<UserResponse> admins = service.getAll().stream()
                .map(admin -> UserResponse.from(admin, AuthenticatedUser.ROLE_ADMIN))
                .toList();

        return ResponseEntity.ok(admins);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getId(@PathVariable Long id) {

        currentUser.requireAdmin();

        return ResponseEntity.ok(
                UserResponse.from(service.getId(id), AuthenticatedUser.ROLE_ADMIN));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id, @RequestBody Administrador obj) {

        currentUser.requireAdmin();

        obj.setId(id);

        return ResponseEntity.ok(
                UserResponse.from(service.update(obj), AuthenticatedUser.ROLE_ADMIN));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        currentUser.requireAdmin();

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
