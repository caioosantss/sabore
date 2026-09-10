package com.projeto_final.receitas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Le o cabecalho Authorization e, quando o token e valido, guarda o usuario
 * como atributo da requisicao. Nao bloqueia ninguem: rotas publicas seguem
 * funcionando sem token e quem exige login usa o CurrentUser.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATTRIBUTE = "authenticatedUser";

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(PREFIX)) {
            AuthenticatedUser user = jwtService.parse(header.substring(PREFIX.length()).trim());

            if (user != null) {
                request.setAttribute(ATTRIBUTE, user);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // O preflight do CORS nunca carrega Authorization; deixar passar direto.
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
