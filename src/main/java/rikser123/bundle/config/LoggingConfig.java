package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.component.LoggingFilter;

@Configuration
@EnableConfigurationProperties(MaskedProperties.class)
public class LoggingConfig {

  @Bean
  public LoggingFilter loggingFilter(ObjectMapper objectMapper, MaskedProperties maskedProperties) {
    return new LoggingFilter(objectMapper, maskedProperties);
  }
}
