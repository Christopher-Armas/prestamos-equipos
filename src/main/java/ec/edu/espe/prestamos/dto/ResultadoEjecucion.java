package ec.edu.espe.prestamos.dto;

import java.util.List;

public record ResultadoEjecucion(String modo, int tareas, long milisegundos, List<String> resultados) {
}
