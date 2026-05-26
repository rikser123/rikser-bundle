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
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rikser123.bundle.dto.TokenDto;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.utils.RikserResponseUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Фильтр аутентификации JWT для Spring Security (синхронная версия)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String HEADER_NAME = "Authorization";
  public static final String HEADER_REFRESH_NAME = "X-Refresh-Token";
  private final UserDetailService userService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws IOException {
    try {
      authenticate(request, response, filterChain);
    } catch (Exception e) {
      sendErrorResponse(
        response,
        HttpStatus.BAD_REQUEST,
        StringUtils.defaultIfEmpty(e.getMessage(), "Ошибка валидации токена"),
        e
      );
    }
  }

  /**
   * Обработка json web token
   */
  private void authenticate(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    var authHeader = request.getHeader(HEADER_NAME);

    if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    var token = authHeader.substring(BEARER_PREFIX.length());
    var refreshToken = request.getHeader(HEADER_REFRESH_NAME);
    MDC.put("token", token);
    MDC.put("refreshToken", refreshToken);

    try {
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        createSecurityContext(token, refreshToken);
      }

      filterChain.doFilter(request, response);
    } catch (MalformedJwtException e) {
      log.warn("Некорректный формат JWT токена", e);
      throw new IllegalStateException("Некорректный формат JWT токена");
    } catch (ExpiredJwtException e) {
      log.warn("Срок действия токена истек", e);

      try {
        if (StringUtils.isNotEmpty(refreshToken)) {
          var newAccessToken = userService.updateToken(refreshToken);
          createSecurityContext(newAccessToken, refreshToken);
          filterChain.doFilter(request, response);
        } else {
          throw new IllegalStateException("Срок действия токена истек");
        }
      } catch (Exception ex) {
        throw new IllegalStateException("Срок действия токена истек");
      }
    } catch (Exception e) {
      log.warn("Ошибка валидации токена", e);
      throw new IllegalStateException("Ошибка валидации токена");
    } finally {
      MDC.remove("token");
      MDC.remove("refreshToken");
    }
  }

  /**
   * Обработка ошибок токена
   */
  private void sendErrorResponse(
    HttpServletResponse response,
    HttpStatus status,
    String message,
    Exception e
  ) throws IOException {
    log.warn("ERROR JWT token {}", message, e);

    try {
      var responseBody = RikserResponseUtils.createResponse(message, status);
      var jsonText = objectMapper.writer().writeValueAsString(responseBody);
      var bytes = jsonText.getBytes(StandardCharsets.UTF_8);

      response.setStatus(status.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getOutputStream().write(bytes);

    } catch (Exception ex) {
      log.error("Error writing authentication error response", ex);

      // Fallback: простой текст в случае ошибки
      var errorMessage = "{\"error\":\"Authentication failed\"}";
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getOutputStream().write(errorMessage.getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Создает объект аутентификации на основе UserDetails
   */
  private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
    var user = (User) userDetails;
    List<SimpleGrantedAuthority> authorities = user.getPrivileges().stream()
      .map(SimpleGrantedAuthority::new)
      .toList();

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }

  private void createSecurityContext(String token, String refreshToken) {
    var context = SecurityContextHolder.createEmptyContext();
    var userDetails = userService.getByUsername(token);
    var authentication = createAuthenticationToken(userDetails);

    var tokenDto = new TokenDto();
    tokenDto.setAccessToken(token);
    tokenDto.setRefreshToken(refreshToken);
    authentication.setDetails(tokenDto);

    context.setAuthentication(authentication);

    SecurityContextHolder.setContext(context);
  }
}