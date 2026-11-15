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
class PrestamoControllerIntegrationTest {

    @Value("${local.server.port}") int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired EquipoRepository equipos;
    @Autowired TipoEquipoRepository tipos;
    @Autowired PrestamoRepository prestamos;

    private final HttpClient http = HttpClient.newHttpClient();
    private TipoEquipo tipo;

    @BeforeEach
    void limpiar() {
        prestamos.deleteAll();
        equipos.deleteAll();
        tipos.deleteAll();
        usuarios.deleteAll();
        TipoEquipo nuevo = new TipoEquipo();
        nuevo.setNombre("Portatil");
        tipo = tipos.saveAndFlush(nuevo);
    }

    @Test
    void registraPrestamoYDevolucionCambiandoDisponibilidad() throws Exception {
        Usuario ana = crearUsuario("ana@gmail.com");
        Equipo equipo = crearEquipo("EQ-001");

        assertTrue(enviar("GET", "/api/prestamos/disponibilidad/" + equipo.getId(), null)
                .body().contains("\"disponible\":true"));

        HttpResponse<String> creado = enviar("POST", "/api/prestamos",
                solicitud(equipo.getId(), ana.getId(), LocalDate.now().plusDays(7)));
        assertEquals(201, creado.statusCode());
        assertTrue(creado.body().contains("\"estado\":\"PRESTADO\""));
        Long id = prestamos.findFirstByEquipoIdAndEstado(equipo.getId(), Prestamo.ESTADO_PRESTADO)
                .orElseThrow().getId();

        assertTrue(enviar("GET", "/api/prestamos/disponibilidad/" + equipo.getId(), null)
                .body().contains("\"disponible\":false"));
        assertFalse(enviar("GET", "/api/prestamos/equipos-disponibles", null).body().contains("EQ-001"));
        assertEquals(200, enviar("GET", "/api/prestamos/" + id, null).statusCode());
        assertTrue(enviar("GET", "/api/prestamos?estado=prestado", null).body().contains("EQ-001"));

        HttpResponse<String> devuelto = enviar("PUT", "/api/prestamos/" + id + "/devolucion", null);
        assertEquals(200, devuelto.statusCode());
        assertTrue(devuelto.body().contains("\"estado\":\"DEVUELTO\""));
        assertEquals(409, enviar("PUT", "/api/prestamos/" + id + "/devolucion", null).statusCode());
        assertTrue(enviar("GET", "/api/prestamos/equipos-disponibles", null).body().contains("EQ-001"));
    }

    @Test
    void aplicaReglasDeNegocio() throws Exception {
        Usuario ana = crearUsuario("ana@gmail.com");
        Usuario luis = crearUsuario("luis@gmail.com");
        Equipo equipo = crearEquipo("EQ-001");
        LocalDate enUnaSemana = LocalDate.now().plusDays(7);

        assertEquals(201, enviar("POST", "/api/prestamos",
                solicitud(equipo.getId(), ana.getId(), enUnaSemana)).statusCode());
        // Equipo ya prestado.
        assertEquals(409, enviar("POST", "/api/prestamos",
                solicitud(equipo.getId(), luis.getId(), enUnaSemana)).statusCode());
        // Fecha en el pasado y prestamo demasiado largo.
        Equipo otro = crearEquipo("EQ-002");
        assertEquals(400, enviar("POST", "/api/prestamos",
                solicitud(otro.getId(), luis.getId(), LocalDate.now().minusDays(1))).statusCode());
        assertEquals(400, enviar("POST", "/api/prestamos",
                solicitud(otro.getId(), luis.getId(), LocalDate.now().plusDays(31))).statusCode());
        // Datos faltantes, equipo o usuario inexistente.
        assertEquals(400, enviar("POST", "/api/prestamos", "{\"equipoId\":" + otro.getId() + "}").statusCode());
        assertEquals(404, enviar("POST", "/api/prestamos",
                solicitud(999999L, luis.getId(), enUnaSemana)).statusCode());
        assertEquals(404, enviar("POST", "/api/prestamos",
                solicitud(otro.getId(), 999999L, enUnaSemana)).statusCode());
        assertEquals(404, enviar("GET", "/api/prestamos/999999", null).statusCode());
        assertEquals(400, enviar("GET", "/api/prestamos?estado=PERDIDO", null).statusCode());
    }

    @Test
    void limitaPrestamosActivosPorUsuario() throws Exception {
        Usuario ana = crearUsuario("ana@gmail.com");
        LocalDate enUnaSemana = LocalDate.now().plusDays(7);
        for (int i = 1; i <= 3; i++) {
            Equipo equipo = crearEquipo("EQ-00" + i);
            assertEquals(201, enviar("POST", "/api/prestamos",
                    solicitud(equipo.getId(), ana.getId(), enUnaSemana)).statusCode());
        }
        Equipo cuarto = crearEquipo("EQ-004");
        assertEquals(409, enviar("POST", "/api/prestamos",
                solicitud(cuarto.getId(), ana.getId(), enUnaSemana)).statusCode());
    }

    @Test
    void consultaVencidosYBloqueaUsuarioMoroso() throws Exception {
        Usuario ana = crearUsuario("ana@gmail.com");
        Usuario luis = crearUsuario("luis@gmail.com");
        Equipo vencido = crearEquipo("EQ-VENC");
        Equipo vigente = crearEquipo("EQ-VIG");
        crearPrestamo(ana, vencido, LocalDate.now().minusDays(3));
        crearPrestamo(luis, vigente, LocalDate.now().plusDays(3));

        HttpResponse<String> respuesta = enviar("GET", "/api/prestamos/vencidos", null);
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("EQ-VENC"));
        assertTrue(respuesta.body().contains("\"diasRetraso\":3"));
        assertFalse(respuesta.body().contains("EQ-VIG"));

        Equipo libre = crearEquipo("EQ-LIBRE");
        HttpResponse<String> bloqueado = enviar("POST", "/api/prestamos",
                solicitud(libre.getId(), ana.getId(), LocalDate.now().plusDays(2)));
        assertEquals(409, bloqueado.statusCode());
        assertTrue(bloqueado.body().contains("vencidos"));
    }

    private String solicitud(Long equipoId, Long usuarioId, LocalDate prevista) {
        return "{\"equipoId\":" + equipoId + ",\"usuarioId\":" + usuarioId
                + ",\"fechaDevolucionPrevista\":\"" + prevista + "\"}";
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

    private Usuario crearUsuario(String correo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Nombre");
        usuario.setApellido("Apellido");
        usuario.setCorreo(correo);
        return usuarios.saveAndFlush(usuario);
    }

    private Equipo crearEquipo(String codigo) {
        Equipo equipo = new Equipo();
        equipo.setTipoEquipo(tipo);
        equipo.setCodigo(codigo);
        equipo.setNombre("Equipo " + codigo);
        return equipos.saveAndFlush(equipo);
    }

    private void crearPrestamo(Usuario usuario, Equipo equipo, LocalDate prevista) {
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEquipo(equipo);
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(10));
        prestamo.setFechaDevolucionPrevista(prevista);
        prestamo.setEstado(Prestamo.ESTADO_PRESTADO);
        prestamos.saveAndFlush(prestamo);
    }
}
