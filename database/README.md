# Base de datos

La aplicación usa PostgreSQL con una base llamada `prestamos_equipos`. El usuario es `postgres` y la
contraseña se lee de la variable de entorno `DB_PASSWORD`.

| Archivo | Contenido |
| --- | --- |
| `prestamos_equipos.sql` | Script de las tablas `tipo_equipo`, `equipo`, `usuario` y `prestamo` (PK, FK y UNIQUE) con datos de ejemplo: 3 tipos, 5 equipos y 3 usuarios. Es SQL estándar con `INSERT`. |
| `preparar_base_datos.bat` | Preparación automática en Windows. |

## Opción 1: automática (Windows)

Doble clic en `preparar_base_datos.bat` y aceptar el permiso de administrador. El script:

1. Instala PostgreSQL 18 con `winget` solo si no está instalado (contraseña `Admin2026`, puerto 5432).
2. Arranca el servicio de PostgreSQL.
3. Pide la contraseña del usuario `postgres` si no es `Admin2026`.
4. Crea la base `prestamos_equipos` y carga `prestamos_equipos.sql`. Si la base ya existe, no la modifica.
5. Guarda la variable de entorno `DB_PASSWORD`.

## Opción 2: manual con pgAdmin

1. Clic derecho en **Databases → Create → Database…** y crear la base `prestamos_equipos`.
2. Clic derecho sobre la base nueva → **Query Tool**.
3. Abrir `prestamos_equipos.sql` (icono de carpeta) y ejecutarlo (F5).

## Opción 3: manual con psql

```
createdb -h localhost -U postgres prestamos_equipos
psql -h localhost -U postgres -d prestamos_equipos -f database/prestamos_equipos.sql
```

## Ejecutar la aplicación

Definir la contraseña de PostgreSQL y arrancar:

```powershell
$env:DB_PASSWORD = 'contraseña_de_postgres'
.\mvnw.cmd spring-boot:run
```

En IntelliJ IDEA: **Run → Edit Configurations → Environment variables** → `DB_PASSWORD=contraseña_de_postgres`.

La API queda en `http://localhost:8080`. Ejemplos: `GET /api/equipos`, `GET /api/tipos-equipo` y `GET /api/usuarios`.

Si la base está vacía, Hibernate (`ddl-auto=update`) crea las tablas al arrancar, aunque sin los
datos de ejemplo.
