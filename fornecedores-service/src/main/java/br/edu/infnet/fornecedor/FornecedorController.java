package br.edu.infnet.fornecedor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {
    private final FornecedorRepository repository;

    public FornecedorController(FornecedorRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Fornecedor criar(@RequestBody FornecedorRequest request) {
        validar(request);
        if (repository.existsByCnpj(request.cnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ ja cadastrado");
        }
        return repository.save(new Fornecedor(request.nome(), request.cnpj()));
    }

    @GetMapping
    public List<Fornecedor> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Fornecedor buscar(@PathVariable Long id) {
        return buscarFornecedor(id);
    }

    @PutMapping("/{id}")
    public Fornecedor atualizar(@PathVariable Long id, @RequestBody FornecedorRequest request) {
        validar(request);
        Fornecedor fornecedor = buscarFornecedor(id);
        if (repository.existsByCnpjAndIdNot(request.cnpj(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ ja cadastrado");
        }
        fornecedor.setNome(request.nome());
        fornecedor.setCnpj(request.cnpj());
        return repository.save(fornecedor);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        repository.delete(buscarFornecedor(id));
    }

    private Fornecedor buscarFornecedor(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Fornecedor nao encontrado"));
    }

    private void validar(FornecedorRequest request) {
        if (request.nome() == null || request.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome e obrigatorio");
        }
        if (request.cnpj() == null || request.cnpj().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O CNPJ e obrigatorio");
        }
    }

    public record FornecedorRequest(String nome, String cnpj) {
    }
}
