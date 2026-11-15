# Prestape API

API REST para la gestión de préstamos de equipos de laboratorio. Administra usuarios, catálogo de equipos, categorías y kits, y expone indicadores para el panel de administración. Es consumida por el cliente web [PrestaPe](https://github.com/K3EG4N/prestape) desarrollado en React.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Actuator) |
| Base de datos | PostgreSQL (Supabase) |
| Migraciones | Flyway |
| Autenticación | JWT (JJWT) con Spring Security |
| Reportes | Apache POI (XLSX) |
| Logging | SLF4J + Logback |
| Build | Maven |

## Arquitectura

La aplicación sigue una arquitectura en capas sobre el patrón MVC de Spring. Cada capa depende solo de la inmediatamente inferior.

- **Controller**: recibe las peticiones HTTP, valida la entrada y aplica la autorización por rol.
- **Service**: contiene la lógica de negocio y define los límites transaccionales.
- **Repository**: acceso a datos con Spring Data JPA (patrón DAO) y filtros dinámicos mediante Specifications.

## Seguridad

- Autenticación mediante JWT.
- Autorización por rol con `@PreAuthorize`. Las operaciones de gestión están restringidas a `ADMIN` y `SUPERVISOR`.
- Filtros adicionales que bloquean el acceso a usuarios suspendidos o con perfil incompleto.
- Validación de entrada con Bean Validation y manejo de errores.

## Logging

Los logs se escriben en consola y en la carpeta definida por `LOG_PATH`, con la configuración de `logback-spring.xml`. Cada petición registra un identificador expuesto en la cabecera `X-Request-Id`.
