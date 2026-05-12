package rikser123.bundle.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {

  @Bean
  public RequestInterceptor authorizationHeaderInterceptor() {
    return template -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication != null && authentication.getDetails() instanceof String token) {
        template.header("Authorization", "Bearer " + token);
      }
    };
  }
}