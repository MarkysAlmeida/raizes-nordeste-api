package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.UnidadeRequest;
import br.com.raizesdonordeste.api.model.Unidade;
import br.com.raizesdonordeste.api.repository.UnidadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadeService {

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

        return unidadeRepository.save(unidade);
    }

    public List<Unidade> listar() {
        return unidadeRepository.findAll();
    }
}