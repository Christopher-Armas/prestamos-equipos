package ec.edu.espe.prestamos.controller;

import ec.edu.espe.prestamos.dto.ResultadoEjecucion;
import ec.edu.espe.prestamos.service.ComparacionConcurrenciaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concurrencia")
public class ComparacionConcurrenciaController {

    private final ComparacionConcurrenciaService servicio;

    public ComparacionConcurrenciaController(ComparacionConcurrenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/bloqueante")
    public ResultadoEjecucion bloqueante() {
        return servicio.ejecutarBloqueante();
    }

    @GetMapping("/no-bloqueante")
    public ResultadoEjecucion noBloqueante() {
        return servicio.ejecutarNoBloqueante();
    }
}
