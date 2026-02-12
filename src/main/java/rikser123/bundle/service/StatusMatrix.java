package rikser123.bundle.service;

import java.util.EnumSet;

/**
 * Интерфейс схемы переходов статусов
 */
public interface StatusMatrix<T extends Enum<T>> {

  /**
   * Добавляет переход в матрицу доступных переходов
   *
   * @param src  начальное состояние
   * @param dest конечное состояние
   */
  void addTransition(T src, EnumSet<T> dest);

  /**
   * Возвращает true, если переход статуса из src в dest возможен
   *
   * @param src  текущий статус
   * @param dest целевой статус
   * @return boolean
   */
  boolean isAvailable(T src, T dest);

}

