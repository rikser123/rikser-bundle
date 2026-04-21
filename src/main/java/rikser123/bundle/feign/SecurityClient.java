package rikser123.bundle.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.dto.request.UserGetDto;
import rikser123.bundle.dto.response.RikserResponseItem;

@FeignClient(
  url = "${security.service.url}",
  name = "security-client"
)
public interface SecurityClient {

  @PostMapping(
    value = "/api/v1/user/token",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  RikserResponseItem<UserDetails> getUser(RikserRequestItem<UserGetDto> requestDto);
}