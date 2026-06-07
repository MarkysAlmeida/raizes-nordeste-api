package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.UnidadeRequest;
import br.com.raizesdonordeste.api.dto.UnidadeUpdateRequest;
import br.com.raizesdonordeste.api.model.Unidade;
import br.com.raizesdonordeste.api.repository.UnidadeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadeService {

    private static final Logger logger =
            LoggerFactory.getLogger(UnidadeService.class);

    private final UnidadeRepository unidadeRepository;

    public UnidadeService(UnidadeRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    public Unidade cadastrar(UnidadeRequest request) {

        Unidade unidade = new Unidade();

        unidade.setNome(request.nome);
        unidade.setCidade(request.cidade);
        unidade.setEndereco(request.endereco);
        unidade.setAtivo(true);

        logger.info("Unidade cadastrada: {}", unidade.getNome());

        return unidadeRepository.save(unidade);
    }

    public List<Unidade> listar() {
        return unidadeRepository.findAll();
    }

    public Unidade editar(Long unidadeId, UnidadeUpdateRequest request) {

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        if (request.nome != null) {
            unidade.setNome(request.nome);
        }

        if (request.cidade != null) {
            unidade.setCidade(request.cidade);
        }

        if (request.endereco != null) {
            unidade.setEndereco(request.endereco);
        }

        if (request.ativo != null) {
            unidade.setAtivo(request.ativo);
        }

        logger.info("Unidade editada: {}", unidade.getNome());

        return unidadeRepository.save(unidade);
    }

    public Unidade desativar(Long unidadeId) {

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        unidade.setAtivo(false);

        logger.info("Unidade desativada: {}", unidade.getNome());

        return unidadeRepository.save(unidade);
    }

    public void excluir(Long unidadeId) {

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        if (Boolean.TRUE.equals(unidade.getAtivo())) {
            throw new RuntimeException("A loja precisa estar desativada para ser excluída");
        }

        try {
            unidadeRepository.delete(unidade);

            logger.info("Unidade excluída: {}", unidade.getNome());

        } catch (Exception e) {
            throw new RuntimeException("Não é possível excluir esta loja porque ela possui usuários, pedidos ou estoque vinculados");
        }
    }
}