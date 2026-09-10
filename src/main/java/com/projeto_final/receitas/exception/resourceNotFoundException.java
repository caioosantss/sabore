package com.projeto_final.receitas.exception;

public class resourceNotFoundException extends RuntimeException {
    public resourceNotFoundException ( String mensagem) {
        super(mensagem);
    }
}
