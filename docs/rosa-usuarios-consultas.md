# Rosa: gestión de usuarios y consultas

Este módulo se integró sobre el proyecto Maven del grupo 6. Usa la entidad `Usuario`
existente y las relaciones de `Prestamo` con `Usuario` y `Equipo`.

## Reglas de negocio

- El nombre, apellido y correo son obligatorios; el correo debe tener formato válido.
- El correo se guarda en minúsculas y no puede repetirse, aunque cambien las mayúsculas.
- No se puede eliminar un usuario que tenga préstamos, incluso si ya fueron devueltos.

## Endpoints

| Método | Ruta | Respuesta |
| --- | --- | --- |
| `POST` | `/api/usuarios` | Crea un usuario. `201` y cabecera `Location`. |
| `GET` | `/api/usuarios` | Lista usuarios. Admite `?nombre=texto`, que busca en nombre o apellido sin distinguir mayúsculas. |
| `GET` | `/api/usuarios/{id}` | Consulta un usuario. `404` si no existe. |
| `GET` | `/api/usuarios/por-correo?correo=...` | Busca por correo sin distinguir mayúsculas. |
| `PUT` | `/api/usuarios/{id}` | Actualiza nombre, apellido y correo. |
| `DELETE` | `/api/usuarios/{id}` | Elimina un usuario sin préstamos. `204` si tuvo éxito; `409` si tiene historial. |
| `GET` | `/api/usuarios/{id}/prestamos` | Historial de préstamos de un usuario, incluidos los devueltos. |

Ejemplo de cuerpo para `POST` y `PUT`:

```json
{"nombre":"Rosa","apellido":"Quito","correo":"rosa@espe.edu.ec"}
```

Las consultas devuelven DTOs; no serializan directamente las entidades JPA.
Los errores usan el formato `ErrorRespuesta` que ya existía en el proyecto.

## Consultas Spring Data

- `UsuarioRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase`
- `UsuarioRepository.findByCorreoIgnoreCase`
- `PrestamoRepository.buscarPorUsuario` (JPQL con equipo y usuario precargados)

## Ejecutar y comprobar

La aplicación requiere JDK 21, PostgreSQL y la variable de entorno `DB_PASSWORD`.
La base esperada se llama `prestamos_equipos`.

```powershell
$env:DB_PASSWORD = 'su_clave_local'
.\mvnw.cmd spring-boot:run
```

Pruebas de integración: `.\mvnw.cmd test`. Usan H2 en memoria (perfil `test`),
levantan la API en un puerto temporal y comprueban CRUD, validación, duplicados e
historial. No requieren PostgreSQL ni modifican datos reales.

Los préstamos vencidos pertenecen al módulo de préstamos (proceso 6) y no se
implementan aquí.
