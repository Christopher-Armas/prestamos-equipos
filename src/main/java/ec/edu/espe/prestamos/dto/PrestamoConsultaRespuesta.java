package ec.edu.espe.prestamos.dto;

import java.time.LocalDate;

import ec.edu.espe.prestamos.entity.Prestamo;

public record PrestamoConsultaRespuesta(
        Long id,
        Long usuarioId,
        Long equipoId,
        String codigoEquipo,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionPrevista,
        LocalDate fechaDevolucionReal,
        String estado) {

    public static PrestamoConsultaRespuesta desde(Prestamo prestamo) {
        return new PrestamoConsultaRespuesta(prestamo.getId(), prestamo.getUsuario().getId(),
                prestamo.getEquipo().getId(), prestamo.getEquipo().getCodigo(),
                prestamo.getFechaPrestamo(), prestamo.getFechaDevolucionPrevista(),
                prestamo.getFechaDevolucionReal(), prestamo.getEstado());
    }
}
