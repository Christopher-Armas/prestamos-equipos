package ec.edu.espe.prestamos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.prestamos.dto.TipoEquipoRequest;
import ec.edu.espe.prestamos.dto.TipoEquipoResponse;
import ec.edu.espe.prestamos.entity.TipoEquipo;
import ec.edu.espe.prestamos.exception.RecursoNoEncontradoException;
import ec.edu.espe.prestamos.exception.ReglaNegocioException;
import ec.edu.espe.prestamos.exception.SolicitudInvalidaException;
import ec.edu.espe.prestamos.repository.EquipoRepository;
import ec.edu.espe.prestamos.repository.TipoEquipoRepository;

@Service
public class TipoEquipoService {

    private final TipoEquipoRepository tipoEquipoRepository;
    private final EquipoRepository equipoRepository;

    public TipoEquipoService(TipoEquipoRepository tipoEquipoRepository, EquipoRepository equipoRepository) {
        this.tipoEquipoRepository = tipoEquipoRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional
    public TipoEquipoResponse crear(TipoEquipoRequest request) {
        String nombre = validarNombre(request);
        if (tipoEquipoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un tipo de equipo con el nombre '" + nombre + "'");
        }
        TipoEquipo tipo = new TipoEquipo();
        tipo.setNombre(nombre);
        tipo.setDescripcion(limpiar(request.descripcion()));
        return aRespuesta(tipoEquipoRepository.save(tipo));
    }

    @Transactional(readOnly = true)
    public List<TipoEquipoResponse> listar() {
        return tipoEquipoRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public TipoEquipoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public TipoEquipoResponse actualizar(Long id, TipoEquipoRequest request) {
        TipoEquipo tipo = buscar(id);
        String nombre = validarNombre(request);
        if (tipoEquipoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe otro tipo de equipo con el nombre '" + nombre + "'");
        }
        tipo.setNombre(nombre);
        tipo.setDescripcion(limpiar(request.descripcion()));
        return aRespuesta(tipoEquipoRepository.save(tipo));
    }

    @Transactional
    public void eliminar(Long id) {
        TipoEquipo tipo = buscar(id);
        if (equipoRepository.existsByTipoEquipoId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el tipo '" + tipo.getNombre() + "' porque tiene equipos asociados");
        }
        tipoEquipoRepository.delete(tipo);
    }

    private TipoEquipo buscar(Long id) {
        return tipoEquipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de equipo con id " + id));
    }

    private String validarNombre(TipoEquipoRequest request) {
        String nombre = request == null ? null : limpiar(request.nombre());
        if (nombre == null) {
            throw new SolicitudInvalidaException("El nombre del tipo de equipo es obligatorio");
        }
        if (nombre.length() > 100) {
            throw new SolicitudInvalidaException("El nombre del tipo de equipo no puede superar 100 caracteres");
        }
        return nombre;
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String recortado = valor.trim();
        return recortado.isEmpty() ? null : recortado;
    }

    private TipoEquipoResponse aRespuesta(TipoEquipo tipo) {
        return new TipoEquipoResponse(tipo.getId(), tipo.getNombre(), tipo.getDescripcion());
    }
}
