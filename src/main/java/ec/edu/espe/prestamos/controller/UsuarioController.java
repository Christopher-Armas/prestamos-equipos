package ec.edu.espe.prestamos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.prestamos.dto.PrestamoConsultaRespuesta;
import ec.edu.espe.prestamos.dto.UsuarioRespuesta;
import ec.edu.espe.prestamos.dto.UsuarioSolicitud;
import ec.edu.espe.prestamos.service.ConsultaPrestamoService;
import ec.edu.espe.prestamos.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarios;
    private final ConsultaPrestamoService consultas;

    public UsuarioController(UsuarioService usuarios, ConsultaPrestamoService consultas) {
        this.usuarios = usuarios;
        this.consultas = consultas;
    }

    @PostMapping
    public ResponseEntity<UsuarioRespuesta> crear(@Valid @RequestBody UsuarioSolicitud solicitud) {
        UsuarioRespuesta usuario = usuarios.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id())).body(usuario);
    }

    @GetMapping
    public List<UsuarioRespuesta> listar(@RequestParam(required = false) String nombre) {
        return usuarios.listar(nombre);
    }

    @GetMapping("/por-correo")
    public UsuarioRespuesta porCorreo(@RequestParam String correo) {
        return usuarios.buscarPorCorreo(correo);
    }

    @GetMapping("/{id}")
    public UsuarioRespuesta porId(@PathVariable Long id) {
        return usuarios.buscarPorId(id);
    }

    @GetMapping("/{id}/prestamos")
    public List<PrestamoConsultaRespuesta> prestamosPorUsuario(@PathVariable Long id) {
        return consultas.porUsuario(id);
    }

    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioSolicitud solicitud) {
        return usuarios.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarios.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
