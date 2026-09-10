package com.projeto_final.receitas.exception;

/** 403 - ha usuario logado, mas ele nao tem permissao para esta acao. */
public class forbiddenException extends RuntimeException {
    public forbiddenException(String mensagem) {
        super(mensagem);
    }
}
