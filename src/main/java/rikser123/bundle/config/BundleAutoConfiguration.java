package rikser123.bundle.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import reactivefeign.spring.config.EnableReactiveFeignClients;

/**
 * Автоконфигурация бандла
 */

@AutoConfiguration
@EnableReactiveFeignClients
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import({SecurityConfig.class, ComponentConfig.class})
public class BundleAutoConfiguration {
}
