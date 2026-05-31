package rikser123.bundle.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rikser123.bundle.dto.User;
import rikser123.bundle.dto.request.LoginRequestDto;
import rikser123.bundle.dto.response.LoginResponseDto;
import rikser123.bundle.dto.response.PublicKeyResponseDto;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.dto.response.UpdateTokenResponseDto;


@FeignClient(
  name = "security-client",
  url = "${bundle.security.service.url}"
)
public interface SecurityClient {
  @GetMapping(
    value = "/api/v1/user/token",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  RikserResponseItem<User> getUser();

  @GetMapping(
    value = "/api/v1/user/token/refresh",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  RikserResponseItem<UpdateTokenResponseDto> updateToken();

  @GetMapping(
    value = "/api/v1/user/public-key",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  RikserResponseItem<PublicKeyResponseDto> getPublicKey();

  @PostMapping(
    value = "/api/v1/user/login",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  RikserResponseItem<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto dto);
}
