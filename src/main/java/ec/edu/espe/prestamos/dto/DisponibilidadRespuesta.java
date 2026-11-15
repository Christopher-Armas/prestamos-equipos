package ec.edu.espe.prestamos.dto;

import java.time.LocalDate;

public record DisponibilidadRespuesta(
        Long equipoId,
        String codigoEquipo,
        boolean disponible,
        Long prestamoActivoId,
        LocalDate fechaDevolucionPrevista) {
}
