package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rikser123.bundle.utils.FieldExtractor;
import rikser123.bundle.utils.SqlSafeUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConstraintValidator {
  private final Validator validator;
  private final ObjectMapper objectMapper;

  /**
   * Валидация объекта
   *
   * @param dto Объект на валидацию
   */
  public <T> void validate(T dto) {
    validateRequest(dto);
  }

  /**
   * Получение всех ошибочных сообщений по валидации
   *
   * @param violations Объекты ошибок
   * @return Строка ошибок
   */
  private <T> String getViolationDescription(Set<ConstraintViolation<T>> violations) {
    return violations.stream()
      .map(ConstraintViolation::getMessage)
      .collect(Collectors.joining(";" + System.lineSeparator()));
  }

  /**
   * Валидация объекта
   *
   * @param dto Объект на валидацию
   */
  private <T> void validateRequest(T dto) {
    Set<ConstraintViolation<T>> violations = validator.validate(dto);
    if (!violations.isEmpty()) {
      String warn = getViolationDescription(violations);
      log.warn("ERROR: not valid fields:\n{}", warn);
      throw new ValidationException("not valid fields: " + warn);
    }

    var sqlValidationFields = FieldExtractor.extractToMap(dto, objectMapper);

    sqlValidationFields.forEach((key, value) -> {
      var isSqlSafe = SqlSafeUtils.isSqlSave(value);
      if (!isSqlSafe) {
        log.warn("CheckInjection Error - not valid '{}': {}", key, value);
        throw new ValidationException("CheckInjection Error");
      }
    });
  }

}