package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class IsStrongPasswordConstraintTest {
    private ConstraintValidatorContext context;
    private IsStrongPassword annotation;
    private IsStrongPasswordConstraint validator;

    @BeforeEach
    void init() {
        context = Mockito.mock(ConstraintValidatorContext.class);
        annotation = new ConstraintAnnotationDescriptor.Builder<>(IsStrongPassword.class).build().getAnnotation();

        validator = new IsStrongPasswordConstraint();
        validator.initialize(annotation);
    }

    @ParameterizedTest()
    @MethodSource("weekPasswords")
    void shouldCatchWeekPassword(String password) {
        var result = validator.isValid(password, context);

        assertFalse(result);
    }

    @ParameterizedTest
    @MethodSource("strongPasswords")
    void shouldHandleStrongPassword(String password) {
        var result = validator.isValid(password, context);

        assertTrue(result);
    }

    private static List<String> weekPasswords() {
        return List.of(
                "",
                "password",
                "weak",
                "123",
                "121212121212",
                "dkjdfdkfAAAA",
                "kfjdfjdfjk"
        );
    }

    private static List<String> strongPasswords() {
        return List.of(
                "1212AAAAAcvk12k!!!",
                "938498AAAAckvj!!ckjvckvj",
                "kjfkdjfAAA0d0(_dkfjkdjf)"
        );
    }
}
