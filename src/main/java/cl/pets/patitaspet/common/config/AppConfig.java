package cl.pets.patitaspet.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "cl.pets.patitaspet.user",
        "cl.pets.patitaspet.pet",
        "cl.pets.patitaspet.appointment",
        "cl.pets.patitaspet.common"
})
public class AppConfig {
    // Configuraciones globales de la aplicación
}