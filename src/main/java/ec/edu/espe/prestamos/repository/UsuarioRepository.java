package ec.edu.espe.prestamos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.espe.prestamos.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
