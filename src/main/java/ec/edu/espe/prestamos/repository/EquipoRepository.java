package ec.edu.espe.prestamos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.edu.espe.prestamos.entity.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByTipoEquipoId(Long tipoEquipoId);

    // Consulta personalizada: todos los equipos con su tipo cargado en una sola consulta.
    @Query("select e from Equipo e join fetch e.tipoEquipo order by e.nombre")
    List<Equipo> listarConTipo();

    // Consulta personalizada: clasificacion de equipos por tipo.
    @Query("select e from Equipo e join fetch e.tipoEquipo t where t.id = :tipoId order by e.nombre")
    List<Equipo> buscarPorTipo(@Param("tipoId") Long tipoId);

    // Consulta personalizada: equipos que no tienen un prestamo activo.
    @Query("select e from Equipo e join fetch e.tipoEquipo where not exists "
            + "(select p.id from Prestamo p where p.equipo = e and p.estado = 'PRESTADO') order by e.nombre")
    List<Equipo> listarDisponibles();

    @Query("select e from Equipo e join fetch e.tipoEquipo where e.id = :id")
    Optional<Equipo> buscarConTipoPorId(@Param("id") Long id);
}
