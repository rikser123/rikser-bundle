package rikser123.bundle.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * Интерфейс для взаимодейтвия с польльзователями
 */
public interface UserDetailService {
  /**
   * Метод получения текущего пользователя из контекста  *
   *
   * @return Пользователь
   */
  Mono<UserDetails> getCurrentUser();

  /**
   * Метод получения сервиса пользователей для контекста безопасности  *
   *
   * @return Сервис пользователей
   */
  ReactiveUserDetailsService userDetailsService();

  /**
   * Метод получения пользователя из security  *
   *
   * @param token Токен безопасности из запроса
   * @return Сервис пользователей
   */
  Mono<UserDetails> getByUsername(String token);
}
