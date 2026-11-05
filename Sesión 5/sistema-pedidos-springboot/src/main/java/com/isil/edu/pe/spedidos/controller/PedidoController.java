package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

  private static final Logger log = LoggerFactory.getLogger(PedidoController.class);
  private final PedidoService pedidoService;
  public PedidoController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
  }
  @GetMapping
  public String listar(Model model, HttpServletResponse response) {
    try {
      cargarDatosVista(model);
      return "pedidos";
    } catch (InventarioNoDisponibleException e) {
      return mostrarServicioNoDisponible(model, response, e.getMessage());
    } catch (RuntimeException e) {
      log.error("Error inesperado al cargar la pantalla de pedidos.", e);
      return mostrarErrorGeneral(model, response);
    }
  }
  @PostMapping
  public String registrar(@RequestParam String cliente, @RequestParam String productoCodigo,
      @RequestParam String cantidad, Model model, HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    try {
      int cantidadNumerica = Integer.parseInt(cantidad);

      Pedido pedido = pedidoService.registrarPedido(cliente, productoCodigo, cantidadNumerica);

      // PRG: POST exitoso -> redirect -> GET.
      redirectAttributes.addAttribute("creado", pedido.getId());
      return "redirect:/pedidos";
    } catch (NumberFormatException e) {
      return mostrarErrorNegocio(model, response, cliente, productoCodigo, cantidad,
          "La cantidad ingresada es inválida.");
    } catch (InventarioNoDisponibleException e) {
      return mostrarServicioNoDisponible(model, response, e.getMessage());
    } catch (PedidoException e) {
      return mostrarErrorNegocio(model, response, cliente, productoCodigo, cantidad,
          e.getMessage());
    } catch (RuntimeException e) {
      log.error("Error inesperado al registrar un pedido.", e);
      return mostrarErrorGeneral(model, response);
    }
  }
  private void cargarDatosVista(Model model) {
    model.addAttribute("productos", pedidoService.listarProductos());
    model.addAttribute("pedidos", pedidoService.listarPedidos());
  }
  private String mostrarErrorNegocio(Model model, HttpServletResponse response, String cliente,
      String productoCodigo, String cantidad, String mensaje) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());

    model.addAttribute("clienteIngresado", cliente);
    model.addAttribute("productoCodigoIngresado", productoCodigo);
    model.addAttribute("cantidadIngresada", cantidad);
    model.addAttribute("error", mensaje);

    try {
      cargarDatosVista(model);
      return "pedidos";
    } catch (InventarioNoDisponibleException e) {
      return mostrarServicioNoDisponible(model, response, e.getMessage());
    }
  }
  private String mostrarServicioNoDisponible(
      Model model,
      HttpServletResponse response,
      String mensaje
  ) {
    response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
    model.addAttribute("error", mensaje);
    return "error";
  }
  private String mostrarErrorGeneral(Model model, HttpServletResponse response) {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    model.addAttribute("error", "Ocurrió un error interno al procesar la solicitud.");
    return "error";
  }

}
