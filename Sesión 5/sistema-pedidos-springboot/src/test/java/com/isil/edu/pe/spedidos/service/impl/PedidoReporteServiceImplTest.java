package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PedidoReporteServiceImplTest {

    @Mock
    private PedidoReporteJdbcRepository jdbcRepository;

    @InjectMocks
    private PedidoReporteServiceImpl reporteService;

    @Test
    void debeRechazarClienteVacioEnBusquedaAvanzada() {

        // Arrange
        String cliente = " ";
        BigDecimal totalMinimo = new BigDecimal("100.00");

        // Act y Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> reporteService.buscarPedidosPorClienteYTotalMinimo(
                        cliente,
                        totalMinimo
                )
        );

        verifyNoInteractions(jdbcRepository);
    }

    @Test
    void debeRechazarTotalMinimoNegativo() {

        // Arrange
        String cliente = "Ana";
        BigDecimal totalMinimo = new BigDecimal("-100.00");

        // Act y Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> reporteService.buscarPedidosPorClienteYTotalMinimo(
                        cliente,
                        totalMinimo
                )
        );

        verifyNoInteractions(jdbcRepository);
    }

    @Test
    void debeRechazarMinimoDePedidosIgualACero() {

        // Arrange
        int minPedidos = 0;

        // Act y Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> reporteService.listarClientesFrecuentes(
                        minPedidos
                )
        );

        verifyNoInteractions(jdbcRepository);
    }
}
