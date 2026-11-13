package ec.edu.espe.prestamos.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.prestamos.dto.UsuarioRespuesta;
import ec.edu.espe.prestamos.dto.UsuarioSolicitud;
import ec.edu.espe.prestamos.entity.Usuario;
import ec.edu.espe.prestamos.exception.RecursoNoEncontradoException;
import ec.edu.espe.prestamos.exception.ReglaNegocioException;
import ec.edu.espe.prestamos.repository.PrestamoRepository;
import ec.edu.espe.prestamos.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PrestamoRepository prestamos;

    public UsuarioService(UsuarioRepository usuarios, PrestamoRepository prestamos) {
        this.usuarios = usuarios;
        this.prestamos = prestamos;
    }

    @Transactional
    public UsuarioRespuesta crear(UsuarioSolicitud solicitud) {
        String correo = normalizarCorreo(solicitud.correo());
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
        }
        Usuario usuario = new Usuario();
        copiarDatos(usuario, solicitud, correo);
        return UsuarioRespuesta.desde(usuarios.saveAndFlush(usuario));
    }

    public List<UsuarioRespuesta> listar(String nombre) {
        List<Usuario> resultado = nombre == null || nombre.isBlank()
                ? usuarios.findAll()
                : usuarios.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
                        nombre.trim(), nombre.trim());
        return resultado.stream().map(UsuarioRespuesta::desde).toList();
    }

    public UsuarioRespuesta buscarPorId(Long id) {
        return UsuarioRespuesta.desde(obtenerEntidad(id));
    }

    public UsuarioRespuesta buscarPorCorreo(String correo) {
        String normalizado = normalizarCorreo(correo);
        return usuarios.findByCorreoIgnoreCase(normalizado)
                .map(UsuarioRespuesta::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario con el correo " + normalizado));
    }

    @Transactional
    public UsuarioRespuesta actualizar(Long id, UsuarioSolicitud solicitud) {
        Usuario usuario = obtenerEntidad(id);
        String correo = normalizarCorreo(solicitud.correo());
        if (usuarios.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
        }
        copiarDatos(usuario, solicitud, correo);
        return UsuarioRespuesta.desde(usuarios.saveAndFlush(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtenerEntidad(id);
        if (prestamos.existsByUsuarioId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un usuario con historial de prestamos");
        }
        usuarios.delete(usuario);
        usuarios.flush();
    }

    private Usuario obtenerEntidad(Long id) {
        return usuarios.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("No existe un usuario con id " + id));
    }

    private static void copiarDatos(Usuario usuario, UsuarioSolicitud solicitud, String correo) {
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellido(solicitud.apellido().trim());
        usuario.setCorreo(correo);
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
