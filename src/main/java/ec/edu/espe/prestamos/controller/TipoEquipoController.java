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

import ec.edu.espe.prestamos.dto.TipoEquipoRequest;
import ec.edu.espe.prestamos.dto.TipoEquipoResponse;
import ec.edu.espe.prestamos.service.TipoEquipoService;

@RestController
@RequestMapping("/api/tipos-equipo")
public class TipoEquipoController {

    private final TipoEquipoService tipoEquipoService;

    public TipoEquipoController(TipoEquipoService tipoEquipoService) {
        this.tipoEquipoService = tipoEquipoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoEquipoResponse crear(@RequestBody TipoEquipoRequest request) {
        return tipoEquipoService.crear(request);
    }

    @GetMapping
    public List<TipoEquipoResponse> listar() {
        return tipoEquipoService.listar();
    }

    @GetMapping("/{id}")
    public TipoEquipoResponse obtener(@PathVariable Long id) {
        return tipoEquipoService.obtener(id);
    }

    @PutMapping("/{id}")
    public TipoEquipoResponse actualizar(@PathVariable Long id, @RequestBody TipoEquipoRequest request) {
        return tipoEquipoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tipoEquipoService.eliminar(id);
    }
}
