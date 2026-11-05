package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import com.isil.edu.pe.spedidos.service.domain.ProductoVendido;
import com.isil.edu.pe.spedidos.service.domain.ResumenPedidos;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoReporteServiceImpl implements PedidoReporteService {

    private final PedidoReporteJdbcRepository jdbcRepository;

    public PedidoReporteServiceImpl(
            PedidoReporteJdbcRepository jdbcRepository) {

        this.jdbcRepository = jdbcRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenPedidos obtenerResumen() {

        ResumenPedidosProjection resumenPedidos =
                jdbcRepository.obtenerResumen();

        return new ResumenPedidos(
                resumenPedidos.totalPedidos(),
                resumenPedidos.totalUnidades(),
                resumenPedidos.importeAcumulado(),
                resumenPedidos.ticketPromedio()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> buscarPorCliente(String cliente) {

        if (cliente == null || cliente.isBlank()) {
            return List.of();
        }

        return jdbcRepository.buscarPorCliente(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoVendido> listarProductosMasVendidos() {

        return jdbcRepository.listarProductosMasVendidos()
                .stream()
                .map(item -> new ProductoVendido(
                        item.productoCodigo(),
                        item.productoNombre(),
                        item.totalPedidos(),
                        item.totalUnidades(),
                        item.importeAcumulado()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> buscarPedidosPorClienteYTotalMinimo(
            String cliente,
            BigDecimal totalMinimo) {

        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del cliente es obligatorio."
            );
        }

        if (totalMinimo == null) {
            throw new IllegalArgumentException(
                    "El total mínimo es obligatorio."
            );
        }

        if (totalMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El total mínimo no puede ser negativo."
            );
        }

        return jdbcRepository.buscarPedidosPorClienteYTotalMinimo(
                cliente.trim(),
                totalMinimo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteFrecuente> listarClientesFrecuentes(
            int minPedidos) {

        if (minPedidos < 1) {
            throw new IllegalArgumentException(
                    "El mínimo de pedidos debe ser al menos 1."
            );
        }

        return jdbcRepository.listarClientesFrecuentes(minPedidos)
                .stream()
                .map(item -> new ClienteFrecuente(
                        item.cliente(),
                        item.totalPedidos(),
                        item.totalUnidades(),
                        item.importeAcumulado(),
                        item.ticketPromedio()
                ))
                .toList();
    }
}
