package ec.edu.espe.prestamos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.prestamos.dto.PrestamoConsultaRespuesta;
import ec.edu.espe.prestamos.exception.RecursoNoEncontradoException;
import ec.edu.espe.prestamos.repository.PrestamoRepository;
import ec.edu.espe.prestamos.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class ConsultaPrestamoService {

    private final PrestamoRepository prestamos;
    private final UsuarioRepository usuarios;

    public ConsultaPrestamoService(PrestamoRepository prestamos, UsuarioRepository usuarios) {
        this.prestamos = prestamos;
        this.usuarios = usuarios;
    }

    public List<PrestamoConsultaRespuesta> porUsuario(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe un usuario con id " + usuarioId);
        }
        return prestamos.buscarPorUsuario(usuarioId).stream()
                .map(PrestamoConsultaRespuesta::desde).toList();
    }
}
