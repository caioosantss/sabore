package com.projeto_final.receitas.dto;

import com.projeto_final.receitas.entity.Usuario;

/** Usuario sem a senha, para nunca vazar hash nem texto puro. */
public record UserResponse(Long id, String name, String email, String role) {

    public static UserResponse from(Usuario usuario, String role) {
        return new UserResponse(usuario.getId(), usuario.getName(), usuario.getEmail(), role);
    }
}
