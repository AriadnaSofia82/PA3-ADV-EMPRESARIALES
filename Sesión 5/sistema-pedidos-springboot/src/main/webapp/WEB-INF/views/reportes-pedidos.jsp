
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Reportes de pedidos | Sistema de Pedidos</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/app.css">
</head>

<body>

<main class="contenedor">

    <!-- ENCABEZADO -->
    <header class="cabecera">
        <div>
            <p class="eyebrow">Módulo de supervisión</p>
            <h1>Reportes de pedidos</h1>
            <p class="subtitulo">
                Consulta pedidos y analiza el comportamiento de los clientes.
            </p>
        </div>

        <div>
            <div class="flujo">
                Spring MVC → Service → Spring JDBC
            </div>
            <p>
                <a href="${pageContext.request.contextPath}/pedidos">
                    Volver a gestión de pedidos
                </a>
            </p>
        </div>
    </header>


    <!-- 1. BÚSQUEDA AVANZADA -->
    <section class="tarjeta">

        <div class="titulo-tabla">
            <h2>1. Búsqueda avanzada de pedidos</h2>
            <p>
                Filtra los pedidos según el nombre del cliente y un importe mínimo.
            </p>
        </div>

        <form method="get"
              action="${pageContext.request.contextPath}/reportes/pedidos">

            <div class="form-grid">

                <label for="cliente">
                    Nombre del cliente
                    <input
                        type="text"
                        id="cliente"
                        name="cliente"
                        placeholder="Ej. Ana"
                        value="<c:out value='${cliente}'/>"
                        required>
                </label>

                <label for="totalMinimo">
                    Total mínimo
                    <input
                        type="number"
                        id="totalMinimo"
                        name="totalMinimo"
                        min="0"
                        step="0.01"
                        placeholder="Ej. 100.00"
                        value="<c:out value='${totalMinimo}'/>"
                        required>
                </label>

                <div>
                    <label>&nbsp;</label>
                    <button type="submit">
                        Buscar pedidos
                    </button>
                </div>

            </div>
        </form>

        <!-- ERROR DE BÚSQUEDA -->
        <c:if test="${not empty errorBusqueda}">
            <div class="alerta error">
                <strong>Error:</strong>
                <c:out value="${errorBusqueda}"/>
            </div>
        </c:if>

        <!-- RESULTADOS DE BÚSQUEDA -->
        <c:if test="${busquedaRealizada and empty errorBusqueda}">

            <div class="titulo-tabla">
                <h2>Resultados de la búsqueda</h2>
                <p>
                    Pedidos que coinciden con los criterios ingresados.
                </p>
            </div>

            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Cliente</th>
                        <th>Producto</th>
                        <th>Precio unitario</th>
                        <th>Cantidad</th>
                        <th>Total</th>
                        <th>Fecha</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach var="pedido" items="${pedidos}">
                        <tr>
                            <td><c:out value="${pedido.id}"/></td>
                            <td><c:out value="${pedido.cliente}"/></td>
                            <td>
                                <c:out value="${pedido.productoNombre}"/>
                            </td>
                            <td>
                                S/ <c:out value="${pedido.precioUnitario}"/>
                            </td>
                            <td><c:out value="${pedido.cantidad}"/></td>
                            <td>
                                S/ <c:out value="${pedido.total}"/>
                            </td>
                            <td><c:out value="${pedido.fecha}"/></td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty pedidos}">
                        <tr>
                            <td colspan="7" class="sin-datos">
                                No se encontraron pedidos con los criterios ingresados.
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

        </c:if>

    </section>


    <!-- 2. CLIENTES FRECUENTES -->
    <section class="tarjeta">

        <div class="titulo-tabla">
            <h2>2. Reporte de clientes frecuentes</h2>
            <p>
                Identifica clientes según la cantidad mínima de pedidos
                registrados y consulta sus indicadores acumulados.
            </p>
        </div>

        <form method="get"
              action="${pageContext.request.contextPath}/reportes/pedidos">

            <div class="form-grid">

                <label for="minPedidos">
                    Cantidad mínima de pedidos
                    <input
                        type="number"
                        id="minPedidos"
                        name="minPedidos"
                        min="1"
                        step="1"
                        value="<c:out value='${minPedidos}'/>"
                        required>
                </label>

                <div>
                    <label>&nbsp;</label>
                    <button type="submit">
                        Consultar clientes
                    </button>
                </div>

            </div>
        </form>

        <!-- ERROR DE CLIENTES FRECUENTES -->
        <c:if test="${not empty errorFrecuentes}">
            <div class="alerta error">
                <strong>Error:</strong>
                <c:out value="${errorFrecuentes}"/>
            </div>
        </c:if>

        <!-- RESULTADOS DE CLIENTES FRECUENTES -->
        <c:if test="${frecuentesRealizado and empty errorFrecuentes}">

            <div class="titulo-tabla">
                <h2>Resultados de clientes frecuentes</h2>
                <p>
                    Clientes con al menos
                    <strong><c:out value="${minPedidos}"/></strong>
                    pedidos registrados.
                </p>
            </div>

            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr>
                        <th>Cliente</th>
                        <th>Total de pedidos</th>
                        <th>Total de unidades</th>
                        <th>Importe acumulado</th>
                        <th>Ticket promedio</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach var="clienteFrecuente"
                               items="${clientesFrecuentes}">
                        <tr>
                            <td>
                                <c:out value="${clienteFrecuente.cliente}"/>
                            </td>
                            <td>
                                <c:out value="${clienteFrecuente.totalPedidos}"/>
                            </td>
                            <td>
                                <c:out value="${clienteFrecuente.totalUnidades}"/>
                            </td>
                            <td>
                                S/ <c:out value="${clienteFrecuente.importeAcumulado}"/>
                            </td>
                            <td>
                                S/ <c:out value="${clienteFrecuente.ticketPromedio}"/>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty clientesFrecuentes}">
                        <tr>
                            <td colspan="5" class="sin-datos">
                                No se encontraron clientes que cumplan
                                con el mínimo de pedidos indicado.
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

        </c:if>

    </section>

</main>

</body>
</html>
