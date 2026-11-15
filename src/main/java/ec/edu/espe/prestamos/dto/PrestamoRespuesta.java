package ec.edu.espe.prestamos.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import ec.edu.espe.prestamos.entity.Prestamo;

public record PrestamoRespuesta(
        Long id,
        Long equipoId,
        String codigoEquipo,
        String nombreEquipo,
        Long usuarioId,
        String nombreUsuario,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionPrevista,
        LocalDate fechaDevolucionReal,
        String estado,
        boolean vencido,
        long diasRetraso) {

    public static PrestamoRespuesta desde(Prestamo prestamo, LocalDate hoy) {
        LocalDate prevista = prestamo.getFechaDevolucionPrevista();
        LocalDate referencia = prestamo.getFechaDevolucionReal() != null ? prestamo.getFechaDevolucionReal() : hoy;
        long retraso = Math.max(0, ChronoUnit.DAYS.between(prevista, referencia));
        boolean vencido = Prestamo.ESTADO_PRESTADO.equals(prestamo.getEstado()) && prevista.isBefore(hoy);
        return new PrestamoRespuesta(prestamo.getId(),
                prestamo.getEquipo().getId(), prestamo.getEquipo().getCodigo(), prestamo.getEquipo().getNombre(),
                prestamo.getUsuario().getId(),
                prestamo.getUsuario().getNombre() + " " + prestamo.getUsuario().getApellido(),
                prestamo.getFechaPrestamo(), prevista, prestamo.getFechaDevolucionReal(),
                prestamo.getEstado(), vencido, retraso);
    }
}
