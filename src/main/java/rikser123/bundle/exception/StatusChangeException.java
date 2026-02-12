package rikser123.bundle.exception;

/**
 * Ошибка при невозможности изменить статус
 */
public class StatusChangeException extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "Переход на новый статус невозможен";

  public StatusChangeException() {
    super(DEFAULT_MESSAGE);
  }
}

