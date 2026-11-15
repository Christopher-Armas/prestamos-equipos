package ec.edu.espe.prestamos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.prestamos.dto.DisponibilidadRespuesta;
import ec.edu.espe.prestamos.dto.EquipoResponse;
import ec.edu.espe.prestamos.dto.PrestamoRespuesta;
import ec.edu.espe.prestamos.dto.PrestamoSolicitud;
import ec.edu.espe.prestamos.entity.Equipo;
import ec.edu.espe.prestamos.entity.Prestamo;
import ec.edu.espe.prestamos.entity.Usuario;
import ec.edu.espe.prestamos.exception.RecursoNoEncontradoException;
import ec.edu.espe.prestamos.exception.ReglaNegocioException;
import ec.edu.espe.prestamos.exception.SolicitudInvalidaException;
import ec.edu.espe.prestamos.repository.EquipoRepository;
import ec.edu.espe.prestamos.repository.PrestamoRepository;
import ec.edu.espe.prestamos.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class PrestamoService {

    public static final int MAX_DIAS_PRESTAMO = 30;
    public static final int MAX_PRESTAMOS_ACTIVOS = 3;

    private final PrestamoRepository prestamos;
    private final EquipoRepository equipos;
    private final UsuarioRepository usuarios;

    public PrestamoService(PrestamoRepository prestamos, EquipoRepository equipos, UsuarioRepository usuarios) {
        this.prestamos = prestamos;
        this.equipos = equipos;
        this.usuarios = usuarios;
    }

    // Proceso 3: registrar un prestamo aplicando las reglas de negocio.
    @Transactional
    public PrestamoRespuesta registrar(PrestamoSolicitud solicitud) {
        LocalDate hoy = LocalDate.now();
        Equipo equipo = equipos.findById(solicitud.equipoId()).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el equipo con id " + solicitud.equipoId()));
        Usuario usuario = usuarios.findById(solicitud.usuarioId()).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe un usuario con id " + solicitud.usuarioId()));

        LocalDate prevista = solicitud.fechaDevolucionPrevista();
        if (prevista.isBefore(hoy)) {
            throw new SolicitudInvalidaException("La fecha de devolucion prevista no puede ser anterior a hoy");
        }
        if (prevista.isAfter(hoy.plusDays(MAX_DIAS_PRESTAMO))) {
            throw new SolicitudInvalidaException(
                    "El prestamo no puede durar mas de " + MAX_DIAS_PRESTAMO + " dias");
        }
        if (prestamos.existsByEquipoIdAndEstado(equipo.getId(), Prestamo.ESTADO_PRESTADO)) {
            throw new ReglaNegocioException("El equipo '" + equipo.getCodigo() + "' no esta disponible");
        }
        if (prestamos.existsByUsuarioIdAndEstadoAndFechaDevolucionPrevistaBefore(
                usuario.getId(), Prestamo.ESTADO_PRESTADO, hoy)) {
            throw new ReglaNegocioException("El usuario tiene prestamos vencidos sin devolver");
        }
        if (prestamos.countByUsuarioIdAndEstado(usuario.getId(), Prestamo.ESTADO_PRESTADO) >= MAX_PRESTAMOS_ACTIVOS) {
            throw new ReglaNegocioException(
                    "El usuario ya tiene " + MAX_PRESTAMOS_ACTIVOS + " prestamos activos");
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setEquipo(equipo);
        prestamo.setUsuario(usuario);
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaDevolucionPrevista(prevista);
        prestamo.setEstado(Prestamo.ESTADO_PRESTADO);
        return PrestamoRespuesta.desde(prestamos.saveAndFlush(prestamo), hoy);
    }

    // Proceso 4: registrar la devolucion de un equipo.
    @Transactional
    public PrestamoRespuesta devolver(Long id) {
        Prestamo prestamo = buscar(id);
        if (Prestamo.ESTADO_DEVUELTO.equals(prestamo.getEstado())) {
            throw new ReglaNegocioException("El prestamo " + id + " ya fue devuelto");
        }
        LocalDate hoy = LocalDate.now();
        prestamo.setFechaDevolucionReal(hoy);
        prestamo.setEstado(Prestamo.ESTADO_DEVUELTO);
        return PrestamoRespuesta.desde(prestamos.saveAndFlush(prestamo), hoy);
    }

    public List<PrestamoRespuesta> listar(String estado) {
        List<Prestamo> resultado;
        if (estado == null || estado.isBlank()) {
            resultado = prestamos.listarConDetalle();
        } else {
            String normalizado = estado.trim().toUpperCase(Locale.ROOT);
            if (!Prestamo.ESTADO_PRESTADO.equals(normalizado) && !Prestamo.ESTADO_DEVUELTO.equals(normalizado)) {
                throw new SolicitudInvalidaException("El estado debe ser PRESTADO o DEVUELTO");
            }
            resultado = prestamos.listarPorEstado(normalizado);
        }
        LocalDate hoy = LocalDate.now();
        return resultado.stream().map(p -> PrestamoRespuesta.desde(p, hoy)).toList();
    }

    public PrestamoRespuesta obtener(Long id) {
        return PrestamoRespuesta.desde(buscar(id), LocalDate.now());
    }

    // Proceso 5: disponibilidad de equipos.
    public DisponibilidadRespuesta disponibilidad(Long equipoId) {
        Equipo equipo = equipos.findById(equipoId).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el equipo con id " + equipoId));
        return prestamos.findFirstByEquipoIdAndEstado(equipoId, Prestamo.ESTADO_PRESTADO)
                .map(p -> new DisponibilidadRespuesta(equipoId, equipo.getCodigo(), false, p.getId(),
                        p.getFechaDevolucionPrevista()))
                .orElseGet(() -> new DisponibilidadRespuesta(equipoId, equipo.getCodigo(), true, null, null));
    }

    public List<EquipoResponse> equiposDisponibles() {
        return equipos.listarDisponibles().stream()
                .map(e -> new EquipoResponse(e.getId(), e.getCodigo(), e.getNombre(), e.getDescripcion(),
                        e.getTipoEquipo().getId(), e.getTipoEquipo().getNombre()))
                .toList();
    }

    // Proceso 6: prestamos vencidos.
    public List<PrestamoRespuesta> vencidos() {
        LocalDate hoy = LocalDate.now();
        return prestamos.buscarVencidos(hoy).stream().map(p -> PrestamoRespuesta.desde(p, hoy)).toList();
    }

    private Prestamo buscar(Long id) {
        return prestamos.buscarConDetallePorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el prestamo con id " + id));
    }
}
