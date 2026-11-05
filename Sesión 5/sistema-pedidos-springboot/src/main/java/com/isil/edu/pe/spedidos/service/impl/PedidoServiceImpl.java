package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.PedidoService;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoServiceImpl.class);

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;
    private final DevolucionStockClient devolucionStockClient;
    private final FalloLocalRegistroPedidoSimulator falloLocalSimulator;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            InventarioClient inventarioClient,
            DevolucionStockClient devolucionStockClient,
            FalloLocalRegistroPedidoSimulator falloLocalSimulator
    ) {
        this.pedidoRepository = pedidoRepository;
        this.inventarioClient = inventarioClient;
        this.devolucionStockClient = devolucionStockClient;
        this.falloLocalSimulator = falloLocalSimulator;
    }

    @Override
    @Transactional
    public Pedido registrarPedido(
            String cliente,
            String productoCodigo,
            int cantidad
    ) {
        validarDatos(cliente, productoCodigo, cantidad);

        final String codigo = productoCodigo.trim();
        final ReservaStockResponse reserva = reservarStock(codigo, cantidad);

        try {
            validarReserva(reserva);

            BigDecimal total = reserva.getPrecio()
                    .multiply(BigDecimal.valueOf(cantidad));

            Pedido pedido = new Pedido(
                    cliente.trim(),
                    reserva.getCodigo(),
                    reserva.getNombre(),
                    reserva.getPrecio(),
                    cantidad,
                    total
            );

            Pedido guardado = pedidoRepository.save(pedido);

            falloLocalSimulator.verificarAntesDelCommit();

            return guardado;

        } catch (RuntimeException falloLocal) {
            compensarReservaConfirmada(codigo, cantidad, falloLocal);
            throw falloLocal;
        }
    }

    @Override
    public List<ProductoInventarioResponse> listarProductos() {
        return inventarioClient.listarProductos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAllByOrderByIdDesc();
    }

    private ReservaStockResponse reservarStock(
            String codigo,
            int cantidad
    ) {
        try {
            return inventarioClient.reservarStock(codigo, cantidad);

        } catch (InventarioClientException e) {

            if (e.getStatus() >= 500) {
                throw new InventarioNoDisponibleException(
                        "El servicio de inventario presentó un error temporal (HTTP "
                                + e.getStatus() + ").",
                        e
                );
            }

            throw new PedidoException(e.getMessage(), e);
        }
    }

    private void compensarReservaConfirmada(
            String codigo,
            int cantidad,
            RuntimeException falloLocal
    ) {
        try {
            DevolucionStockResponse devolucion =
                    devolucionStockClient.devolverStock(codigo, cantidad);

            if (devolucion == null
                    || !codigo.equals(devolucion.codigo())
                    || devolucion.cantidadDevuelta() != cantidad) {

                throw new PedidoException(
                        "Inventario no confirmó correctamente la devolución compensatoria."
                );
            }

            log.warn(
                    "PA2_COMPENSACION_OK codigo={} cantidad={} causaLocal={}",
                    codigo,
                    cantidad,
                    falloLocal.getMessage()
            );

        } catch (RuntimeException falloCompensacion) {

            falloLocal.addSuppressed(falloCompensacion);

            log.error(
                    "PA2_PENDIENTE_CONCILIACION codigo={} cantidad={}: "
                            + "la reserva remota fue confirmada, ocurrió un fallo local "
                            + "y la compensación no pudo confirmarse.",
                    codigo,
                    cantidad,
                    falloCompensacion
            );
        }
    }
    private void validarDatos(
            String cliente,
            String productoCodigo,
            int cantidad
    ) {
        if (cliente == null || cliente.isBlank()) {
            throw new PedidoException("El cliente es obligatorio.");
        }

        if (productoCodigo == null || productoCodigo.isBlank()) {
            throw new PedidoException("Debe seleccionar un producto.");
        }

        if (cantidad <= 0) {
            throw new PedidoException(
                    "La cantidad debe ser mayor que cero."
            );
        }
    }
    private void validarReserva(ReservaStockResponse reserva) {
        if (reserva == null
                || reserva.getCodigo() == null
                || reserva.getCodigo().isBlank()
                || reserva.getNombre() == null
                || reserva.getNombre().isBlank()
                || reserva.getPrecio() == null) {

            throw new PedidoException(
                    "Inventario devolvió una respuesta incompleta."
            );
        }
    }
}
