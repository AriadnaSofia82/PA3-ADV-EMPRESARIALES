package com.isil.edu.pe.spedidos.controller.dto;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResponse(
        Long id,
        String cliente,
        String productoCodigo,
        String productoNombre,
        BigDecimal precioUnitario,
        int cantidad,
        BigDecimal total,
        LocalDateTime fecha,
        PedidoEstado estado
) {

    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente(),
                pedido.getProductoCodigo(),
                pedido.getProductoNombre(),
                pedido.getPrecioUnitario(),
                pedido.getCantidad(),
                pedido.getTotal(),
                pedido.getFecha(),
                pedido.getEstado()
        );
    }
}
