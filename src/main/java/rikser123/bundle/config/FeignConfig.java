package rikser123.bundle.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@Configuration
@Slf4j
public class FeignConfig {

  @Bean
  public RequestInterceptor authorizationHeaderInterceptor() {
    return template -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String token = null;

      log.warn("auth {}", authentication);
      log.warn("mdc {}", MDC.get("token"));


      if (authentication != null && authentication.getDetails() instanceof String autoToken) {
        token = autoToken;
      }

      if (Objects.isNull(token)) {
        token = MDC.get("token");
      }

      if (!Objects.isNull(token)) {
        template.header("Authorization", "Bearer " + token);
      }
    };
  }
}