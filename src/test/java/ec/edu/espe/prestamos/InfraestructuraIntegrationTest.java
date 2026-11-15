package ec.edu.espe.prestamos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import ec.edu.espe.prestamos.interceptor.RegistroSolicitudesInterceptor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class InfraestructuraIntegrationTest {

    @Value("${local.server.port}") int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void interceptorAgregaIdentificadorYRegistraLaSolicitud(CapturedOutput salida) throws Exception {
        HttpResponse<String> respuesta = enviar("GET", "/api/tipos-equipo", null, null);
        assertEquals(200, respuesta.statusCode());
        String id = respuesta.headers().firstValue(RegistroSolicitudesInterceptor.CABECERA_ID).orElseThrow();
        assertEquals(8, id.length());
        assertTrue(salida.getOut().contains("[" + id + "] --> GET /api/tipos-equipo"));
        assertTrue(salida.getOut().contains("[" + id + "] <-- GET /api/tipos-equipo estado=200"));
    }

    @Test
    void aspectoAuditaLaCapaDeServicios(CapturedOutput salida) throws Exception {
        enviar("GET", "/api/tipos-equipo", null, null);
        assertTrue(salida.getOut().contains("[AOP] TipoEquipoService.listar ejecutado en"));

        enviar("GET", "/api/tipos-equipo/999999", null, null);
        assertTrue(salida.getOut().contains("[AOP] TipoEquipoService.obtener lanzo RecursoNoEncontradoException"));
    }

    @Test
    void erroresDelFrameworkUsanElFormatoComun() throws Exception {
        HttpResponse<String> tipoInvalido = enviar("GET", "/api/equipos/abc", null, null);
        assertEquals(400, tipoInvalido.statusCode());
        assertTrue(tipoInvalido.body().contains("\"mensaje\":\"El valor 'abc' no es valido para el parametro 'id'\""));

        HttpResponse<String> faltaParametro = enviar("GET", "/api/usuarios/por-correo", null, null);
        assertEquals(400, faltaParametro.statusCode());
        assertTrue(faltaParametro.body().contains("correo"));

        HttpResponse<String> metodo = enviar("PATCH", "/api/tipos-equipo", "{}", "application/json");
        assertEquals(405, metodo.statusCode());
        assertTrue(metodo.body().contains("\"estado\":405"));

        HttpResponse<String> contenido = enviar("POST", "/api/tipos-equipo", "nombre=x", "text/plain");
        assertEquals(415, contenido.statusCode());

        HttpResponse<String> ruta = enviar("GET", "/api/no-existe", null, null);
        assertEquals(404, ruta.statusCode());
        assertTrue(ruta.body().contains("no existe"));
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo, String tipo) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + port + ruta));
        if (cuerpo == null) {
            solicitud.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            solicitud.header("Content-Type", tipo).method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        }
        return http.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }
}
