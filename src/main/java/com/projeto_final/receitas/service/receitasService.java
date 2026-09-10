package com.projeto_final.receitas.service;

import com.projeto_final.receitas.entity.Receita;
import com.projeto_final.receitas.exception.businessException;
import com.projeto_final.receitas.exception.resourceNotFoundException;
import com.projeto_final.receitas.repository.receitaRepository;
import com.projeto_final.receitas.storage.SupabaseStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class receitasService {

    private static final Set<String> CATEGORIAS = Set.of("doce", "salgada");

    private final receitaRepository repository;
    private final SupabaseStorageService storage;

    public receitasService(receitaRepository repository, SupabaseStorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public List<Receita> getall() {
        return repository.findAll();
    }

    /**
     * Aplica o filtro escolhido na barra lateral do front.
     * usuarioId so e usado pelo filtro "favorites" e pode vir nulo.
     */
    @Transactional(readOnly = true)
    public List<Receita> listar(String filtro, Long usuarioId) {

        if (filtro == null || filtro.isBlank()) {
            return repository.findAll();
        }

        return switch (filtro.toLowerCase(Locale.ROOT)) {
            case "favorites" -> usuarioId == null
                    ? List.of()
                    : repository.findFavoritasDoUsuario(usuarioId);
            case "quick" -> repository.findByTempoLessThanEqualOrderByTempoAsc(30);
            case "sweet" -> repository.findByCategoriaIgnoreCaseOrderByNomeAsc("doce");
            case "savory" -> repository.findByCategoriaIgnoreCaseOrderByNomeAsc("salgada");
            case "weekly" -> repository.findMaisFavoritadas();
            default -> repository.findAll();
        };
    }

    @Transactional(readOnly = true)
    public Receita getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new resourceNotFoundException(
                        "Receita não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public Receita getByNome(String nome) {
        return repository.findByNome(nome)
                .orElseThrow(() -> new resourceNotFoundException(
                        "Receita não encontrada com o nome: " + nome));
    }

    // ------------------------------------------------------------------ escrita

    @Transactional
    public Receita create(
            String nome,
            String descricao,
            Integer tempo,
            String categoria,
            MultipartFile imagem) {

        validar(nome, descricao, tempo, categoria);

        if (repository.existsByNomeIgnoreCase(nome.trim())) {
            throw new businessException("Já existe uma receita chamada \"" + nome.trim() + "\".");
        }

        Receita receita = new Receita();
        receita.setNome(nome.trim());
        receita.setDesc(descricao.trim());
        receita.setTempo(tempo);
        receita.setCategoria(normalizarCategoria(categoria));
        receita.setImg(storage.upload(imagem));

        return repository.save(receita);
    }

    @Transactional
    public Receita update(
            Long id,
            String nome,
            String descricao,
            Integer tempo,
            String categoria,
            MultipartFile imagem) {

        Receita receita = getById(id);

        validar(nome, descricao, tempo, categoria);

        String nomeLimpo = nome.trim();

        // Permite manter o proprio nome, mas nao roubar o de outra receita.
        repository.findByNome(nomeLimpo).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new businessException("Já existe uma receita chamada \"" + nomeLimpo + "\".");
            }
        });

        receita.setNome(nomeLimpo);
        receita.setDesc(descricao.trim());
        receita.setTempo(tempo);
        receita.setCategoria(normalizarCategoria(categoria));

        // Sem arquivo novo, a foto atual e preservada.
        String novaImagem = storage.upload(imagem);

        if (novaImagem != null) {
            receita.setImg(novaImagem);
        }

        return repository.save(receita);
    }

    @Transactional
    public void delete(Long id) {
        Receita receita = getById(id);
        repository.delete(receita);
    }

    // ---------------------------------------------------------------- validacao

    private void validar(String nome, String descricao, Integer tempo, String categoria) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new businessException("O nome da receita é obrigatório.");
        }

        if (nome.trim().length() > 120) {
            throw new businessException("O nome da receita deve ter no máximo 120 caracteres.");
        }

        if (descricao == null || descricao.trim().isEmpty()) {
            throw new businessException("A descrição é obrigatória.");
        }

        if (descricao.trim().length() > 1000) {
            throw new businessException("A descrição deve ter no máximo 1000 caracteres.");
        }

        if (tempo == null) {
            throw new businessException("O tempo de preparo é obrigatório.");
        }

        if (tempo < 1 || tempo > 1440) {
            throw new businessException("O tempo de preparo deve ficar entre 1 e 1440 minutos.");
        }

        if (categoria != null && !categoria.isBlank()
                && !CATEGORIAS.contains(categoria.trim().toLowerCase(Locale.ROOT))) {
            throw new businessException("A categoria deve ser \"doce\" ou \"salgada\".");
        }
    }

    private String normalizarCategoria(String categoria) {
        return categoria == null || categoria.isBlank()
                ? null
                : categoria.trim().toLowerCase(Locale.ROOT);
    }
}
