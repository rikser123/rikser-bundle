package rikser123.bundle.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import rikser123.bundle.dto.User;
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
}
