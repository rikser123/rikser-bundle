package rikser123.bundle.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsEqualConstraint.class)
public @interface IsEqual {
    String firstField();
    String secondField();
    String message() default "Значения полей не совпадают";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
