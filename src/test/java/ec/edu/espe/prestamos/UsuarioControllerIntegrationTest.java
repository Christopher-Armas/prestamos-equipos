package ec.edu.espe.prestamos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import ec.edu.espe.prestamos.entity.Equipo;
import ec.edu.espe.prestamos.entity.Prestamo;
import ec.edu.espe.prestamos.entity.TipoEquipo;
import ec.edu.espe.prestamos.entity.Usuario;
import ec.edu.espe.prestamos.repository.EquipoRepository;
import ec.edu.espe.prestamos.repository.PrestamoRepository;
import ec.edu.espe.prestamos.repository.TipoEquipoRepository;
import ec.edu.espe.prestamos.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UsuarioControllerIntegrationTest {

    @Value("${local.server.port}") int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired EquipoRepository equipos;
    @Autowired TipoEquipoRepository tipos;
    @Autowired PrestamoRepository prestamos;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void limpiar() {
        prestamos.deleteAll();
        equipos.deleteAll();
        tipos.deleteAll();
        usuarios.deleteAll();
    }

    @Test
    void creaConsultaActualizaYEliminaUsuario() throws Exception {
        HttpResponse<String> creado = enviar("POST", "/api/usuarios", """
                {"nombre":"Rosa","apellido":"Quito","correo":"rosaquito@gmail.com"}
                """);
        assertEquals(201, creado.statusCode());
        assertTrue(creado.body().contains("rosaquito@gmail.com"));

        Long id = usuarios.findByCorreoIgnoreCase("rosaquito@gmail.com").orElseThrow().getId();
        assertEquals(200, enviar("GET", "/api/usuarios/" + id, null).statusCode());
        assertTrue(enviar("GET", "/api/usuarios?nombre=qui", null).body().contains("Quito"));
        assertTrue(enviar("GET", "/api/usuarios/por-correo?correo=rosaquito@gmail.com", null)
                .body().contains("Rosa"));

        HttpResponse<String> actualizado = enviar("PUT", "/api/usuarios/" + id, """
                {"nombre":"Rosa Silvana","apellido":"Quito","correo":"rosa.nueva@gmail.com"}
                """);
        assertEquals(200, actualizado.statusCode());
        assertTrue(actualizado.body().contains("Rosa Silvana"));

        assertEquals(204, enviar("DELETE", "/api/usuarios/" + id, null).statusCode());
        assertEquals(404, enviar("GET", "/api/usuarios/" + id, null).statusCode());
    }

    @Test
    void rechazaCorreoDuplicadoYDatosInvalidos() throws Exception {
        crearUsuario("Ana", "Perez", "ana@gmail.com");
        HttpResponse<String> duplicado = enviar("POST", "/api/usuarios", """
                {"nombre":"Otra","apellido":"Persona","correo":"ana@gmail.com"}
                """);
        assertEquals(409, duplicado.statusCode());
        assertTrue(duplicado.body().contains("Ya existe"));

        assertEquals(400, enviar("POST", "/api/usuarios", """
                {"nombre":" ","apellido":"Perez","correo":"no-es-correo"}
                """).statusCode());
        assertEquals(404, enviar("GET", "/api/usuarios/999999", null).statusCode());
    }

    @Test
    void historialRespetaUsuarioYBloqueaBorrado() throws Exception {
        Usuario ana = crearUsuario("Ana", "Perez", "ana@gmail.com");
        Usuario luis = crearUsuario("Luis", "Diaz", "luis@gmail.com");
        TipoEquipo tipo = new TipoEquipo();
        tipo.setNombre("Portatil");
        tipo = tipos.saveAndFlush(tipo);
        Equipo equipo = crearEquipo(tipo, "EQ-001");
        Equipo otroEquipo = crearEquipo(tipo, "EQ-002");
        crearPrestamo(ana, equipo, LocalDate.now().minusDays(2), Prestamo.ESTADO_PRESTADO, null);
        crearPrestamo(ana, otroEquipo, LocalDate.now().minusDays(1),
                Prestamo.ESTADO_DEVUELTO, LocalDate.now());
        crearPrestamo(luis, equipo, LocalDate.now().plusDays(1), Prestamo.ESTADO_PRESTADO, null);

        HttpResponse<String> historial = enviar("GET", "/api/usuarios/" + ana.getId() + "/prestamos", null);
        assertEquals(200, historial.statusCode());
        assertTrue(historial.body().contains("EQ-001"));
        assertTrue(historial.body().contains("EQ-002"));
        assertFalse(historial.body().contains("\"usuarioId\":" + luis.getId()));

        assertEquals(404, enviar("GET", "/api/usuarios/999999/prestamos", null).statusCode());
        assertEquals(409, enviar("DELETE", "/api/usuarios/" + ana.getId(), null).statusCode());
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + port + ruta));
        if (cuerpo == null) {
            solicitud.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            solicitud.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        }
        return http.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Usuario crearUsuario(String nombre, String apellido, String correo) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        return usuarios.saveAndFlush(usuario);
    }

    private Equipo crearEquipo(TipoEquipo tipo, String codigo) {
        Equipo equipo = new Equipo();
        equipo.setTipoEquipo(tipo);
        equipo.setCodigo(codigo);
        equipo.setNombre("Equipo " + codigo);
        return equipos.saveAndFlush(equipo);
    }

    private void crearPrestamo(Usuario usuario, Equipo equipo, LocalDate prevista,
            String estado, LocalDate devuelta) {
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEquipo(equipo);
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(5));
        prestamo.setFechaDevolucionPrevista(prevista);
        prestamo.setFechaDevolucionReal(devuelta);
        prestamo.setEstado(estado);
        prestamos.saveAndFlush(prestamo);
    }
}
