package rikser123.bundle.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Автоконфигурация бандла
 */

@AutoConfiguration
@EnableFeignClients
@EnableWebSecurity
@EnableMethodSecurity
@Import({
  JaksonConfig.class,
  SecurityConfig.class,
  ComponentConfig.class,
  FeignConfig.class
})
public class BundleAutoConfiguration {
}
