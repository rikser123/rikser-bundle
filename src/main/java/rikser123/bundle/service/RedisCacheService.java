package rikser123.bundle.service;

import java.util.Optional;

public interface RedisCacheService {

  /**
   * Сохранить значение в кэш
   *
   * @param key   ключ
   * @param value значение
   */
  <T> void put(String key, T value);

  /**
   * Получить значение из кэша
   *
   * @param key ключ
   * @return Optional с значением или пустой
   */
  <T> Optional<T> get(String key, Class<T> parsedCLass);

  /**
   * Удалить значение из кэша
   *
   * @param key ключ
   */
  void delete(String key);

  /**
   * Проверить наличие ключа в кэше
   *
   * @param key ключ
   * @return true если ключ существует
   */
  boolean contains(String key);
}