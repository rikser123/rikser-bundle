package rikser123.bundle.validation;


import jakarta.validation.ConstraintValidatorContext;
import lombok.Data;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class IsEqualConstraintTest {
  private ConstraintValidatorContext context;
  private ConstraintValidatorContext.ConstraintViolationBuilder builder;
  private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeContext;
  private IsEqual annotation;

  @BeforeEach()
  void init() {
    context = Mockito.mock(ConstraintValidatorContext.class);
    builder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    nodeContext = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
    annotation = getIsEqualAnnotation("password", "passwordConfirmation");

    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    when(builder.addPropertyNode(any())).thenReturn(nodeContext);
  }

  @Test
  void shouldDetectEqual() {
    var constraint = new IsEqualConstraint();
    var userRequestDto = new CreateUserRequestDto();
    userRequestDto.setPassword("a");
    userRequestDto.setPasswordConfirmation("a");

    constraint.initialize(annotation);
    var result = constraint.isValid(userRequestDto, context);
    assertTrue(result);
  }

  @Test
  void shouldDetectNotEqual() {
    var constraint = new IsEqualConstraint();
    var userRequestDto = new CreateUserRequestDto();
    userRequestDto.setPassword("ab");
    userRequestDto.setPasswordConfirmation("a");

    constraint.initialize(annotation);
    var result = constraint.isValid(userRequestDto, context);
    assertFalse(result);
  }

  @Test
  void shouldDetectIfNoFields() {
    var constraint = new IsEqualConstraint();
    var userRequestDto = new CreateUserRequestDto();
    userRequestDto.setPassword("ab");
    userRequestDto.setPasswordConfirmation("a");

    annotation = getIsEqualAnnotation("field1", "field2");

    constraint.initialize(annotation);
    var result = constraint.isValid(userRequestDto, context);
    assertFalse(result);
  }

  private IsEqual getIsEqualAnnotation(String firstFieldValue, String secondFieldValue) {
    var annotation = new ConstraintAnnotationDescriptor.Builder<>(IsEqual.class);
    annotation.setAttribute("firstField", firstFieldValue);
    annotation.setAttribute("secondField", secondFieldValue);

    return annotation.build().getAnnotation();

  }

  @Data
  private static class CreateUserRequestDto {
    private String password;
    private String passwordConfirmation;
  }
}
