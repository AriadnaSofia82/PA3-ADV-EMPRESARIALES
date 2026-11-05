package com.isil.edu.pe.spedidos.controller.reportes.pedidos;

import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import com.isil.edu.pe.spedidos.entity.Pedido;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class PedidoReporteWebController {

    private final PedidoReporteService reporteService;

    public PedidoReporteWebController(PedidoReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/pedidos")
    public String mostrarReportes(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) BigDecimal totalMinimo,
            @RequestParam(required = false) Integer minPedidos,
            Model model) {

        model.addAttribute("pedidos", List.of());
        model.addAttribute("clientesFrecuentes", List.of());

        model.addAttribute("cliente", cliente != null ? cliente : "");
        model.addAttribute("totalMinimo", totalMinimo);
        model.addAttribute("minPedidos", minPedidos != null ? minPedidos : 2);

        model.addAttribute("busquedaRealizada", false);
        model.addAttribute("frecuentesRealizado", false);

        if (cliente != null || totalMinimo != null) {
            model.addAttribute("busquedaRealizada", true);

            try {
                List<Pedido> pedidos = reporteService
                        .buscarPedidosPorClienteYTotalMinimo(cliente, totalMinimo);

                model.addAttribute("pedidos", pedidos);

            } catch (IllegalArgumentException e) {
                model.addAttribute("errorBusqueda", e.getMessage());
            }
        }

        if (minPedidos != null) {
            model.addAttribute("frecuentesRealizado", true);

            try {
                List<ClienteFrecuente> clientesFrecuentes = reporteService
                        .listarClientesFrecuentes(minPedidos);

                model.addAttribute("clientesFrecuentes", clientesFrecuentes);

            } catch (IllegalArgumentException e) {
                model.addAttribute("errorFrecuentes", e.getMessage());
            }
        }

        return "reportes-pedidos";
    }
}
