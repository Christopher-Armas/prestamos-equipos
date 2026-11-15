package ec.edu.espe.prestamos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.prestamos.dto.DisponibilidadRespuesta;
import ec.edu.espe.prestamos.dto.EquipoResponse;
import ec.edu.espe.prestamos.dto.PrestamoRespuesta;
import ec.edu.espe.prestamos.dto.PrestamoSolicitud;
import ec.edu.espe.prestamos.service.PrestamoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamos;

    public PrestamoController(PrestamoService prestamos) {
        this.prestamos = prestamos;
    }

    @PostMapping
    public ResponseEntity<PrestamoRespuesta> registrar(@Valid @RequestBody PrestamoSolicitud solicitud) {
        PrestamoRespuesta prestamo = prestamos.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/prestamos/" + prestamo.id())).body(prestamo);
    }

    @GetMapping
    public List<PrestamoRespuesta> listar(@RequestParam(required = false) String estado) {
        return prestamos.listar(estado);
    }

    @GetMapping("/vencidos")
    public List<PrestamoRespuesta> vencidos() {
        return prestamos.vencidos();
    }

    @GetMapping("/equipos-disponibles")
    public List<EquipoResponse> equiposDisponibles() {
        return prestamos.equiposDisponibles();
    }

    @GetMapping("/disponibilidad/{equipoId}")
    public DisponibilidadRespuesta disponibilidad(@PathVariable Long equipoId) {
        return prestamos.disponibilidad(equipoId);
    }

    @GetMapping("/{id}")
    public PrestamoRespuesta obtener(@PathVariable Long id) {
        return prestamos.obtener(id);
    }

    @PutMapping("/{id}/devolucion")
    public PrestamoRespuesta devolver(@PathVariable Long id) {
        return prestamos.devolver(id);
    }
}
