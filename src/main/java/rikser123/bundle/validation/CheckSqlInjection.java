package rikser123.bundle.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CheckSqlInjectionConstraint.class)
public @interface CheckSqlInjection {
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String message() default "Поле содержит sql инъекции!";
}
