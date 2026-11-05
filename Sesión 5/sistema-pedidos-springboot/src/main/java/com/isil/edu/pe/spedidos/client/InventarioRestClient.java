package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.ApiError;
import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockRequest;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
@Component
public class InventarioRestClient implements InventarioClient {

  private final RestClient restClient;
  private final String baseUrl;
  public InventarioRestClient(
      @Qualifier("inventarioHttpClient")
      RestClient restClient,

      @Value("${inventario.api.base-url}")
      String baseUrl) {

    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }
  @Override
  public List<ProductoInventarioResponse> listarProductos() {
    try {
      List<ProductoInventarioResponse> productos = restClient
          .get()
          .uri(baseUrl + "/productos")
          .retrieve()
          .body(new ParameterizedTypeReference<List<ProductoInventarioResponse>>() { });

      return productos == null ? List.of() : productos;
    } catch (RestClientResponseException e) {
      throw convertirError(e);
    } catch (ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  @Override
  public ReservaStockResponse reservarStock(String codigo, int cantidad) {
    /*
     * DTO utilizado como cuerpo de la solicitud HTTP POST.
     */
    ReservaStockRequest request = new ReservaStockRequest(cantidad);
    try {
      return restClient
          .post()
          .uri(baseUrl + "/productos/codigo/{codigo}/reservas", codigo)
          .body(request)
          .retrieve()
          .body(ReservaStockResponse.class);
    } catch (RestClientResponseException e) {
      throw convertirError(e);
    } catch (ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  private InventarioClientException convertirError(RestClientResponseException e) {
    String mensaje = "inventario-service respondió HTTP " + e.getStatusCode().value() + ".";

    try {
      ApiError error = e.getResponseBodyAs(ApiError.class);
      if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
        mensaje = error.getMessage();
      }
    } catch (RuntimeException ignored) {
      /*
       * Si el body recibido no tiene el formato esperado
       * de ApiError, se conserva el mensaje genérico.
       */
    }

    return new InventarioClientException(e.getStatusCode().value(), mensaje);
  }
  private InventarioNoDisponibleException noDisponible(ResourceAccessException cause) {
    return new InventarioNoDisponibleException(
        "El servicio de inventario no se encuentra disponible o excedió el tiempo de espera.",
        cause
    );
  }
}
