package rikser123.bundle.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import rikser123.bundle.dto.User;

/**
 * Интерфейс для взаимодейтвия с польльзователями
 */
public interface UserDetailService {
  /**
   * Метод получения текущего пользователя из контекста  *
   *
   * @return Пользователь
   */
  User getCurrentUser();

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
  User getByUsername(String token);

  /**
   * Метод обновления токена  *
   *
   * @return Токен доступа
   */
  String updateToken(String refreshToken);
}
