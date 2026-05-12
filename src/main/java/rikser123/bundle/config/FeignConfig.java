package rikser123.bundle.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rikser123.bundle.dto.TokenDto;

import java.util.Objects;

@Configuration
@Slf4j
public class FeignConfig {

  @Bean
  public RequestInterceptor authorizationHeaderInterceptor() {
    return template -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String token = null;
      String refreshToken = null;

      if (authentication != null && authentication.getDetails() instanceof TokenDto tokenDto) {
        token = tokenDto.getAccessToken();
        refreshToken = tokenDto.getRefreshToken();
      }

      if (Objects.isNull(token)) {
        token = MDC.get("token");
        refreshToken = MDC.get("refreshToken");
      }

      if (!Objects.isNull(token)) {
        template.header("Authorization", "Bearer " + token);
      }

      if (!Objects.isNull(refreshToken)) {
        template.header("X-Refresh-Token", refreshToken);
      }
    };
  }
}