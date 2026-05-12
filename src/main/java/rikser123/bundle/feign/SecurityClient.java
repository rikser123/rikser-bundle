package rikser123.bundle.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import rikser123.bundle.dto.User;
import rikser123.bundle.dto.response.RikserResponseItem;

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
  RikserResponseItem<User> getUser(
    @RequestHeader("Authorization") String authorization
  );
}
