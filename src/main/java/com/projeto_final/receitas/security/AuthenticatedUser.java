package com.projeto_final.receitas.security;

/**
 * Usuario extraido do JWT da requisicao atual.
 */
public record AuthenticatedUser(Long id, String email, String role) {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}
