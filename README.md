# Domain service — Coni Cream Al-Paso (Grupo BR)

Servicio de dominio de la tienda de helados: **productos, inventario, pedidos, liquidación y reporte diario en Excel**.
Se apoya en **AuthCore** para la identidad (JWT) y sigue su misma arquitectura hexagonal.

**Stack (el declarado por el grupo):** Java 21 · Spring Boot 3.5 · Gradle · MySQL 8.4 · Apache POI.

## Estado

| Capa | Estado |
|---|---|
| Dominio + aplicación (HU-8 a HU-12) | Implementados. **79 pruebas unitarias puras** (sin Spring ni base de datos) pasan. |
| Infraestructura (REST, JPA, Excel con POI, seguridad JWT) | Implementada. Sus pruebas con Spring (`@DataJpaTest`, `ConiCreamRestTest`, ArchUnit) **debe ejecutarlas el equipo** con `./gradlew test`: el entorno donde se generó este proyecto no tenía acceso a los repositorios de dependencias. |

> Si alguna prueba de infraestructura falla en su máquina, es un hallazgo real: léala, corríjala y documente qué cambió. Es parte del trabajo.

## Historias de usuario y dónde viven

| HU | Qué hace | Casos de uso | Endpoints |
|---|---|---|---|
| HU-8 | Catálogo de productos | `ProductoUseCase` | `POST/GET/PUT/DELETE /api/productos` |
| HU-9 | Inventario en cajas y unidades | `InventarioUseCase` | `GET /api/productos/{id}/stock`, `POST .../stock/reposiciones`, `PUT .../stock` |
| HU-10 | Pedido del comerciante, confirmar descuenta stock | `PedidoUseCase` | `POST /api/pedidos`, `GET /api/pedidos/{id}`, `POST .../confirmacion` |
| HU-11 | Liquidación: (despachadas − sobrantes) × precio | `LiquidacionUseCase` | `POST/GET /api/pedidos/{id}/liquidacion` |
| HU-12 | Reporte diario en Excel | `ReporteUseCase` | `GET /api/reportes/diario?fecha=AAAA-MM-DD` |

Roles de AuthCore: `USER` = comerciante, `ADMIN` = administrador. El catálogo y el stock los ve cualquier usuario autenticado y los cambia solo el ADMIN;
los pedidos y la liquidación son del comerciante (el id del comerciante es el `sub` del token, que debe ser su cédula);
el reporte es solo ADMIN. Un comerciante **no** ve pedidos de otro (403); el ADMIN sí.

## Estructura

```
com.conicream.alpaso
├── domain            Producto, StockProducto, Pedido, Liquidacion, ReporteDiario, Money; reglas (ReglaLiquidacion, ValidadorStock)
├── application       puertos de entrada (casos de uso), puertos de salida (repositorios, ReporteExportPort, TransaccionPort), servicios
└── infrastructure    REST, persistencia JPA, ReporteExcelAdapter (POI), seguridad JWT, configuración (BeanConfig)
```

Regla de dependencias (la verifica ArchUnit): `infrastructure → application → domain`. El dominio y la aplicación **no** importan Spring, JPA ni POI.

## Ejecutar

Requisitos: JDK 21. El wrapper de Gradle ya está incluido.

```bash
# la clave compartida con AuthCore (Base64, mínimo 48 bytes) se entrega por variable de entorno; NUNCA en el código
export JWT_SECRET_BASE64="<la misma clave que usa AuthCore>"

./gradlew test        # todas las pruebas
./gradlew bootRun     # arranca en el puerto 8082 con H2 en memoria
```

Con MySQL 8.4 (perfil `prod`):

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://localhost:3306/conicream"
export DB_USER="..."          # no se escriben en archivos del repositorio
export DB_PASSWORD="..."
./gradlew bootRun
```

Pruebe los endpoints con `requests.http` (pegue allí un token emitido por AuthCore).

## Decisiones de diseño (y por qué)

- **Hexagonal + DIP:** los servicios reciben puertos por constructor; `BeanConfig` es el único lugar que los conecta.
- **Factory Method:** `Producto.registrar`, `Pedido.crear`, `Liquidacion.nueva` y los `reconstruir` para cargar desde la base. Un objeto nunca nace inválido.
- **Strategy (OCP):** `ReglaLiquidacion` / `LiquidacionPorVendidas`. Otra regla de cobro = otra clase + cambiar una línea en `BeanConfig`.
- **Dinero:** `Money` sobre `BigDecimal`, sin `double` en el dominio.
- **Precio congelado:** el pedido guarda nombre y precio del momento; cambiar el catálogo después no altera pedidos ni liquidaciones.
- **Stock en unidades:** se guarda el total de unidades y se presenta en cajas + sueltas, así vender "100 conos" y "una caja de 24" no se contradicen.
- **Confirmar = todo o nada:** `TransaccionPort` envuelve revalidación de stock + descuento + cambio de estado. Si algo falla, no queda nada a medias.
- **Concurrencia:** `@Version` en producto, stock y pedido; `UNIQUE` en la clave activa del producto y en `liquidacion.pedido_id`. Si dos peticiones chocan, una recibe 409.
- **Sobrantes no regresan al stock:** un helado que salió de la tienda no se puede garantizar apto para volver al congelador. Es una regla de negocio **propuesta**; si el cliente decide otra cosa, se cambia en un solo lugar.
- **Retirar un producto es baja lógica:** conserva el historial de pedidos; libera su nombre y marca.
- **Reporte Excel:** una hoja, una fila por producto de cada pedido, dinero como número con formato, pedidos sin liquidar marcados "Pendiente" y fuera del total.
- **Reloj inyectado (`Clock`):** la fecha de los pedidos y la validación "no se pide reporte del futuro" se prueban sin depender de la hora real.

## Pendiente / siguiente paso del equipo

- Ejecutar `./gradlew test` y corregir lo que muestre.
- Reemplazar `ddl-auto: update` por migraciones Flyway antes de producción.
- Paginación en listados si el catálogo crece.
- Confirmar con el cliente las reglas marcadas como *propuestas* (ver hoja de decisiones del Excel del backlog).
- Pruebas manuales de punta a punta con AuthCore real (token firmado con la clave compartida).
