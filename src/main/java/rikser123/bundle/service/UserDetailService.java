package rikser123.bundle.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Интерфейс для взаимодейтвия с польльзователями
 */
public interface UserDetailService {
  /**
   * Метод получения текущего пользователя из контекста  *
   *
   * @return Пользователь
   */
  UserDetails getCurrentUser();

  /**
   * Метод получения сервиса пользователей для контекста безопасности  *
   *
   * @return Сервис пользователей
   */
  UserDetailsService userDetailsService();

  /**
   * Метод получения пользователя из security  *
   *
   * @param token Токен безопасности из запроса
   * @return Сервис пользователей
   */
  UserDetails getByUsername(String token);
}
