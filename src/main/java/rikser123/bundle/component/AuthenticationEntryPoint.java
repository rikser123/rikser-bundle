package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import rikser123.bundle.utils.RikserResponseUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Обработка ошибок аутентикации
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
    return Mono.defer(
        () -> {
          try {
            var responseBody =
                RikserResponseUtils.createResponse(
                    StringUtils.isNotEmpty(authException.getMessage())
                        ? authException.getMessage()
                        : "Доступ к запрашиваемому ресурсу запрещен",
                    HttpStatus.FORBIDDEN);

            var jsonText = objectMapper.writer().writeValueAsString(responseBody);
            var bytes = jsonText.getBytes(StandardCharsets.UTF_8);

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            Optional.ofNullable(exchange)
                .map(ServerWebExchange::getResponse)
                .filter(response -> !response.isCommitted())
                .map(HttpMessage::getHeaders)
                .ifPresent(
                    headers -> {
                      headers.setContentType(MediaType.APPLICATION_JSON);
                    });

            var buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));

          } catch (Exception e) {
            log.error("Error writing authentication error response", e);

            // Fallback: простой текст в случае ошибки
            var errorMessage = "{\"error\":\"Authentication failed\"}";
            var buffer =
                exchange
                    .getResponse()
                    .bufferFactory()
                    .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

            return exchange.getResponse().writeWith(Mono.just(buffer));
          }
        });
  }
}
