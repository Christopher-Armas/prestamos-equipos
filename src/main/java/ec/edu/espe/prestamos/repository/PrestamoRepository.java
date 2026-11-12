package ec.edu.espe.prestamos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.espe.prestamos.entity.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    boolean existsByEquipoId(Long equipoId);
}
