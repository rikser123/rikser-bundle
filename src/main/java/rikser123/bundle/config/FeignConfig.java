package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.webclient.WebReactiveFeign;
import rikser123.bundle.component.ReactiveFeignLoggingListener;
import rikser123.bundle.feign.SecurityClient;

@Configuration
@EnableConfigurationProperties(MaskedProperties.class)
public class FeignConfig {
  @Value("${bundle.security.service.url}")
  private String securityServiceUrl;

  @Bean
  public ReactiveFeignLoggingListener reactiveLoggerListener(ObjectMapper objectMapper, MaskedProperties maskedProperties) {
    return new ReactiveFeignLoggingListener(maskedProperties, objectMapper);
  }

  @Bean
  public SecurityClient securityClient(ReactiveFeignLoggingListener logger) {
    return WebReactiveFeign.<SecurityClient>builder()
      .addLoggerListener(logger)
      .target(SecurityClient.class, securityServiceUrl);
  }
}
