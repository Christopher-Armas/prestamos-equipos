package ec.edu.espe.prestamos.service;

import ec.edu.espe.prestamos.dto.ResultadoEjecucion;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Ejemplo didactico: tres consultas independientes, cada una con una espera simulada
 * (como si llamaran a un servicio externo), ejecutadas de dos formas.
 */
@Service
public class ComparacionConcurrenciaService {

    private static final List<String> TAREAS = List.of(
            "Consulta de equipos", "Consulta de usuarios", "Consulta de prestamos");

    private final long latenciaMs;
    private final ExecutorService ejecutor = Executors.newFixedThreadPool(TAREAS.size());

    public ComparacionConcurrenciaService(@Value("${demo.latencia-ms:300}") long latenciaMs) {
        this.latenciaMs = latenciaMs;
    }

    /** Bloqueante: cada tarea espera a que termine la anterior, en el mismo hilo. */
    public ResultadoEjecucion ejecutarBloqueante() {
        long inicio = System.nanoTime();
        List<String> resultados = new ArrayList<>();
        for (String tarea : TAREAS) {
            resultados.add(simular(tarea));
        }
        return resultado("BLOQUEANTE", inicio, resultados);
    }

    /** No bloqueante entre tareas: se lanzan todas a la vez con CompletableFuture. */
    public ResultadoEjecucion ejecutarNoBloqueante() {
        long inicio = System.nanoTime();
        List<CompletableFuture<String>> futuros = TAREAS.stream()
                .map(tarea -> CompletableFuture.supplyAsync(() -> simular(tarea), ejecutor))
                .toList();
        CompletableFuture.allOf(futuros.toArray(new CompletableFuture[0])).join();
        List<String> resultados = futuros.stream().map(CompletableFuture::join).toList();
        return resultado("NO_BLOQUEANTE", inicio, resultados);
    }

    private String simular(String tarea) {
        try {
            Thread.sleep(latenciaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La tarea fue interrumpida: " + tarea, e);
        }
        return tarea + " completada en el hilo " + Thread.currentThread().getName();
    }

    private ResultadoEjecucion resultado(String modo, long inicio, List<String> resultados) {
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;
        return new ResultadoEjecucion(modo, resultados.size(), milisegundos, resultados);
    }

    @PreDestroy
    public void cerrar() {
        ejecutor.shutdown();
    }
}
