
package com.isil.edu.pe.spedidos.service.domain;

import java.math.BigDecimal;

public record ClienteFrecuente(
        String cliente,
        long totalPedidos,
        long totalUnidades,
        BigDecimal importeAcumulado,
        BigDecimal ticketPromedio
) {
}
