package rikser123.bundle.advice;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.exception.SqlSafeException;
import rikser123.bundle.utils.RikserResponseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Класс для работы с глобальными исключениями
 */

@RestControllerAdvice
@Slf4j
@Order(-2)
@NoArgsConstructor
public class GlobalExceptionHandler {

  private static String getFieldLastPart(String field) {
    var fieldParts = field.split("\\.");
    return fieldParts[fieldParts.length - 1];
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RikserResponseItem handleValidationException(MethodArgumentNotValidException exception) {
    var errors = new HashMap<String, List<String>>();

    exception.getBindingResult().getAllErrors().forEach(error -> {
      var fieldName = ((FieldError) error).getField();
      var message = error.getDefaultMessage();

      var fieldLastPart = getFieldLastPart(fieldName);
      errors.computeIfPresent(fieldLastPart, (key, value) -> {
        value.add(message);
        return value;
      });
      errors.putIfAbsent(fieldLastPart, new ArrayList<>(List.of(message)));
    });

    return RikserResponseUtils.createResponse(HttpStatus.BAD_REQUEST, errors, null);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public RikserResponseItem handleRuntimeException(RuntimeException exception) {
    log.error("Internal server error", exception);
    return RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public RikserResponseItem handleAccessDeniedException(AccessDeniedException exception) {
    log.warn("access forbidden", exception);
    return RikserResponseUtils.createResponse("Доступ к запрашиваемому ресурсу запрещен", HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(EntityExistsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RikserResponseItem handleEntityExistsException(EntityExistsException exception) {
    log.warn("entity exists", exception);
    return RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RikserResponseItem handleEntityNotFoundException(EntityNotFoundException exception) {
    log.warn("entity not found", exception);
    return RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(SqlSafeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RikserResponseItem handleSqlSaveException(SqlSafeException exception) {
    log.warn("sql injection");
    return RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }
}