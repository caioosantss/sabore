package com.projeto_final.receitas.exception;

/** 401 - nao ha usuario logado (ou o token expirou). */
public class unauthorizedException extends RuntimeException {
    public unauthorizedException(String mensagem) {
        super(mensagem);
    }
}
