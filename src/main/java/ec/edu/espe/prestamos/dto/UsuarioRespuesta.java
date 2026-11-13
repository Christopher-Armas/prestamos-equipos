package ec.edu.espe.prestamos.dto;

import ec.edu.espe.prestamos.entity.Usuario;

public record UsuarioRespuesta(Long id, String nombre, String apellido, String correo) {
    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(usuario.getId(), usuario.getNombre(),
                usuario.getApellido(), usuario.getCorreo());
    }
}
