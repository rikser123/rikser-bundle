package rikser123.bundle.feign;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.User;
import rikser123.bundle.dto.response.RikserResponseItem;

public interface SecurityClient {
  @GetMapping(
    value = "/api/v1/user/token",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  Mono<RikserResponseItem<User>> getUser();
}
