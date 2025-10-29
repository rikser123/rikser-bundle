package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IsStrongPasswordConstraint implements ConstraintValidator<IsStrongPassword, String> {
    private static final Pattern lettersPattern = Pattern.compile("(.*[a-zA-Z]).*");
    private static final Pattern digitsPattern = Pattern.compile("(.*\\d).*");
    private static final Pattern specialCharactersPattern = Pattern.compile("(.*[^a-zA-Z\\d].*)");
    private static final int PASSWORD_GOOD_STRENGTH = 4;

    private int passwordMinLength;

    @Override
    public void initialize(IsStrongPassword annotation) {
        passwordMinLength = annotation.passwordMinLength();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        var passwordStrength = 0;

        if (password.length() >= passwordMinLength) {
            passwordStrength += 1;
        }

        var lettersMatcher = lettersPattern.matcher(password);
        if (lettersMatcher.matches()) {
            passwordStrength +=1;
        }

        var digitsMatcher = digitsPattern.matcher(password);
        if (digitsMatcher.matches()) {
            passwordStrength += 1;
        }

        var specialMatcher = specialCharactersPattern.matcher(password);
        if (specialMatcher.matches()) {
            passwordStrength += 1;
        }

        return PASSWORD_GOOD_STRENGTH <= passwordStrength;
    }
}
