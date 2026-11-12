package ec.edu.espe.prestamos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.prestamos.dto.EquipoRequest;
import ec.edu.espe.prestamos.dto.EquipoResponse;
import ec.edu.espe.prestamos.entity.Equipo;
import ec.edu.espe.prestamos.entity.TipoEquipo;
import ec.edu.espe.prestamos.exception.RecursoNoEncontradoException;
import ec.edu.espe.prestamos.exception.ReglaNegocioException;
import ec.edu.espe.prestamos.exception.SolicitudInvalidaException;
import ec.edu.espe.prestamos.repository.EquipoRepository;
import ec.edu.espe.prestamos.repository.PrestamoRepository;
import ec.edu.espe.prestamos.repository.TipoEquipoRepository;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final PrestamoRepository prestamoRepository;

    public EquipoService(EquipoRepository equipoRepository, TipoEquipoRepository tipoEquipoRepository,
            PrestamoRepository prestamoRepository) {
        this.equipoRepository = equipoRepository;
        this.tipoEquipoRepository = tipoEquipoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest request) {
        validar(request);
        String codigo = request.codigo().trim();
        if (equipoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un equipo con el codigo '" + codigo + "'");
        }
        Equipo equipo = new Equipo();
        aplicar(equipo, request, codigo);
        return aRespuesta(equipoRepository.save(equipo));
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listar() {
        return equipoRepository.listarConTipo().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listarPorTipo(Long tipoEquipoId) {
        if (!tipoEquipoRepository.existsById(tipoEquipoId)) {
            throw new RecursoNoEncontradoException("No existe el tipo de equipo con id " + tipoEquipoId);
        }
        return equipoRepository.buscarPorTipo(tipoEquipoId).stream().map(this::aRespuesta).toList();
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest request) {
        Equipo equipo = buscar(id);
        validar(request);
        String codigo = request.codigo().trim();
        if (equipoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("Ya existe otro equipo con el codigo '" + codigo + "'");
        }
        aplicar(equipo, request, codigo);
        return aRespuesta(equipoRepository.save(equipo));
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = buscar(id);
        if (prestamoRepository.existsByEquipoId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el equipo '" + equipo.getCodigo() + "' porque tiene prestamos registrados");
        }
        equipoRepository.delete(equipo);
    }

    private Equipo buscar(Long id) {
        return equipoRepository.buscarConTipoPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el equipo con id " + id));
    }

    private void aplicar(Equipo equipo, EquipoRequest request, String codigo) {
        TipoEquipo tipo = tipoEquipoRepository.findById(request.tipoEquipoId()).orElseThrow(
                () -> new RecursoNoEncontradoException(
                        "No existe el tipo de equipo con id " + request.tipoEquipoId()));
        equipo.setCodigo(codigo);
        equipo.setNombre(request.nombre().trim());
        String descripcion = request.descripcion() == null ? null : request.descripcion().trim();
        equipo.setDescripcion(descripcion == null || descripcion.isEmpty() ? null : descripcion);
        equipo.setTipoEquipo(tipo);
    }

    private void validar(EquipoRequest request) {
        if (request == null || esVacio(request.codigo())) {
            throw new SolicitudInvalidaException("El codigo del equipo es obligatorio");
        }
        if (esVacio(request.nombre())) {
            throw new SolicitudInvalidaException("El nombre del equipo es obligatorio");
        }
        if (request.tipoEquipoId() == null) {
            throw new SolicitudInvalidaException("El tipo de equipo es obligatorio");
        }
        if (request.codigo().trim().length() > 50) {
            throw new SolicitudInvalidaException("El codigo no puede superar 50 caracteres");
        }
        if (request.nombre().trim().length() > 150) {
            throw new SolicitudInvalidaException("El nombre no puede superar 150 caracteres");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private EquipoResponse aRespuesta(Equipo equipo) {
        TipoEquipo tipo = equipo.getTipoEquipo();
        return new EquipoResponse(equipo.getId(), equipo.getCodigo(), equipo.getNombre(),
                equipo.getDescripcion(), tipo.getId(), tipo.getNombre());
    }
}
