package ec.edu.espe.prestamos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.espe.prestamos.entity.TipoEquipo;

public interface TipoEquipoRepository extends JpaRepository<TipoEquipo, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
