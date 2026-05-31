package rikser123.bundle.component;


import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rikser123.bundle.dto.request.LoginRequestDto;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.PublicKeyLoaderService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerInterceptor implements ProducerInterceptor<String, String> {
  private final SecurityClient securityClient;
  private final PublicKeyLoaderService publicKeyLoaderService;

  @Value("${bundle.kafka.user}")
  private String kafkaUser;

  @Value("${bundle.kafka.password}")
  private String kafkaPassword;

  private volatile String token;

  @PostConstruct
  void init() {
    this.token = fetchToken();
  }

  @Override
  public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
    record.headers().add("Authorization",
      ("Bearer " + getToken()).getBytes(StandardCharsets.UTF_8));
    return record;
  }

  @Override
  public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> map) {

  }

  private String fetchToken() {
    var loginDto = new LoginRequestDto();
    loginDto.setLogin(kafkaUser);
    loginDto.setPassword(kafkaPassword);

    var request = new RikserRequestItem<LoginRequestDto>();
    request.setChannel("System");
    request.setData(loginDto);
    
    var result = securityClient.login(request);

    if (BooleanUtils.isFalse(result.isResult()) || StringUtils.isEmpty(result.getData().getToken())) {
      throw new IllegalStateException("Не удалось авторизатоваться системному пользователю!");
    }

    return result.getData().getToken();
  }

  private String getToken() {
    var currentToken = this.token;

    if (isTokenValid(currentToken)) {
      return currentToken;
    }

    synchronized (this) {
      if (isTokenValid(this.token)) {
        return this.token;
      }

      log.info("System user token is outdated. Try to update");
      var newToken = fetchToken();
      this.token = newToken;
      return newToken;
    }
  }

  private boolean isTokenValid(String token) {
    if (token == null) return false;

    try {
      var publicKey = publicKeyLoaderService.getPublicKey();
      Jwts.parser().setSigningKey(publicKey).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
