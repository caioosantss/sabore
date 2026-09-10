package com.projeto_final.receitas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Converte qualquer excecao em um JSON com o mesmo formato (standardError),
 * para o front sempre conseguir ler "message" e mostrar algo util.
 */
@RestControllerAdvice
public class globalExceptionHandler {

    private ResponseEntity<standardError> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {

        standardError err = new standardError(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(resourceNotFoundException.class)
    public ResponseEntity<standardError> resourceNotFound(
            resourceNotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", e.getMessage(), request);
    }

    @ExceptionHandler(businessException.class)
    public ResponseEntity<standardError> businessError(
            businessException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Regra de negócio violada", e.getMessage(), request);
    }

    @ExceptionHandler(unauthorizedException.class)
    public ResponseEntity<standardError> unauthorized(
            unauthorizedException e, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Não autenticado", e.getMessage(), request);
    }

    @ExceptionHandler(forbiddenException.class)
    public ResponseEntity<standardError> forbidden(
            forbiddenException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage(), request);
    }

    /** Campos anotados com @NotBlank, @Min etc. que nao passaram. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<standardError> validation(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        String detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes, request);
    }

    /** Faltou um campo do formulario (ex.: "nome" nao veio no multipart). */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<standardError> missingParameter(
            Exception e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage(), request);
    }

    /** Ex.: /recipes/abc quando o id e numerico. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<standardError> typeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "O valor '" + e.getValue() + "' não é válido para o parâmetro '" + e.getName() + "'.",
                request);
    }

    /** JSON malformado no corpo da requisicao. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<standardError> unreadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "O corpo da requisição não pôde ser lido. Verifique o JSON enviado.", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<standardError> uploadTooLarge(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande",
                "A imagem enviada excede o tamanho máximo permitido.", request);
    }

    /** Viola unique/foreign key no banco (ex.: receita com nome repetido). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<standardError> dataIntegrity(
            DataIntegrityViolationException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflito de dados",
                "Já existe um registro com esses dados ou ele está em uso.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<standardError> methodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                e.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<standardError> noResource(
            NoResourceFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Rota não encontrada",
                "Nenhum recurso mapeado para " + request.getRequestURI(), request);
    }

    /**
     * Rede de seguranca: qualquer erro nao previsto vira 500 com mensagem
     * generica. O detalhe real fica no log do servidor, nao na resposta.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<standardError> unexpected(
            Exception e, HttpServletRequest request) {

        System.err.println("[ERRO NAO TRATADO] " + request.getRequestURI());
        e.printStackTrace();

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Algo deu errado no servidor. Tente novamente em instantes.", request);
    }
}
