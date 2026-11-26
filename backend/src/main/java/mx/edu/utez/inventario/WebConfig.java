package mx.edu.utez.inventario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = Path.of(uploadDir).toAbsolutePath().toUri().toString();
        if (!ubicacion.endsWith("/")) ubicacion += "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(ubicacion);
    }
}
