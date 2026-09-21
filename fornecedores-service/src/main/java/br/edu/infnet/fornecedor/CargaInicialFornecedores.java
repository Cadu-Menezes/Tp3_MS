package br.edu.infnet.fornecedor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialFornecedores implements CommandLineRunner {
    private final FornecedorRepository repository;

    public CargaInicialFornecedores(FornecedorRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        repository.save(new Fornecedor("Alpha Materiais Ltda", "11.111.111/0001-11"));
        repository.save(new Fornecedor("Beta Distribuidora Ltda", "22.222.222/0001-22"));
        repository.save(new Fornecedor("Gamma Tecnologia Ltda", "33.333.333/0001-33"));
        repository.save(new Fornecedor("Delta Comercio Ltda", "44.444.444/0001-44"));
        repository.save(new Fornecedor("Epsilon Servicos Ltda", "55.555.555/0001-55"));
    }
}
