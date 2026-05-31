package rikser123.bundle.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.bundle.validation.CheckSqlInjection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Входные параметры для логина пользователя")
public class LoginRequestDto {
  @NotEmpty(message = "Логин не должен быть пустым")
  @CheckSqlInjection
  private String login;

  @NotEmpty(message = "Пароль не должен быть пустым")
  @CheckSqlInjection
  private String password;
}
