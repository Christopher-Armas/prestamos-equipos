package ec.edu.espe.prestamos.dto;

import java.time.LocalDateTime;

public record ErrorRespuesta(int estado, String error, String mensaje, LocalDateTime fecha) {
}
