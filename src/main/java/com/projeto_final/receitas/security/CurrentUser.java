package com.projeto_final.receitas.security;

import com.projeto_final.receitas.exception.forbiddenException;
import com.projeto_final.receitas.exception.unauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Atalho para os controllers perguntarem quem esta chamando.
 */
@Component
public class CurrentUser {

    private final HttpServletRequest request;

    public CurrentUser(HttpServletRequest request) {
        this.request = request;
    }

    /** Usuario logado ou null quando a chamada e anonima. */
    public AuthenticatedUser get() {
        return (AuthenticatedUser) request.getAttribute(JwtAuthFilter.ATTRIBUTE);
    }

    /** Exige login. */
    public AuthenticatedUser require() {
        AuthenticatedUser user = get();

        if (user == null) {
            throw new unauthorizedException(
                    "Faca login para continuar.");
        }

        return user;
    }

    /** Exige login E perfil de administrador. */
    public AuthenticatedUser requireAdmin() {
        AuthenticatedUser user = require();

        if (!user.isAdmin()) {
            throw new forbiddenException(
                    "Apenas administradores podem gerenciar receitas.");
        }

        return user;
    }
}
