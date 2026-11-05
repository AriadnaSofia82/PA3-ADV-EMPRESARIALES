package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.PedidoResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.service.PedidoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class PedidoApiController {

    private final PedidoService pedidoService;

    public PedidoApiController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoResponse> listarPedidos() {
        return pedidoService.listarPedidos()
                .stream()
                .map(PedidoResponse::from)
                .toList();
    }

    @GetMapping(
            value = "/resumen",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> resumenPedidos() {
        List<Pedido> pedidos = pedidoService.listarPedidos();

        int totalUnidades = pedidos.stream()
                .mapToInt(Pedido::getCantidad)
                .sum();

        Map<String, Object> resumen = new HashMap<>();

        resumen.put("modulo", "Sistema de Pedidos");
        resumen.put("totalPedidos", pedidos.size());
        resumen.put("totalUnidades", totalUnidades);

        return resumen;
    }
}
