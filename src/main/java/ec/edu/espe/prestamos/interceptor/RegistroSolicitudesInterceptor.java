package ec.edu.espe.prestamos.interceptor;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor HTTP: asigna un identificador a cada solicitud de la API, lo devuelve en la
 * cabecera X-Solicitud-Id y registra metodo, ruta, estado y tiempo de respuesta.
 */
@Component
public class RegistroSolicitudesInterceptor implements HandlerInterceptor {

    public static final String CABECERA_ID = "X-Solicitud-Id";
    private static final String ATRIBUTO_INICIO = RegistroSolicitudesInterceptor.class.getName() + ".inicio";
    private static final Logger log = LoggerFactory.getLogger(RegistroSolicitudesInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute(CABECERA_ID, id);
        request.setAttribute(ATRIBUTO_INICIO, System.nanoTime());
        response.setHeader(CABECERA_ID, id);
        log.info("[{}] --> {} {}", id, request.getMethod(), rutaCompleta(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        Object inicio = request.getAttribute(ATRIBUTO_INICIO);
        long milisegundos = inicio instanceof Long nanos ? (System.nanoTime() - nanos) / 1_000_000 : -1;
        log.info("[{}] <-- {} {} estado={} tiempo={} ms", request.getAttribute(CABECERA_ID),
                request.getMethod(), rutaCompleta(request), response.getStatus(), milisegundos);
    }

    private static String rutaCompleta(HttpServletRequest request) {
        String consulta = request.getQueryString();
        return consulta == null ? request.getRequestURI() : request.getRequestURI() + "?" + consulta;
    }
}
