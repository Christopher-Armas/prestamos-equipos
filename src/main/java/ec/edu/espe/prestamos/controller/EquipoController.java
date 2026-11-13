package ec.edu.espe.prestamos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.prestamos.dto.EquipoRequest;
import ec.edu.espe.prestamos.dto.EquipoResponse;
import ec.edu.espe.prestamos.service.EquipoService;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@RequestBody EquipoRequest request) {
        return equipoService.crear(request);
    }

    @GetMapping
    public List<EquipoResponse> listar() {
        return equipoService.listar();
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable Long id) {
        return equipoService.obtener(id);
    }

    @GetMapping("/tipo/{tipoId}")
    public List<EquipoResponse> listarPorTipo(@PathVariable Long tipoId) {
        return equipoService.listarPorTipo(tipoId);
    }

    @PutMapping("/{id}")
    public EquipoResponse actualizar(@PathVariable Long id, @RequestBody EquipoRequest request) {
        return equipoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
    }
}
