package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IsEqualConstraint implements ConstraintValidator<IsEqual, Object> {
    private String firstField;
    private String secondField;

    @Override
    public void initialize(IsEqual annotation) {
        firstField = annotation.firstField();
        secondField = annotation.secondField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        var fields = object.getClass().getDeclaredFields();
        Object firstFieldValue = new Object();
        Object secondFieldValue = new Object();

        try {
            for (var field : fields) {
                if (field.getName().equals(firstField)) {
                    field.setAccessible(true);
                    firstFieldValue = field.get(object);
                }

                if (field.getName().equals(secondField)) {
                    field.setAccessible(true);
                    secondFieldValue = field.get(object);
                }
            }
        } catch (Exception e) {
            log.warn("Error trying to get fields value {} {}", firstField, secondField, e);
            return false;
        }

        var isValid = firstFieldValue.equals(secondFieldValue);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(secondField)
                    .addConstraintViolation();
        }

        return isValid;
    }
}
