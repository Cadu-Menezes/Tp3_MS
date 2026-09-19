package br.edu.infnet.pedido;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    @PostMapping
    public Map<String, Object> criar(@RequestBody PedidoRequest request,
                                     jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (request.descricao() == null || request.descricao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A descricao e obrigatoria");
        }

        return Map.of(
                "id", UUID.randomUUID().toString(),
                "descricao", request.descricao(),
                "usuario", httpRequest.getAttribute("usuario"),
                "status", "CRIADO");
    }

    public record PedidoRequest(String descricao) {
    }
}
