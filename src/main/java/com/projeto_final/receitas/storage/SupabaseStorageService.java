package com.projeto_final.receitas.storage;

import com.projeto_final.receitas.exception.businessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Envia a foto da receita para o Storage do Supabase e devolve a URL publica.
 *
 * Usa o HttpClient que ja vem no Java, entao nao precisa de biblioteca extra.
 * A API de Storage do Supabase e um PUT/POST simples:
 *
 *   POST {SUPABASE_URL}/storage/v1/object/{bucket}/{arquivo}
 *   Authorization: Bearer {SUPABASE_SERVICE_KEY}
 *
 * e a URL publica (bucket publico) fica em:
 *
 *   {SUPABASE_URL}/storage/v1/object/public/{bucket}/{arquivo}
 */
@Service
public class SupabaseStorageService {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024; // 5 MB

    private static final List<String> TIPOS_ACEITOS = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final String url;
    private final String serviceKey;
    private final String bucket;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public SupabaseStorageService(
            @Value("${app.supabase.url:}") String url,
            @Value("${app.supabase.service-key:}") String serviceKey,
            @Value("${app.supabase.bucket:receitas}") String bucket) {

        this.url = url == null ? "" : url.replaceAll("/+$", "");
        this.serviceKey = serviceKey == null ? "" : serviceKey.trim();
        this.bucket = bucket;
    }

    public boolean isConfigurado() {
        return !url.isBlank() && !serviceKey.isBlank();
    }

    /**
     * Faz o upload e devolve a URL publica da imagem.
     * Devolve null quando nenhum arquivo foi enviado (imagem e opcional).
     */
    public String upload(MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }

        validar(arquivo);

        if (!isConfigurado()) {
            throw new businessException(
                    "O envio de imagens não está configurado. Defina SUPABASE_URL "
                            + "e SUPABASE_SERVICE_KEY nas variáveis de ambiente.");
        }

        String nomeArquivo = gerarNome(arquivo.getOriginalFilename());
        String destino = url + "/storage/v1/object/" + bucket + "/" + nomeArquivo;

        try {
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(destino))
                    .timeout(Duration.ofSeconds(30))
                    // Os dois headers de proposito, para aceitar os dois
                    // formatos de chave do Supabase:
                    //   - service_role (legada, um JWT "eyJ...") -> Authorization
                    //   - sb_secret_... (nova)                   -> apikey
                    // A rota Authorization tenta decodificar a chave como JWT e
                    // devolve "Invalid Compact JWS" para o formato novo; o
                    // header apikey entende os dois.
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", arquivo.getContentType())
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(arquivo.getBytes()))
                    .build();

            HttpResponse<String> resposta =
                    http.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() >= 200 && resposta.statusCode() < 300) {
                return url + "/storage/v1/object/public/" + bucket + "/" + nomeArquivo;
            }

            // Repassa o motivo que o proprio Supabase devolveu. Sem isso a
            // mensagem virava adivinhacao: "Bucket not found" e
            // "Invalid Compact JWS" (chave do tipo errado) chegavam os dois
            // como um HTTP 400 generico.
            throw new businessException(
                    "O Supabase recusou o envio da imagem (HTTP "
                            + resposta.statusCode() + "): " + resumo(resposta.body())
                            + " — confira SUPABASE_BUCKET (o bucket '" + bucket
                            + "' precisa existir e ser público) e SUPABASE_SERVICE_KEY"
                            + " (precisa ser a chave secreta, não a publishable).");

        } catch (IOException e) {
            throw new businessException(
                    "Não foi possível enviar a imagem para o Supabase: " + e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new businessException("O envio da imagem foi interrompido.");
        }
    }

    /** Corpo de erro do Supabase, encurtado para caber na mensagem. */
    private String resumo(String corpo) {

        if (corpo == null || corpo.isBlank()) {
            return "(sem detalhes)";
        }

        String limpo = corpo.replaceAll("\\s+", " ").trim();

        return limpo.length() > 300 ? limpo.substring(0, 300) + "..." : limpo;
    }

    private void validar(MultipartFile arquivo) {

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new businessException("A imagem deve ter no máximo 5 MB.");
        }

        String tipo = arquivo.getContentType();

        if (tipo == null || !TIPOS_ACEITOS.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new businessException(
                    "Formato de imagem inválido. Use JPG, PNG, WEBP ou GIF.");
        }
    }

    /** Nome unico para nao sobrescrever a foto de outra receita. */
    private String gerarNome(String nomeOriginal) {

        String extensao = "";

        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal
                    .substring(nomeOriginal.lastIndexOf('.'))
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9.]", "");
        }

        return UUID.randomUUID() + extensao;
    }
}
