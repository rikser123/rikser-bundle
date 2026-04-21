package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.utils.RikserResponseUtils;

import java.io.IOException;
import java.util.List;

/**
 * Фильтр аутентификации JWT для Spring WebFlux Security
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String HEADER_NAME = "Authorization";
  private final UserDetailService userService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain
  ) throws ServletException, IOException {
    authenticate(request, response, chain);
  }

  /**
   * Обработка json web token
   *
   * @param request  Запрос
   * @param response Ответ
   * @param chain    Фильтры spring security
   */
  private void authenticate(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain
  ) throws IOException, ServletException {
    var authHeader = request.getHeader(HEADER_NAME);

    if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      chain.doFilter(request, response);
      return;
    }

    var token = authHeader.substring(BEARER_PREFIX.length());

    try {
      var userDetails = userService.getByUsername(token);
      var authentication = createAuthenticationToken(userDetails);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      chain.doFilter(request, response);
    } catch (MalformedJwtException e) {
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Некорректный формат JWT токена", e);
    } catch (ExpiredJwtException e) {
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Срок действия токена истек", e);
    } catch (Exception e) {
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Ошибка валидации токена", e);
    }
  }

  /**
   * Обработка ошибок токена
   *
   * @param response Запрос
   * @param status   Статус запроса
   * @param message  Сообщение об ошибке
   */
  private void sendErrorResponse(
    HttpServletResponse response,
    HttpStatus status,
    String message,
    Exception e
  ) throws IOException {
    log.warn("ERROR JWT token {}", message, e);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    try {
      var responseBody = RikserResponseUtils.createResponse(message, status);
      var jsonText = objectMapper.writer().writeValueAsString(responseBody);
      response.getWriter().write(jsonText);
    } catch (Exception ex) {
      log.error("Error writing authentication error response", ex);
      response.getWriter().write("{\"error\":\"Authentication failed\"}");
    }
  }

  /**
   * Создает объект аутентификации на основе UserDetails
   */
  private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
    var user = (User) userDetails;
    List<SimpleGrantedAuthority> authorities = user
      .getPrivileges()
      .stream()
      .map(SimpleGrantedAuthority::new)
      .toList();

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
