<div align="center">

UNIVERSIDAD DE LAS FUERZAS ARMADAS "ESPE"

Departamento de Ciencias de la Computación

Ingeniería en Tecnologías de la Información

<br>

Programación Avanzada – NRC 36759 – Taller Grupal 1

<br>

GRUPO 6

</div>

# Sistema de Préstamos de Equipos Tecnológicos

API REST para gestionar los préstamos de equipos tecnológicos (laptops, proyectores, tablets) de una institución: tipos de equipo, equipos, usuarios, préstamos y devoluciones. Controla la disponibilidad de cada equipo e identifica los préstamos vencidos.

## Integrantes

| Integrante | Módulo |
| --- | --- |
| Christopher Armas | Proyecto base, entidades, repositorios, excepciones, módulo de tipos de equipo y equipos, y base de datos |
| Rosa Quito | Módulo de usuarios: CRUD, búsquedas, historial y validaciones |
| Marlon Naula | Módulo de préstamos: registro, devolución, disponibilidad y vencidos |
| Wilmer Paguay | Manejo global de errores, interceptor HTTP, aspecto AOP, ejemplo bloqueante/no bloqueante y pipeline CI/CD |

Docente: Dra. Eleana Jerez Villota

## Tecnologías

- Java 21
- Spring Boot 4.1.2 (Web MVC, Data JPA, Validation, AspectJ)
- PostgreSQL 18
- H2 (solo para las pruebas automáticas)
- Maven Wrapper
- GitHub Actions

## Arquitectura

El proyecto sigue una arquitectura N capas dentro del paquete `ec.edu.espe.prestamos`:

```
controller   -> endpoints REST
dto          -> records de solicitud y respuesta
service      -> reglas de negocio
repository   -> Spring Data JPA (consultas derivadas y @Query)
entity       -> TipoEquipo, Equipo, Usuario, Prestamo
exception    -> excepciones personalizadas y ManejadorGlobalExcepciones
interceptor  -> RegistroSolicitudesInterceptor
config       -> WebConfig (registra el interceptor en /api/**)
aspect       -> AuditoriaServiciosAspect (AOP)
```

## Cómo ejecutar

### 1. Base de datos

1. Ejecutar `database/preparar_base_datos.bat` (Windows). El script instala PostgreSQL 18 si no existe, crea la base `prestamos_equipos`, carga los datos iniciales de `database/prestamos_equipos.sql` y guarda la variable de entorno `DB_PASSWORD`.
2. Si PostgreSQL ya está instalado con otra contraseña, crear la base `prestamos_equipos`, cargar `database/prestamos_equipos.sql` y definir la variable de entorno `DB_PASSWORD` con la contraseña del usuario `postgres` (en IntelliJ: *Run > Edit Configurations > Environment variables*).

La conexión está en `src/main/resources/application.properties` (`jdbc:postgresql://localhost:5432/prestamos_equipos`, usuario `postgres`, contraseña tomada de `DB_PASSWORD`).

### 2. Aplicación

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

La API queda disponible en `http://localhost:8080`.

### 3. Pruebas

```bash
mvnw.cmd test        # en PowerShell: .\mvnw.cmd test
```

Se ejecutan 12 pruebas sobre una base H2 en memoria, por lo que no necesitan PostgreSQL.

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST, GET | `/api/tipos-equipo` | Crear y listar tipos de equipo |
| GET, PUT, DELETE | `/api/tipos-equipo/{id}` | Consultar, actualizar y eliminar un tipo |
| POST, GET | `/api/equipos` | Crear y listar equipos |
| GET, PUT, DELETE | `/api/equipos/{id}` | Consultar, actualizar y eliminar un equipo |
| GET | `/api/equipos/tipo/{tipoId}` | Equipos de un tipo |
| POST, GET | `/api/usuarios?nombre=` | Crear, listar y buscar usuarios por nombre |
| GET | `/api/usuarios/por-correo?correo=` | Buscar usuario por correo |
| GET, PUT, DELETE | `/api/usuarios/{id}` | Consultar, actualizar y eliminar un usuario |
| GET | `/api/usuarios/{id}/prestamos` | Historial de préstamos del usuario |
| POST, GET | `/api/prestamos?estado=` | Registrar y listar préstamos (PRESTADO / DEVUELTO) |
| GET | `/api/prestamos/{id}` | Consultar un préstamo |
| PUT | `/api/prestamos/{id}/devolucion` | Registrar la devolución |
| GET | `/api/prestamos/disponibilidad/{equipoId}` | Disponibilidad de un equipo |
| GET | `/api/prestamos/equipos-disponibles` | Equipos sin préstamo activo |
| GET | `/api/prestamos/vencidos` | Préstamos vencidos |
| GET | `/api/concurrencia/bloqueante` | Ejemplo de ejecución bloqueante |
| GET | `/api/concurrencia/no-bloqueante` | Ejemplo con `CompletableFuture` |

## Reglas de negocio

- No se repite el código de un equipo, el nombre de un tipo ni el correo de un usuario (409).
- No se elimina un tipo con equipos, un equipo con préstamos ni un usuario con historial (409).
- Un equipo ya prestado no se puede volver a prestar (409).
- La fecha de devolución prevista debe estar entre hoy y 30 días (400).
- Un usuario con préstamos vencidos no puede pedir otro (409).
- Máximo 3 préstamos activos por usuario (409).
- Un préstamo no se puede devolver dos veces (409).

## Manejo de errores

Todas las respuestas de error tienen el mismo formato:

```json
{
  "estado": 409,
  "error": "Conflict",
  "mensaje": "Ya existe un equipo con el codigo 'LAP-001'",
  "fecha": "2026-11-15T10:00:00"
}
```

Códigos usados: 400 (datos inválidos), 404 (recurso o ruta inexistente), 405 (método no permitido), 409 (regla de negocio), 415 (cuerpo que no es JSON) y 500 (error interno, sin detalles).

## Interceptor y aspecto

- **Interceptor HTTP:** agrega la cabecera `X-Solicitud-Id` a cada respuesta de `/api/**` y registra en consola la entrada (`-->`) y la salida (`<--`) con el estado y el tiempo en milisegundos.
- **Aspecto AOP:** mide el tiempo de cada método de la capa de servicios (registra `WARN` si supera 500 ms) y registra las excepciones que lanzan.

## Integración y entrega continua

El pipeline `.github/workflows/ci.yml` se ejecuta en cada push a `main` o `feat/**` y en cada Pull Request:

1. **compilar-y-probar:** descarga el código, instala JDK 21 y ejecuta `./mvnw -B verify`. Si una prueba falla, la ejecución termina en rojo.
2. **entrega-continua:** solo en `main` y si el paso anterior pasó; empaqueta la aplicación y publica el `.jar` como artefacto descargable desde la pestaña *Actions*.
