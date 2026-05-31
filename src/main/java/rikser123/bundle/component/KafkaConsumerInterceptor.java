package rikser123.bundle.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import rikser123.bundle.dto.TokenDto;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.UserDetailService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerInterceptor implements ConsumerInterceptor<String, String> {
  private final UserDetailService userDetailService;

  @Override
  public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> records) {
    var validatedRecords = new HashMap<TopicPartition, List<ConsumerRecord<String, String>>>();

    for (var partition : records.partitions()) {
      var partitionRecords = records.records(partition);
      var validRecords = new ArrayList<ConsumerRecord<String, String>>();

      for (ConsumerRecord<String, String> record : partitionRecords) {
        if (isValidAuthorization(record)) {
          validRecords.add(record);
        } else {
          log.warn("Invalid or missing token for record from topic: {}, offset: {}",
            record.topic(), record.offset());
        }
      }
      validatedRecords.put(partition, validRecords);
    }

    return new ConsumerRecords<>(validatedRecords);
  }

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
      var context = SecurityContextHolder.createEmptyContext();
      var userDetails = userDetailService.getByUsername(token);
      var authentication = createAuthenticationToken(userDetails);

      var tokenDto = new TokenDto();
      tokenDto.setAccessToken(token);
      authentication.setDetails(tokenDto);

      context.setAuthentication(authentication);

      SecurityContextHolder.setContext(context);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {

  }

  @Override
  public void close() {

  }


  @Override
  public void configure(Map<String, ?> map) {

  }

  private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
    var user = (User) userDetails;
    List<SimpleGrantedAuthority> authorities = user.getPrivileges().stream()
      .map(SimpleGrantedAuthority::new)
      .toList();

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }

}
