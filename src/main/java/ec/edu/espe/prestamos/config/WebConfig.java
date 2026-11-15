package ec.edu.espe.prestamos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ec.edu.espe.prestamos.interceptor.RegistroSolicitudesInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RegistroSolicitudesInterceptor registroSolicitudes;

    public WebConfig(RegistroSolicitudesInterceptor registroSolicitudes) {
        this.registroSolicitudes = registroSolicitudes;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(registroSolicitudes).addPathPatterns("/api/**");
    }
}
