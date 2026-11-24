# Bitácora de Decisiones Arquitectónicas y Refactorización (Domain Service Coni-Cream)

### [2026-11-07] - Alineación de Stack Tecnológico y Configuración de Persistencia PostgreSQL
* **Capa arquitectónica:** Infraestructura (`build.gradle` y `application.yml`).
* **¿Qué se cambió o agregó?**
  - Se actualizó el toolchain de Gradle para compilar bajo `JavaLanguageVersion.of(25)`.
  - Se sustituyó la dependencia de base de datos en memoria H2 por el driver oficial `org.postgresql:postgresql`.
  - Se configuró `application.yml` con el dialecto `PostgreSQLDialect` y la conexión hacia `core_db` en el puerto 5434.
  - Se unificó el decodificador criptográfico del secreto JWT en Base64 con `authcore-service`.
* **¿Por qué se hizo?**
  - Para garantizar interoperabilidad descentralizada de tokens JWT entre microservicios y unificar el motor de persistencia en PostgreSQL 17.
* **Impacto arquitectónico:** Cero impacto en el dominio. Se mantiene el aislamiento de puertos y adaptadores.

### [2026-11-07] - Actualización de ArchUnit a 1.4.1 y Aislamiento de Pruebas
* **Capa arquitectónica:** QA y Testing (`build.gradle` y `src/test/resources/application.yml`).
* **¿Qué se cambió o agregó?**
  - Se actualizó ArchUnit a la versión `1.4.1` para habilitar el análisis de bytecode de Java 25.
  - Se configuró `src/test/resources/application.yml` utilizando H2 en modo emulación PostgreSQL (`MODE=PostgreSQL`) para pruebas herméticas sin dependencia de servicios externos.
* **¿Por qué se hizo?**
  - Para resolver el error de lectura de clases en ArchUnit bajo Java 25 y permitir la ejecución determinística de los tests de integración en entornos CI/CD.
* **Impacto arquitectónico:** Permite validar las 8 reglas de Arquitectura Hexagonal y los flujos REST de extremo a extremo sin requerir puertos de red externos abiertos.

### [2026-11-15] - Sincronización de Persistencia JPA y Esquema DDL en 3NF
* **Capa arquitectónica:** Infraestructura Persistencia (`entity/`, `mapper/` e `init-db/01_schema.sql`).
* **¿Qué se cambió o agregó?**
  - Se alineó `ProductoEntity` y `StockEntity` eliminando el concepto de cajas y reflejando `codigo` y `stock_minimo`.
  - Se sincronizaron los mappers `ProductoMapper` y `StockMapper` para traducir exclusivamente unidades directas y precios en `Money`.
  - Se armonizó la restricción de base de datos `chk_pedido_estado` con los valores del enum de dominio `EstadoPedido` (`BORRADOR`, `CONFIRMADO`, `LIQUIDADO`).
  - Se confirmó el mapeo de colecciones `@ElementCollection` para `pedido_detalle` y `liquidacion_linea` con claves compuestas por orden de inserción.
* **¿Por qué se hizo?**
  - Para asegurar coherencia relacional sin discrepancias de columnas entre Hibernate y PostgreSQL 17.
* **Impacto arquitectónico:** Preserva el aislamiento de los Agregados de DDD y el principio 3NF de la base de datos sin alterar contratos de dominio.


### [2026-11-16] - Integración del Adaptador OpenPDF para Comprobantes de Caja (HU-15 / FEAT-13)
* **Capa arquitectónica:** Aplicación e Infraestructura Externa (`port/out/`, `adapters/out/report/`, `in/rest/`).
* **¿Qué se cambió o agregó?**
  - Se creó el puerto de salida `ComprobantePdfPort` en la capa de aplicación.
  - Se implementó el adaptador `ComprobantePdfAdapter` utilizando la librería OpenPDF `2.0.3` para renderizar el recibo de caja sin IVA ni impuestos.
  - Se inyectó `StockRepositoryPort` y `ComprobantePdfPort` en `BeanConfig` para orquestar la reintegración de inventario y la emisión de comprobantes.
  - Se expuso el endpoint seguro `GET /api/pedidos/{id}/comprobante-pdf` retornando el stream binario `application/pdf`.
* **¿Por qué se hizo?**
  - Para cumplir con el requerimiento de negocio `[HU-15]` de entregar al comerciante un comprobante físico o digital al momento de rendir cuentas en efectivo.
* **Impacto arquitectónico:** Se respeta estrictamente el principio de inversión de dependencias (DIP) y las 8 reglas de ArchUnit: el dominio y la aplicación no conocen la librería OpenPDF.

### [2026-11-24] - Refactorización Integral del Dominio, Corrección de Inventario y Validación al 100%
* **Capa arquitectónica:** Dominio, Aplicación, Infraestructura (Persistencia, Seguridad, Reportes) y Testing.
* **¿Qué se cambió o agregó?**
  1. **Purga de Sobreingeniería de Cajas:** Se eliminó el factor `unidadesPorCaja` y las operaciones con cajas en `StockProducto`, `Producto`, comandos y DTOs, unificando todo el inventario en unidades físicas directas.
  2. **Resolución del Bug Crítico de Inventario:** Se inyectó `StockRepositoryPort` en `LiquidacionService` y se añadió el método `stock.reintegrar()` para que los helados sobrantes devueltos en la jornada física regresen automáticamente al stock disponible en el congelador.
  3. **Ciclo de Vida de Temporadas:** Se añadió el método de negocio `reactivar()` en `Producto` para que las ediciones limitadas (Navidad, Halloween) puedan volver a venderse en futuras temporadas sin duplicar registros.
  4. **Patrón Cuenta Abierta (Recargas):** Se añadió el método `recargar()` en `Pedido` y el caso de uso `RecargarPedidoCommand` para permitir anexar despachos adicionales a la misma comanda diaria mientras el comerciante está en ruta.
  5. **Comprobante en PDF (HU-15 / FEAT-13):** Se incorporó la librería OpenPDF 2.0.3 y se implementó `ComprobantePdfAdapter` cumpliendo con el puerto de salida `ComprobantePdfPort`.
  6. **Alineación de Seguridad y Base de Datos:** Se actualizó `SecurityConfig` y `Solicitantes` para sincronizar los roles `ADMINISTRADOR` y `COMERCIANTE` provenientes de Auth, y se generó el esquema relacional normalizado en 3NF (`01_schema.sql` y `02_seed.sql` en `init-db/`).
  7. **Testing y Calidad:** Se subió ArchUnit a 1.4.1 (Java 25) y se adaptaron todas las pruebas unitarias, de contrato y de integración REST a unidades puras.
* **¿Por qué se hizo?**
  - Para erradicar la fuga contable de helados en las liquidaciones, alinear el software con la operativa real de distribución de helados en calle y garantizar que los microservicios se comuniquen sin rechazos de seguridad.
* **Impacto arquitectónico:** Se preservó el aislamiento estricto de las capas hexagonales: el dominio sigue siendo Java 25 puro sin frameworks, las entidades JPA reflejan fielmente el esquema relacional en 3NF y los tests pasaron al 100% en verde (`BUILD SUCCESSFUL`).
