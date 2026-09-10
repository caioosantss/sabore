package com.projeto_final.receitas.dto;

/**
 * Resposta do login. O papel vai junto para o front saber se mostra
 * os botoes de gerenciar receitas.
 */
public record LoginResponse(String token, UserResponse user) {
}
