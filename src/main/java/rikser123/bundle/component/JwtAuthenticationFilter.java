package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.utils.RikserResponseUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фильтр аутентификации JWT для Spring WebFlux Security
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String HEADER_NAME = "Authorization";
  private final UserDetailService userService;
  private final ObjectMapper objectMapper;
  private final Map<String, Boolean> processedRequests = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var requestId = exchange.getRequest().getId();

    if (processedRequests.putIfAbsent(requestId, true) != null) {
      return chain.filter(exchange);
    }

    return authenticate(exchange, chain)
        .doFinally(
            signal -> {
              processedRequests.remove(requestId);
            });
  }

  /**
   * Обработка json web token
   *
   * @param exchange Запрос
   * @param chain    Фильтры spring security
   */
  private Mono<Void> authenticate(ServerWebExchange exchange, WebFilterChain chain) {
    var authHeader = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);

    if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      return chain.filter(exchange);
    }

    var token = authHeader.substring(BEARER_PREFIX.length());

    return userService
        .getByUsername(token)
        .flatMap(
            userDetails -> {
              var authentication = createAuthenticationToken(userDetails);
              
              return chain
                  .filter(exchange)
                  .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            })
        .switchIfEmpty(chain.filter(exchange))
        .onErrorResume(
            MalformedJwtException.class,
            e ->
                sendErrorResponse(
                    exchange, HttpStatus.BAD_REQUEST, "Некорректный формат JWT токена", e))
        .onErrorResume(
            ExpiredJwtException.class,
            e ->
                sendErrorResponse(
                    exchange, HttpStatus.BAD_REQUEST, "Срок действия токена истек", e))
        .onErrorResume(
            Exception.class,
            e -> sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Ошибка валидации токена", e));
  }

  /**
   * Обработка ошибок токена
   *
   * @param exchange Запрос
   * @param status   Статус запроса
   * @param message  Сообщение об ошибке
   */
  private Mono<Void> sendErrorResponse(
      ServerWebExchange exchange, HttpStatus status, String message, Exception e) {
    log.warn("ERROR JWT token {}", message, e);

    return Mono.fromCallable(
            () -> {
              exchange.getResponse().setStatusCode(status);
              exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

              var responseBody = RikserResponseUtils.createResponse(message, status);

              var jsonText = objectMapper.writer().writeValueAsString(responseBody);
              var bytes = jsonText.getBytes(StandardCharsets.UTF_8);
              return exchange.getResponse().bufferFactory().wrap(bytes);
            })
        .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
        .onErrorResume(
            ex -> {
              log.error("Error writing authentication error response", ex);

              // Fallback: простой текст в случае ошибки
              var errorMessage = "{\"error\":\"Authentication failed\"}";
              var buffer =
                  exchange
                      .getResponse()
                      .bufferFactory()
                      .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

              return exchange.getResponse().writeWith(Mono.just(buffer));
            });
  }

  /**
   * Создает объект аутентификации на основе UserDetails
   */
  private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
    var user = (User) userDetails;
    List<SimpleGrantedAuthority> authorities =
        user.getPrivileges().stream()
            .map(privilege -> new SimpleGrantedAuthority(privilege))
            .toList();

    return new UsernamePasswordAuthenticationToken(
        user,
        null, // credentials - обычно null для JWT
        authorities);
  }
}
