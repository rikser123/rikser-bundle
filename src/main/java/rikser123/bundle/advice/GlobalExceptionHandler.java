package rikser123.bundle.advice;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.exception.SqlSafeException;
import rikser123.bundle.utils.RikserResponseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
  public ResponseEntity<RikserResponseItem> handleValidationException(
    MethodArgumentNotValidException exception
  ) {
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

    var response = RikserResponseUtils.createResponse(HttpStatus.BAD_REQUEST, errors, null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<RikserResponseItem> handleRuntimeException(RuntimeException exception) {
    log.error("Internal server error", exception);
    var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<RikserResponseItem> handleAccessDeniedException(
    AccessDeniedException exception
  ) {
    log.warn("access forbidden", exception);
    var response = RikserResponseUtils.createResponse("Доступ к запрашиваемому ресурсу запрещен", HttpStatus.FORBIDDEN);
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(EntityExistsException.class)
  public ResponseEntity<RikserResponseItem> handleEntityExistsException(
    EntityExistsException exception
  ) {
    log.warn("entity exists", exception);
    var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<RikserResponseItem> handleEntityNotFoundException(
    EntityNotFoundException exception
  ) {
    log.warn("entity not found", exception);
    var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(SqlSafeException.class)
  public ResponseEntity<RikserResponseItem> handleSqlSaveException(SqlSafeException exception) {
    log.warn("sql injection");
    var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<RikserResponseItem> handleMissingParam(MissingServletRequestParameterException exception) {
    log.warn("missing required param");
    var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}