package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rikser123.bundle.exception.SqlSafeException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckSqlInjectionConstraintTest {
  private ConstraintValidatorContext context;
  private CheckSqlInjection annotation;
  private CheckSqlInjectionConstraint validator;

  @BeforeEach
  void init() {
    context = Mockito.mock(ConstraintValidatorContext.class);
    annotation = new ConstraintAnnotationDescriptor.Builder<>(CheckSqlInjection.class).build().getAnnotation();

    validator = new CheckSqlInjectionConstraint();
    validator.initialize(annotation);
  }

  @Test
  void sqlSafe() {
    var given = "DENISOV";
    var result = validator.isValid(given, context);
    assertTrue(result);
  }

  @Test
  void sqlUnsafe() {
    var given = "(xxe)";
    assertThrows(SqlSafeException.class, () -> validator.isValid(given, context));
  }
}
