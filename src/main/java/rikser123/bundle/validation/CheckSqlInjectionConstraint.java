package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import rikser123.bundle.exception.SqlSafeException;
import rikser123.bundle.utils.SqlSafeUtils;

import java.util.List;

@Slf4j
public class CheckSqlInjectionConstraint implements ConstraintValidator<CheckSqlInjection, Object> {
    @Override
    public boolean isValid(Object field, ConstraintValidatorContext context) {
        var isValid = true;
        if (field instanceof List<?> list) {
            isValid = list.stream().anyMatch(SqlSafeUtils::isSqlSave);
        } else {
            isValid = SqlSafeUtils.isSqlSave(field);
        }

        if (!isValid) {
            var message = "%s содержит sql инъекцию".formatted(field);
            log.warn("{} содержит sql инъекцию", field);
            throw new SqlSafeException(message);
        }

        return isValid;
    }
}
