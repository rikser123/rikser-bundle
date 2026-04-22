package rikser123.bundle.feign;

import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.dto.request.UserGetDto;
import rikser123.bundle.dto.response.RikserResponseItem;

@ReactiveFeignClient(
  url = "${security.service.url}",
  name = "security-client"
)
public interface SecurityClient {
  @PostMapping(
    value = "/api/v1/user/token",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  Mono<RikserResponseItem<UserDetails>> getUser(RikserRequestItem<UserGetDto> requestDto);
}
