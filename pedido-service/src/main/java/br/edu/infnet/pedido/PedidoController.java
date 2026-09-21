package br.edu.infnet.pedido;

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
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoRepository repository;

    public PedidoController(PedidoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Pedido criar(@RequestBody PedidoRequest request,
                        jakarta.servlet.http.HttpServletRequest httpRequest) {
        validarDescricao(request);
        Pedido pedido = new Pedido(request.descricao(),
                (String) httpRequest.getAttribute("usuario"), "CRIADO");
        return repository.save(pedido);
    }

    @GetMapping
    public List<Pedido> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Pedido buscar(@PathVariable Long id) {
        return buscarPedido(id);
    }

    @PutMapping("/{id}")
    public Pedido atualizar(@PathVariable Long id, @RequestBody PedidoRequest request) {
        validarDescricao(request);
        Pedido pedido = buscarPedido(id);
        pedido.setDescricao(request.descricao());
        pedido.setStatus("ATUALIZADO");
        return repository.save(pedido);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        Pedido pedido = buscarPedido(id);
        repository.delete(pedido);
    }

    private Pedido buscarPedido(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido nao encontrado"));
    }

    private void validarDescricao(PedidoRequest request) {
        if (request.descricao() == null || request.descricao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A descricao e obrigatoria");
        }
    }

    public record PedidoRequest(String descricao) {
    }
}
