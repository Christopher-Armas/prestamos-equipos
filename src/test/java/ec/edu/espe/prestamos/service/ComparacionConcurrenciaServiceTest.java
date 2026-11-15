package ec.edu.espe.prestamos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.espe.prestamos.dto.ResultadoEjecucion;
import org.junit.jupiter.api.Test;

class ComparacionConcurrenciaServiceTest {

    @Test
    void ejecutarEnConcurrenteEsMasRapidoQueEnSecuencia() {
        ComparacionConcurrenciaService servicio = new ComparacionConcurrenciaService(150);
        try {
            ResultadoEjecucion bloqueante = servicio.ejecutarBloqueante();
            ResultadoEjecucion noBloqueante = servicio.ejecutarNoBloqueante();

            assertEquals(3, bloqueante.resultados().size());
            assertEquals(3, noBloqueante.resultados().size());
            assertTrue(bloqueante.milisegundos() >= 450,
                    "En secuencia debe tardar la suma de las tres esperas");
            assertTrue(noBloqueante.milisegundos() < bloqueante.milisegundos(),
                    "En concurrente debe tardar menos que en secuencia");
        } finally {
            servicio.cerrar();
        }
    }
}
