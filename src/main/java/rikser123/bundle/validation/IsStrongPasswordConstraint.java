package rikser123.bundle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IsStrongPasswordConstraint implements ConstraintValidator<IsStrongPassword, String> {
  private static final Pattern LETTERS_PATTERN = Pattern.compile("(.*[a-zA-Z]).*");
  private static final Pattern DIGITS_PATTERN = Pattern.compile("(.*\\d).*");
  private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("(.*[^a-zA-Z\\d].*)");
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

    var lettersMatcher = LETTERS_PATTERN.matcher(password);
    if (lettersMatcher.matches()) {
      passwordStrength += 1;
    }

    var digitsMatcher = DIGITS_PATTERN.matcher(password);
    if (digitsMatcher.matches()) {
      passwordStrength += 1;
    }

    var specialMatcher = SPECIAL_CHARACTERS_PATTERN.matcher(password);
    if (specialMatcher.matches()) {
      passwordStrength += 1;
    }

    return PASSWORD_GOOD_STRENGTH <= passwordStrength;
  }
}
