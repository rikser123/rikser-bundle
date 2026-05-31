package rikser123.bundle.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import rikser123.bundle.dto.TokenDto;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.UserDetailService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerInterceptor implements RecordInterceptor<String, String> {

  private final UserDetailService userDetailService;

  private boolean isValidAuthorization(ConsumerRecord<String, String> record) {
    var authHeader = record.headers().lastHeader("Authorization");
    if (authHeader == null) {
      log.warn("Missing Authorization header");
      return false;
    }

    var token = new String(authHeader.value(), StandardCharsets.UTF_8);
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    try {
      var userDetails = userDetailService.getByUsername(token);
      if (userDetails == null) {
        log.warn("User not found: {}", token);
        return false;
      }

      var authentication = createAuthenticationToken(userDetails);

      var tokenDto = new TokenDto();
      tokenDto.setAccessToken(token);
      authentication.setDetails(tokenDto);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      return true;
    } catch (Exception e) {
      log.warn("Authorization failed for token: {}", e.getMessage());
      return false;
    }
  }

  private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
    var user = (User) userDetails;
    List<SimpleGrantedAuthority> authorities = user.getPrivileges().stream()
      .map(SimpleGrantedAuthority::new)
      .toList();

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }

  @Override
  public ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
    if (isValidAuthorization(record)) {
      return record;
    }

    log.warn("Invalid or missing token for record from topic: {}, offset: {}, returning null",
      record.topic(), record.offset());
    return null;
  }

  @Override
  public void success(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
    RecordInterceptor.super.success(record, consumer);
  }

  @Override
  public void failure(ConsumerRecord<String, String> record, Exception exception, Consumer<String, String> consumer) {
    RecordInterceptor.super.failure(record, exception, consumer);
  }

  @Override
  public void afterRecord(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
    RecordInterceptor.super.afterRecord(record, consumer);
  }
}
