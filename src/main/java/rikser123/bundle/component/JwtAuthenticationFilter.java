package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String HEADER_NAME = "Authorization";
  private static final String HEADER_REFRESH_NAME = "X-Refresh-Token";
  private static final String INVALID_FORMAT_TOKEN_MESSAGE = "Некорректный формат JWT токена";
  private static final String EXPIRED_TOKEN_MESSAGE = "Срок действия токена истек";
  private static final String VALIDATION_ERROR_TOKEN_MESSAGE = "Ошибка валидации токена";

  private final UserDetailService userService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    authenticate(request, response, filterChain);
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
      log.warn(INVALID_FORMAT_TOKEN_MESSAGE, e);
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, INVALID_FORMAT_TOKEN_MESSAGE, e);
    } catch (ExpiredJwtException e) {
      log.warn(EXPIRED_TOKEN_MESSAGE, e);
      updateAccessToken(refreshToken, request, response, filterChain, e);
    } catch (FeignException.BadRequest | FeignException.InternalServerError ex) {
      var body = ex.contentUTF8();
      var node = objectMapper.readTree(body);
      var message = node.get("message").asText();

      if (INVALID_FORMAT_TOKEN_MESSAGE.equals(message)) {
        log.warn(INVALID_FORMAT_TOKEN_MESSAGE, ex);
        sendErrorResponse(response, HttpStatus.BAD_REQUEST, INVALID_FORMAT_TOKEN_MESSAGE, ex);
      } else if (EXPIRED_TOKEN_MESSAGE.equals(message) || message.startsWith("JWT expired")) {
        log.warn(EXPIRED_TOKEN_MESSAGE, ex);
        updateAccessToken(refreshToken, request, response, filterChain, ex);
      } else {
        throw new IllegalStateException(VALIDATION_ERROR_TOKEN_MESSAGE);
      }
    } catch (Exception e) {
      log.warn(VALIDATION_ERROR_TOKEN_MESSAGE, e);
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, VALIDATION_ERROR_TOKEN_MESSAGE, e);
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

  private void updateAccessToken(
    String refreshToken,
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain,
    Exception ex
  ) throws IOException {
    try {
      if (StringUtils.isNotEmpty(refreshToken)) {
        var newAccessToken = userService.updateToken(refreshToken);
        MDC.put("token", newAccessToken);
        createSecurityContext(newAccessToken, refreshToken);
        filterChain.doFilter(request, response);
      } else {
        sendErrorResponse(response, HttpStatus.BAD_REQUEST, EXPIRED_TOKEN_MESSAGE, ex);
      }
    } catch (Exception e) {
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, EXPIRED_TOKEN_MESSAGE, e);
    }
  }
}