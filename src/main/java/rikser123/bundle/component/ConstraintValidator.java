package rikser123.bundle.component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConstraintValidator {
  private final Validator validator;

  /**
   * Валидация объекта
   *
   * @param dto Объект на валидацию
   */
  public <T> void validate(T dto) {
    validateRequest(dto);
  }

  /**
   * Валидация объекта на проверки
   *
   * @param dto Объект на валидацию
   */
  private <T> void validateRequest(T dto) {
    var violations = validator.validate(dto);
    if (!violations.isEmpty()) {
      var warn = getViolationDescription(violations);
      log.warn("ERROR: not valid fields:\n{}", warn);
      throw new ValidationException("not valid fields: " + warn);
    }
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
}