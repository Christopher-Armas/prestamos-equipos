package ec.edu.espe.prestamos.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP: audita todos los metodos de la capa de servicios sin modificar su codigo.
 * Registra el tiempo de ejecucion de cada metodo y las excepciones de negocio que lanzan.
 */
@Aspect
@Component
public class AuditoriaServiciosAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiciosAspect.class);
    private static final long UMBRAL_LENTO_MS = 500;

    @Pointcut("within(ec.edu.espe.prestamos.service..*)")
    public void capaServicios() {
    }

    @Around("capaServicios()")
    public Object medirTiempo(ProceedingJoinPoint punto) throws Throwable {
        long inicio = System.nanoTime();
        try {
            return punto.proceed();
        } finally {
            long milisegundos = (System.nanoTime() - inicio) / 1_000_000;
            if (milisegundos > UMBRAL_LENTO_MS) {
                log.warn("[AOP] {} tardo {} ms (lento)", nombre(punto), milisegundos);
            } else {
                log.info("[AOP] {} ejecutado en {} ms", nombre(punto), milisegundos);
            }
        }
    }

    @AfterThrowing(pointcut = "capaServicios()", throwing = "ex")
    public void registrarExcepcion(JoinPoint punto, Exception ex) {
        log.warn("[AOP] {} lanzo {}: {}", nombre(punto), ex.getClass().getSimpleName(), ex.getMessage());
    }

    private static String nombre(JoinPoint punto) {
        return punto.getSignature().getDeclaringType().getSimpleName() + "." + punto.getSignature().getName();
    }
}
