package ec.edu.espe.prestamos.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PrestamoSolicitud(
        @NotNull Long equipoId,
        @NotNull Long usuarioId,
        @NotNull LocalDate fechaDevolucionPrevista) {
}
