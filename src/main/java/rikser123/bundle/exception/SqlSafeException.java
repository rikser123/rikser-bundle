package rikser123.bundle.exception;

/**
 * Ошибка при валидации на sql инъекции
 *
 */
public class SqlSafeException extends RuntimeException {
    public SqlSafeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlSafeException(String message) {
        super(message);
    }
}
