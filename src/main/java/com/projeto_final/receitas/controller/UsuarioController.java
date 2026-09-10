package com.projeto_final.receitas.controller;

import com.projeto_final.receitas.dto.LoginResponse;
import com.projeto_final.receitas.dto.UserResponse;
import com.projeto_final.receitas.entity.Usuario;
import com.projeto_final.receitas.exception.forbiddenException;
import com.projeto_final.receitas.security.AuthenticatedUser;
import com.projeto_final.receitas.security.CurrentUser;
import com.projeto_final.receitas.security.JwtService;
import com.projeto_final.receitas.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UsuarioController {

    private final UsuarioService service;
    private final JwtService jwtService;
    private final CurrentUser currentUser;

    public UsuarioController(
            UsuarioService service,
            JwtService jwtService,
            CurrentUser currentUser) {
        this.service = service;
        this.jwtService = jwtService;
        this.currentUser = currentUser;
    }

    // -------------------------------------------------------------- publico

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@RequestBody Usuario obj) {

        Usuario criado = service.create(obj);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(criado, service.papelDe(criado)));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody Usuario obj) {

        Usuario usuario = service.login(obj.getEmail(), obj.getPassword());
        String papel = service.papelDe(usuario);

        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), papel);

        return ResponseEntity.ok(
                new LoginResponse(token, UserResponse.from(usuario, papel)));
    }

    /** Permite ao front revalidar a sessao guardada no localStorage. */
    @GetMapping("/auth/me")
    public ResponseEntity<UserResponse> me() {

        AuthenticatedUser autenticado = currentUser.require();
        Usuario usuario = service.getId(autenticado.id());

        return ResponseEntity.ok(UserResponse.from(usuario, service.papelDe(usuario)));
    }

    // ----------------------------------------------- gestao (restrita)

    @GetMapping("/usuarios")
    public ResponseEntity<List<UserResponse>> getAll() {

        currentUser.requireAdmin();

        List<UserResponse> usuarios = service.getAll().stream()
                .map(usuario -> UserResponse.from(usuario, service.papelDe(usuario)))
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UserResponse> getId(@PathVariable Long id) {

        exigirDonoOuAdmin(id);

        Usuario usuario = service.getId(id);

        return ResponseEntity.ok(UserResponse.from(usuario, service.papelDe(usuario)));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id, @RequestBody Usuario obj) {

        exigirDonoOuAdmin(id);

        obj.setId(id);
        Usuario usuario = service.update(obj);

        return ResponseEntity.ok(UserResponse.from(usuario, service.papelDe(usuario)));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        exigirDonoOuAdmin(id);

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    /** Cada um mexe na propria conta; administrador mexe em qualquer uma. */
    private void exigirDonoOuAdmin(Long id) {

        AuthenticatedUser autenticado = currentUser.require();

        if (!autenticado.isAdmin() && !autenticado.id().equals(id)) {
            throw new forbiddenException("Você só pode acessar a sua própria conta.");
        }
    }
}
