package ec.edu.espe.prestamos.dto;

public record EquipoResponse(Long id, String codigo, String nombre, String descripcion,
        Long tipoEquipoId, String tipoEquipoNombre) {
}
