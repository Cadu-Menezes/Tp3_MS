package br.edu.infnet.fornecedor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "pedido-service", path = "/api/pedidos")
public interface PedidoClient {
    @GetMapping
    List<PedidoDto> listar();
}
