package rikser123.bundle.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Автоконфигурация бандла
 */

@AutoConfiguration
@EnableFeignClients(basePackages = "rikser123.bundle.feign")
@EnableMethodSecurity
@AutoConfigureBefore({SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@Import({
  JaksonConfig.class,
  SecurityConfig.class,
  ComponentConfig.class,
  FeignConfig.class
})
public class BundleAutoConfiguration {
}
