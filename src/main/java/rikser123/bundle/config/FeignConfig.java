package rikser123.bundle.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@Slf4j
public class FeignConfig {

  @Bean
  public RequestInterceptor authorizationHeaderInterceptor() {
    return template -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      log.warn("auth {}", authentication);

      if (authentication != null && authentication.getDetails() instanceof String token) {
        template.header("Authorization", "Bearer " + token);
      }
    };
  }
}