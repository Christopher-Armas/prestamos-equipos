package ec.edu.espe.prestamos.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.edu.espe.prestamos.entity.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    boolean existsByEquipoId(Long equipoId);

    boolean existsByUsuarioId(Long usuarioId);

    @Query("select p from Prestamo p join fetch p.equipo join fetch p.usuario "
            + "where p.usuario.id = :usuarioId order by p.fechaPrestamo desc, p.id desc")
    List<Prestamo> buscarPorUsuario(@Param("usuarioId") Long usuarioId);

    // Un equipo esta prestado si tiene un prestamo en estado PRESTADO.
    boolean existsByEquipoIdAndEstado(Long equipoId, String estado);

    // Regla: un usuario con prestamos vencidos sin devolver no puede pedir otro.
    boolean existsByUsuarioIdAndEstadoAndFechaDevolucionPrevistaBefore(Long usuarioId, String estado,
            LocalDate fecha);

    long countByUsuarioIdAndEstado(Long usuarioId, String estado);

    Optional<Prestamo> findFirstByEquipoIdAndEstado(Long equipoId, String estado);

    @Query("select p from Prestamo p join fetch p.equipo join fetch p.usuario where p.id = :id")
    Optional<Prestamo> buscarConDetallePorId(@Param("id") Long id);

    @Query("select p from Prestamo p join fetch p.equipo join fetch p.usuario "
            + "order by p.fechaPrestamo desc, p.id desc")
    List<Prestamo> listarConDetalle();

    @Query("select p from Prestamo p join fetch p.equipo join fetch p.usuario "
            + "where p.estado = :estado order by p.fechaPrestamo desc, p.id desc")
    List<Prestamo> listarPorEstado(@Param("estado") String estado);

    // Consulta personalizada: prestamos vencidos (no devueltos y con fecha prevista pasada).
    @Query("select p from Prestamo p join fetch p.equipo join fetch p.usuario "
            + "where p.estado = 'PRESTADO' and p.fechaDevolucionPrevista < :hoy "
            + "order by p.fechaDevolucionPrevista, p.id")
    List<Prestamo> buscarVencidos(@Param("hoy") LocalDate hoy);
}
