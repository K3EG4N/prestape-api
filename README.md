# PrestaPe API

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

```
Controller  ->  Service  ->  Repository  ->  PostgreSQL
```

- **Controller**: recibe las peticiones HTTP, valida la entrada y aplica la autorización por rol.
- **Service**: contiene la lógica de negocio y define los límites transaccionales.
- **Repository**: acceso a datos con Spring Data JPA (patrón DAO) y filtros dinámicos mediante Specifications.

### Estructura de paquetes

```
src/main/java/com/prestape
├── controller       Endpoints REST
├── service          Lógica de negocio
├── repository       Acceso a datos
├── specifications   Filtros dinámicos para consultas
├── entity           Entidades JPA y enumeraciones
├── dto              Objetos de request y response
├── security         Configuración de seguridad, JWT y filtros
├── exception        Excepciones de negocio y manejador global
├── report           Generación de reportes Excel
├── config           Propiedades de configuración
├── constants        Valores constantes por módulo
└── util             Utilidades reutilizables

src/main/resources
├── application.properties
├── logback-spring.xml
└── db/migration     Scripts de Flyway (V1 a V12)
```

## Requisitos

- JDK 21
- Maven 3.9 o superior
- PostgreSQL 15 o superior (local o Supabase)

## Configuración

La aplicación lee variables desde un archivo `.env` en la raíz del proyecto (excluido del repositorio). Si una variable no está definida se usa el valor por defecto.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de la base de datos | `jdbc:postgresql://localhost:5432/Prestape` |
| `DB_USER` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | - |
| `DB_POOL_SIZE` | Tamaño máximo del pool de conexiones | `10` |
| `APP_TIMEZONE` | Zona horaria usada en reportes | `America/Lima` |
| `REPORT_MAX_ROWS` | Límite de filas por exportación | `10000` |
| `LOG_PATH` | Carpeta de archivos de log | `logs` |
| `LOG_LEVEL` | Nivel de log del paquete `com.prestape` | `DEBUG` |

Ejemplo de `.env`:

```properties
DB_URL=jdbc:postgresql://<host>:5432/postgres
DB_USER=<usuario>
DB_PASSWORD=<contraseña>
```

Otras propiedades relevantes en `application.properties`: `server.port` (8080), `jwt.expiration-ms` (8 horas) y `app.cors.allowed-origins` (`http://localhost:5173`).

## Ejecución

```bash
mvn clean spring-boot:run
```

Al iniciar, Flyway aplica las migraciones pendientes y Hibernate valida el esquema (`ddl-auto=validate`). La API queda disponible en `http://localhost:8080/api`.

Para cargar datos de prueba se puede ejecutar `db/seed/seed-data.sql` sobre la base de datos.

Para generar el artefacto:

```bash
mvn clean package
java -jar target/prestape-api-0.0.1-SNAPSHOT.jar
```

## Seguridad

- Autenticación stateless mediante JWT enviado en la cabecera `Authorization: Bearer <token>`.
- Contraseñas almacenadas con BCrypt.
- Autorización por rol con `@PreAuthorize`. Las operaciones de gestión están restringidas a `ADMIN` y `SUPERVISOR`.
- Filtros adicionales que bloquean el acceso a usuarios suspendidos o con perfil incompleto.
- Validación de entrada con Bean Validation y manejo centralizado de errores.
- Borrado lógico de registros (`deleted_at`) e índices únicos parciales.

## Endpoints

Todas las rutas, excepto `/api/auth/**`, requieren autenticación.

| Método | Ruta | Descripción | Rol |
|---|---|---|---|
| POST | `/api/auth/login` | Inicio de sesión | Público |
| POST | `/api/auth/register` | Registro de usuario | Público |
| GET | `/api/me` | Datos del usuario autenticado | Autenticado |
| POST | `/api/me/profile` | Completar perfil | Autenticado |
| GET | `/api/menus` | Menú según rol | Autenticado |
| GET | `/api/roles`, `/api/roles/options` | Roles disponibles | Autenticado |
| GET | `/api/users` | Listado paginado con filtros | Admin, Supervisor |
| GET | `/api/users/export` | Exportación a Excel | Admin, Supervisor |
| POST, PUT | `/api/users`, `/api/users/{id}` | Crear y editar usuario | Admin, Supervisor |
| PUT | `/api/users/{id}/suspend`, `/api/users/{id}/reactivate` | Suspender y reactivar | Admin, Supervisor |
| GET | `/api/equipments`, `/api/equipments/options` | Listado y opciones de equipos | Autenticado |
| GET | `/api/equipments/export` | Exportación a Excel | Admin, Supervisor |
| POST, PUT, DELETE | `/api/equipments`, `/api/equipments/{id}` | Gestión de equipos | Admin, Supervisor |
| GET | `/api/kits` | Listado de kits | Autenticado |
| GET | `/api/kits/export` | Exportación a Excel | Admin, Supervisor |
| POST, PUT, DELETE | `/api/kits`, `/api/kits/{id}` | Gestión de kits | Admin, Supervisor |
| GET | `/api/equipment-categories`, `/api/equipment-categories/options` | Listado y opciones de categorías | Autenticado |
| GET | `/api/equipment-categories/export` | Exportación a Excel | Admin, Supervisor |
| PUT, DELETE | `/api/equipment-categories/{id}` | Gestión de categorías | Admin, Supervisor |
| GET | `/api/dashboard/admin` | Indicadores del panel | Admin, Supervisor |

Los listados aceptan los parámetros `page`, `size` (máximo 100) y `search`, además de los filtros propios de cada módulo.

### Formato de respuesta

```json
{
  "status": 200,
  "success": true,
  "message": "OK",
  "value": {}
}
```

En errores de validación se incluye el campo `errors` con el detalle por atributo.

## Logging

Los logs se escriben en consola y en la carpeta definida por `LOG_PATH`, con la configuración de `logback-spring.xml`. Cada petición registra un identificador expuesto en la cabecera `X-Request-Id`.
