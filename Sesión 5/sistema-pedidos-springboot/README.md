# Sistema de Pedidos -- PA3

## Desarrollo de Aplicaciones Empresariales Avanzado \| ISIL 2026-20

Sistema de gestión y consulta de pedidos desarrollado como evolución
individual del proyecto de PA1 y PA2. En esta tercera entrega se
incorpora un módulo de consulta y supervisión que permite buscar
pedidos, consolidar información de clientes frecuentes, exponer
resultados mediante una vista JSP y una API REST/JSON, y proteger el
acceso mediante Spring Security.

> **Alcance de PA3:** se conserva el CRUD existente basado en Spring
> Data JPA y la integración HTTP con `inventario-service`. Las dos
> consultas nuevas de reportes se implementan obligatoriamente con
> Spring JDBC. No se reemplaza la arquitectura previa ni se vuelve a
> implementar el flujo completo de edición/eliminación de PA1 o la
> anulación de PA2.

------------------------------------------------------------------------

## 1. Tecnologías utilizadas

  -----------------------------------------------------------------------
  Tecnología                          Uso
  ----------------------------------- -----------------------------------
  Java 21                             Lenguaje y plataforma de ejecución

  Spring Boot                         Configuración y ejecución de la
                                      aplicación

  Spring MVC                          Controladores web y navegación

  Spring Data JPA                     Persistencia de las operaciones
                                      existentes de pedidos

  Spring JDBC                         Consultas SQL específicas del
                                      módulo de reportes

  `NamedParameterJdbcTemplate`        Ejecución de consultas
                                      parametrizadas

  Spring Security                     Autenticación, autorización por
                                      roles y protección CSRF

  JSP / JSTL                          Renderizado de la vista web de
                                      reportes

  MySQL                               Base de datos de ejecución normal

  H2                                  Base de datos para pruebas
                                      automatizadas

  JUnit 5                             Pruebas automatizadas

  Mockito                             Aislamiento de servicios y
                                      controladores en pruebas

  Maven                               Gestión de dependencias,
                                      compilación y pruebas

  Postman                             Verificación de endpoints y
                                      escenarios HTTP

  `inventario-service`                Servicio externo para la
                                      reserva/validación de inventario al
                                      registrar pedidos
  -----------------------------------------------------------------------

## 2. Arquitectura y organización

La solución mantiene separación de responsabilidades por capas:

``` text
Navegador / Postman
        |
        v
  Spring Security
  (autenticación, roles y CSRF)
        |
        v
 DispatcherServlet
        |
        +-----------------------------+
        |                             |
        v                             v
PedidoReporteWebController       PedidoApiController
@Controller                     @RestController
        |                             |
        +--------------+--------------+
                       |
                       v
             PedidoReporteService
                       |
                       v
             PedidoReporteServiceImpl
                       |
                       v
          PedidoReporteJdbcRepository
          (Spring JDBC / SQL parametrizado)
                       |
                       v
                     MySQL
```

La operación transaccional existente de pedidos continúa utilizando
Spring Data JPA y su repositorio correspondiente. El registro de pedidos
conserva la integración externa mediante `InventarioRestClient` hacia
`inventario-service`.

### Componentes del módulo de reportes

-   **`PedidoReporteWebController`**: atiende la ruta web de reportes,
    prepara los datos mediante `Model` y retorna la vista JSP.
-   **`PedidoApiController`**: publica los endpoints JSON y delega las
    operaciones en la capa Service.
-   **`PedidoReporteService` / `PedidoReporteServiceImpl`**: centralizan
    las operaciones y validaciones de negocio.
-   **`PedidoReporteJdbcRepository`**: contiene las consultas SQL nuevas
    implementadas con Spring JDBC.
-   **`PedidoRowMapper`**: reutiliza el mapeo de filas cuando el
    resultado corresponde a pedidos.
-   **Proyecciones/DTO de reporte**: representan los resultados
    agregados de clientes frecuentes.

La vista JSP y la API REST utilizan la misma capa Service. Los
controladores no duplican SQL ni reglas de negocio.

## 3. Funcionalidades implementadas para PA3

### 3.1. Búsqueda avanzada de pedidos

Se incorporó la consulta por cliente y monto mínimo:

``` text
buscarPedidosPorClienteYTotalMinimo(cliente, totalMinimo)
```

Características:

-   Busca pedidos correspondientes al cliente indicado.
-   Filtra por un total igual o superior al monto mínimo.
-   Utiliza `NamedParameterJdbcTemplate`.
-   Envía los valores como parámetros con nombre; no concatena los datos
    del usuario al SQL.
-   Reutiliza `PedidoRowMapper` para convertir los resultados en objetos
    `Pedido`.
-   Ordena los resultados por identificador descendente.
-   La validación de cliente no vacío y monto mínimo no negativo se
    realiza en Service.

### 3.2. Reporte de clientes frecuentes

Se incorporó la consulta:

``` text
listarClientesFrecuentes(minPedidos)
```

Características:

-   Utiliza SQL con `GROUP BY` y `HAVING`.
-   Filtra los clientes que alcanzan el mínimo de pedidos indicado.
-   Devuelve los siguientes indicadores por cliente:
    -   `totalPedidos`: cantidad de pedidos.
    -   `totalUnidades`: unidades acumuladas.
    -   `importeAcumulado`: importe total de los pedidos.
    -   `ticketPromedio`: importe promedio por pedido.
-   Utiliza una proyección/DTO para representar los datos agregados.
-   Valida que `minPedidos` sea mayor o igual a 1 antes de consultar.

### 3.3. Vista web JSP

Se incorporó la ruta:

``` http
GET /reportes/pedidos
```

La vista se encuentra en:

``` text
/WEB-INF/views/reporte-pedidos.jsp
```

El controlador utiliza `@Controller`, `@GetMapping` y `Model`, y retorna
la vista mediante el ViewResolver configurado para JSP. La información
se obtiene desde la capa Service.

La vista permite consultar el módulo de reportes desde el navegador, sin
generar HTML directamente en el controlador.

### 3.4. API REST / JSON

Se incorporaron los siguientes endpoints:

  ---------------------------------------------------------------------------------------------
  Método            Ruta                                    Parámetros        Finalidad
  ----------------- --------------------------------------- ----------------- -----------------
  GET               `/api/v1/pedidos/busqueda-avanzada`     `cliente`,        Buscar pedidos
                                                            `totalMinimo`     por cliente y
                                                                              monto mínimo

  GET               `/api/v1/pedidos/clientes-frecuentes`   `minPedidos`      Consultar
                                                                              clientes que
                                                                              cumplen el umbral
                                                                              de pedidos
  ---------------------------------------------------------------------------------------------

Ejemplos de consulta (considerando el context path `/sistema-pedidos`):

**Búsqueda avanzada**

``` http
GET http://localhost:8081/sistema-pedidos/api/v1/pedidos/busqueda-avanzada?cliente=Juan&totalMinimo=100
```

**Clientes frecuentes**

``` http
GET http://localhost:8081/sistema-pedidos/api/v1/pedidos/clientes-frecuentes?minPedidos=2
```

Los endpoints responden JSON y delegan en la misma capa Service
utilizada por la vista JSP. Los parámetros inválidos se manejan de forma
controlada mediante respuestas HTTP coherentes.

## 4. Seguridad: Spring Security

Se actualizó `SecurityConfig` para controlar el acceso al módulo de
reportes y conservar la protección de las operaciones de pedidos.

### Roles de laboratorio

  -----------------------------------------------------------------------
  Usuario                 Rol                     Permisos
  ----------------------- ----------------------- -----------------------
  `operador`              `OPERADOR`              Consultar reportes y
                                                  registrar pedidos

  `consulta`              `CONSULTA`              Consultar reportes; no
                                                  puede registrar pedidos
  -----------------------------------------------------------------------

Los usuarios de laboratorio están configurados en memoria, de acuerdo
con el alcance de la actividad. No se requiere implementar JWT,
OAuth2/OIDC ni persistencia de usuarios en base de datos.

### Reglas de acceso

-   Los usuarios no autenticados que ingresan a `/reportes/pedidos`
    deben pasar por el mecanismo de autenticación configurado.
-   Los roles `OPERADOR` y `CONSULTA` pueden consultar `/reportes/**`.
-   Los roles `OPERADOR` y `CONSULTA` pueden consultar los endpoints de
    reportes `/api/v1/pedidos/**`.
-   El registro mediante `POST /pedidos` está reservado al rol
    `OPERADOR`.
-   Se conserva la protección CSRF. No se deshabilita globalmente para
    permitir solicitudes POST.

**Importante:** las contraseñas de laboratorio deben consultarse en la
configuración local del proyecto. No se incluyen credenciales en este
README. No subir contraseñas personales, tokens ni claves reales al
repositorio.

## 5. Configuración del entorno

### 5.1. Requisitos previos

-   JDK 21.
-   Maven (o Maven Wrapper, si está incluido en el repositorio).
-   MySQL Server.
-   Un cliente HTTP como Postman.
-   `inventario-service` disponible para probar el registro de pedidos.
-   IDE recomendado: IntelliJ IDEA o Visual Studio Code.

### 5.2. Base de datos MySQL

La ejecución normal utiliza MySQL. Crear la base de datos y el usuario
de laboratorio mediante el script proporcionado:

``` text
Script_BD_usuario_MySql.sql
```

Ejecutar el script en MySQL antes de iniciar la aplicación. Verificar
que el usuario tenga permisos sobre la base de datos creada.

### 5.3. Variables de entorno

Las credenciales y la URL de conexión deben externalizarse. La
aplicación utiliza las siguientes variables:

  Variable        Descripción
  --------------- ------------------------------------
  `DB_URL`        URL JDBC de la base de datos MySQL
  `DB_USERNAME`   Usuario de MySQL
  `DB_PASSWORD`   Contraseña del usuario de MySQL

Ejemplo de configuración en PowerShell (reemplazar los valores por los
del entorno local):

``` powershell
$env:DB_URL="jdbc:mysql://localhost:3306/NOMBRE_BASE_DATOS"
$env:DB_USERNAME="USUARIO_MYSQL"
$env:DB_PASSWORD="CONTRASENA_LOCAL"
```

Mantener los valores reales fuera del repositorio. No publicar archivos
`.env`, contraseñas, tokens ni configuraciones sensibles.

### 5.4. Base de datos de pruebas

H2 se reserva para las pruebas automatizadas. Las pruebas de repositorio
pueden ejecutarse con datos de prueba aislados y no deben depender de la
base MySQL de ejecución normal.

## 6. Orden de arranque

La solución depende de `inventario-service` para el registro de pedidos,
pero las consultas del módulo de reportes acceden a la base de datos
local.

### Paso 1. Iniciar MySQL

-   Encender el servicio MySQL.
-   Verificar que la base de datos exista.
-   Confirmar que `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` sean
    correctos.

### Paso 2. Iniciar `inventario-service`

Iniciar el servicio de inventario en el entorno configurado para PA2. En
la configuración de trabajo utilizada, el servicio se ejecuta en el
puerto `8082`, con el contexto `/inventario-service` y la ruta API:

``` text
http://localhost:8082/inventario-service/api/inventario
```

Verificar que el servicio esté disponible antes de probar el registro de
pedidos.

### Paso 3. Iniciar `sistema-pedidos-springboot-jdbc-mysql`

Desde la raíz del proyecto:

``` bash
mvn clean package
```

Luego:

``` bash
mvn spring-boot:run
```

La aplicación Spring Boot se ejecuta en el puerto `8081` y utiliza el
context path:

``` text
http://localhost:8081/sistema-pedidos
```

> Si se modifica el puerto, el context path o la URL de inventario en la
> configuración local, utilizar los valores efectivos definidos en
> `application.properties` y en las variables de entorno.

## 7. Rutas principales

Considerando la ejecución local con puerto `8081` y context path
`/sistema-pedidos`:

  -------------------------------------------------------------------------------------------------------------------------------------------
  Recurso                             URL
  ----------------------------------- -------------------------------------------------------------------------------------------------------
  Aplicación                          `http://localhost:8081/sistema-pedidos`

  Vista de reportes                   `http://localhost:8081/sistema-pedidos/reportes/pedidos`

  API búsqueda avanzada               `http://localhost:8081/sistema-pedidos/api/v1/pedidos/busqueda-avanzada?cliente=Juan&totalMinimo=100`

  API clientes frecuentes             `http://localhost:8081/sistema-pedidos/api/v1/pedidos/clientes-frecuentes?minPedidos=2`
  -------------------------------------------------------------------------------------------------------------------------------------------

Para las rutas de registro y demás operaciones preexistentes, consultar
los controladores y la configuración efectiva del proyecto.

## 8. Pruebas automatizadas

Se incorporaron pruebas específicas para las funcionalidades de PA3. Las
clases de prueba identificadas en el proyecto son:

  ------------------------------------------------------------------------------
  Clase de prueba                            Alcance
  ------------------------------------------ -----------------------------------
  `PedidoApiControllerTest`                  Comportamiento del controlador API
                                             y validaciones/respuestas

  `PedidoReporteControllerIntegrationTest`   Integración de la ruta web de
                                             reportes

  `PedidoSecurityIntegrationTest`            Autenticación, autorización y
                                             reglas de seguridad

  `PedidoServiceImplTest`                    Comportamiento del servicio de
                                             pedidos y continuidad con
                                             inventario

  `PedidoReporteServiceTest`                 Lógica y validaciones del servicio
                                             de reportes
  ------------------------------------------------------------------------------

Las pruebas deben aplicar el patrón AAA (Arrange -- Act -- Assert),
utilizar JUnit 5 y Mockito cuando corresponda, y H2 para las pruebas de
integración del acceso a datos.

### Ejecutar las pruebas

Desde la raíz del proyecto:

``` bash
mvn test
```

Para compilar y ejecutar el ciclo de pruebas:

``` bash
mvn clean verify
```

El resultado válido debe mostrar `BUILD SUCCESS`. Revisar el resumen de
Maven y los reportes generados en:

``` text
target/surefire-reports/
```

> La presencia de las clases de prueba no sustituye la ejecución. Antes
> de entregar, ejecutar `mvn test` y registrar el resultado real. No
> declarar pruebas exitosas si la última ejecución no terminó
> correctamente.

## 9. Cinco escenarios funcionales exigidos por PA3

Los siguientes escenarios deben poder reproducirse en la misma solución
y documentarse con evidencias (parámetros, respuesta, código HTTP y,
cuando corresponda, consulta/datos MySQL).

### Caso 1. Búsqueda por cliente y monto mínimo

1.  Registrar al menos tres pedidos.
2.  Preparar datos donde solo una parte cumpla simultáneamente el
    cliente y el monto mínimo.
3.  Consultar la vista `/reportes/pedidos`.
4.  Consultar el endpoint `/api/v1/pedidos/busqueda-avanzada`.
5.  Comparar los pedidos mostrados con los registros de MySQL.

**Resultado esperado:** únicamente aparecen los pedidos que cumplen
ambos filtros, ordenados por identificador descendente.

### Caso 2. Clientes frecuentes y consolidado

1.  Generar pedidos para al menos dos clientes.
2.  Consultar el endpoint de clientes frecuentes con `minPedidos=2`.
3.  Revisar los valores de `totalPedidos`, `totalUnidades`,
    `importeAcumulado` y `ticketPromedio`.
4.  Contrastar los resultados con los datos de MySQL.

**Resultado esperado:** solo aparecen los clientes que alcanzan el
umbral y sus indicadores agregados son correctos.

### Caso 3. Parámetros inválidos

Probar los siguientes valores:

``` text
cliente=
totalMinimo=-1
minPedidos=0
```

**Resultado esperado:** la aplicación rechaza los parámetros inválidos
de forma controlada (por ejemplo, HTTP 400), sin ejecutar consultas
construidas mediante concatenación insegura.

### Caso 4. Continuidad con PA2: inventario indisponible

1.  Detener `inventario-service`.
2.  Intentar registrar un pedido nuevo.
3.  Comprobar que la aplicación informa la indisponibilidad de
    inventario de forma controlada.
4.  Sin reiniciar el sistema de pedidos, ingresar al módulo de reportes
    y consultar pedidos previamente almacenados.

**Resultado esperado:** el registro no se completa si no se puede
validar/reservar inventario, pero los reportes locales continúan
funcionando porque consultan la base de datos del sistema de pedidos.

### Caso 5. Seguridad por roles y CSRF

Verificar los siguientes recorridos:

1.  Acceder como usuario anónimo a `/reportes/pedidos`: debe solicitar
    autenticación o redirigir al mecanismo configurado.
2.  Iniciar sesión como `CONSULTA`: debe poder visualizar los reportes.
3.  Intentar registrar un pedido como `CONSULTA`, incluso enviando un
    token CSRF válido: debe ser rechazado por autorización.
4.  Intentar un POST como `OPERADOR` sin token CSRF: debe ser rechazado
    por la protección CSRF.
5.  Restaurar `inventario-service` para la evidencia funcional final de
    registro.

**Resultado esperado:** se respetan los permisos por rol y CSRF
permanece habilitado.

## 10. Postman y evidencias

La colección de Postman del proyecto permite reproducir las solicitudes
de la API. Para la entrega, documentar como mínimo:

-   Método y URL solicitada.
-   Parámetros enviados.
-   Usuario/rol utilizado cuando corresponda.
-   Código HTTP recibido.
-   Respuesta JSON o resultado de la vista.
-   Comparación con MySQL cuando el caso lo requiera.

La colección debe mantenerse actualizada con las rutas del context path
y los endpoints de reportes. Para las operaciones protegidas, iniciar
sesión y conservar el mecanismo de sesión/cookies y token CSRF requerido
por la configuración de seguridad.

Las capturas deben complementar, no reemplazar, el repositorio, el
código y la ejecución reproducible de las pruebas.

## 11. Repositorio y control de versiones

La entrega corresponde a un desarrollo individual sobre la base
`sistema-pedidos-springboot-jdbc-mysql`.

Completar antes de presentar:

-   **Repositorio individual:** `PEGAR_URL_DEL_REPOSITORIO`
-   **Rama evaluada:** `PEGAR_NOMBRE_DE_LA_RAMA`
-   **Commit final:** `PEGAR_HASH_DEL_COMMIT`

El repositorio debe permitir revisar el código fuente, los commits y las
pruebas. No se considera suficiente entregar únicamente un ZIP o
capturas de pantalla.

## 12. Limitaciones y consideraciones

-   MySQL es la base de datos para la ejecución normal; H2 se utiliza en
    las pruebas automatizadas.
-   `inventario-service` es una dependencia externa para el registro de
    pedidos. Su indisponibilidad no debe impedir consultar los reportes
    almacenados localmente.
-   Los usuarios de laboratorio de Spring Security están configurados en
    memoria; no se implementa persistencia de usuarios.
-   No se implementan JWT, OAuth2/OIDC ni una nueva solución de
    autenticación externa porque no forman parte del alcance requerido.
-   La protección CSRF debe permanecer activa.
-   No se deben subir credenciales reales, tokens ni archivos sensibles.
-   La ejecución exitosa de `mvn test`, la disponibilidad del
    repositorio y las evidencias de los cinco escenarios deben
    verificarse antes de la entrega.

## 13. Referencias

-   Spring. (s. f.). *Spring Boot Reference Documentation*.
    https://docs.spring.io/spring-boot/
-   Spring. (s. f.). *Spring Framework Reference Documentation*.
    https://docs.spring.io/spring-framework/reference/
-   Spring. (s. f.). *Spring Security Reference*.
    https://docs.spring.io/spring-security/reference/
-   Spring. (s. f.). *Spring Data JPA Reference Documentation*.
    https://docs.spring.io/spring-data/jpa/reference/
-   Spring. (s. f.). *JDBC*.
    https://docs.spring.io/spring-framework/reference/data-access/jdbc.html
-   Apache Maven. (s. f.). *Maven Documentation*.
    https://maven.apache.org/guides/
-   JUnit. (s. f.). *JUnit 5 User Guide*.
    https://junit.org/junit5/docs/current/user-guide/
-   MySQL. (s. f.). *MySQL Reference Manual*. https://dev.mysql.com/doc/

------------------------------------------------------------------------

**Documento de apoyo para la ejecución y revisión técnica de PA3.**
Antes de publicar el repositorio, completar los datos de Git y comprobar
que los puertos, rutas, usuarios de laboratorio y variables de entorno
coincidan con la configuración efectiva del proyecto.
