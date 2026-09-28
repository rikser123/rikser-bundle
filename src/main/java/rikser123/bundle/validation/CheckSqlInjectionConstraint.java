package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import rikser123.bundle.utils.SqlSafeUtils;

import java.util.List;

@Slf4j
public class CheckSqlInjectionConstraint implements ConstraintValidator<CheckSqlInjection, Object> {
  @Override
  public boolean isValid(Object field, ConstraintValidatorContext context) {
    var isValid = true;
    if (field instanceof List<?> list) {
      isValid = list.stream().allMatch(SqlSafeUtils::isSqlSave);
    } else {
      isValid = SqlSafeUtils.isSqlSave(field);
    }

    return isValid;
  }
}
