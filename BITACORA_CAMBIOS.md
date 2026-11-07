# Bitácora de Decisiones Arquitectónicas y Refactorización (Domain Service Coni-Cream)

### [2026-11-04] - Alineación de Stack Tecnológico y Configuración de Persistencia PostgreSQL
* **Capa arquitectónica:** Infraestructura (`build.gradle` y `application.yml`).
* **¿Qué se cambió o agregó?**
  - Se actualizó el toolchain de Gradle para compilar bajo `JavaLanguageVersion.of(25)`.
  - Se sustituyó la dependencia de base de datos en memoria H2 por el driver oficial `org.postgresql:postgresql`.
  - Se configuró `application.yml` con el dialecto `PostgreSQLDialect` y la conexión hacia `core_db` en el puerto 5434.
  - Se unificó el decodificador criptográfico del secreto JWT en Base64 con `authcore-service`.
* **¿Por qué se hizo?**
  - Para garantizar interoperabilidad descentralizada de tokens JWT entre microservicios y unificar el motor de persistencia en PostgreSQL 17.
* **Impacto arquitectónico:** Cero impacto en el dominio. Se mantiene el aislamiento de puertos y adaptadores.

### [2026-11-04] - Actualización de ArchUnit a 1.4.1 y Aislamiento de Pruebas
* **Capa arquitectónica:** QA y Testing (`build.gradle` y `src/test/resources/application.yml`).
* **¿Qué se cambió o agregó?**
  - Se actualizó ArchUnit a la versión `1.4.1` para habilitar el análisis de bytecode de Java 25.
  - Se configuró `src/test/resources/application.yml` utilizando H2 en modo emulación PostgreSQL (`MODE=PostgreSQL`) para pruebas herméticas sin dependencia de servicios externos.
* **¿Por qué se hizo?**
  - Para resolver el error de lectura de clases en ArchUnit bajo Java 25 y permitir la ejecución determinística de los tests de integración en entornos CI/CD.
* **Impacto arquitectónico:** Permite validar las 8 reglas de Arquitectura Hexagonal y los flujos REST de extremo a extremo sin requerir puertos de red externos abiertos.
