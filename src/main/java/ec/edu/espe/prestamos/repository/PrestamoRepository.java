package ec.edu.espe.prestamos.repository;

import java.util.List;

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
}
