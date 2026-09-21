package br.edu.infnet.fornecedor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fornecedores")
public class PedidoConsultaController {
    private final PedidoClient pedidoClient;

    public PedidoConsultaController(PedidoClient pedidoClient) {
        this.pedidoClient = pedidoClient;
    }

    @GetMapping("/pedidos")
    public List<PedidoDto> listarPedidos() {
        return pedidoClient.listar();
    }
}
