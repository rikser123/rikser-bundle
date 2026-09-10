package rikser123.bundle.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import rikser123.bundle.dto.request.LoginRequestDto;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.feign.SecurityClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerInterceptor implements ProducerInterceptor<String, String> {
  private static final int REFRESH_MINUTES_MARGIN = 3;

  private final KafkaLogger kafkaLogger;
  private final SecurityClient securityClient;
  private final TaskScheduler taskScheduler;

  @Value("${bundle.kafka.user}")
  private String kafkaUser;

  @Value("${bundle.kafka.password}")
  private String kafkaPassword;

  @Value("${bundle.expiration-time}")
  private long expirationTime;

  private volatile String token;
  private volatile Instant expiry;

  private ScheduledFuture<?> scheduledFuture;

  @PostConstruct
  void init() {
    try {
      applyNewToken();
    } catch (RuntimeException e) {
      throw new IllegalStateException("Не удалось получить токен при старте приложения", e);
    }
  }

  @Override
  public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
    try {
      kafkaLogger.logKafkaMessage(record);
    } catch (IOException e) {
      log.warn("Не удалось залогировать сообщение кафки");
    }

    record.headers().add("Authorization",
      ("Bearer " + token).getBytes(StandardCharsets.UTF_8));
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

    var result = securityClient.loginSystem(request);

    if (BooleanUtils.isFalse(result.isResult()) || StringUtils.isEmpty(result.getData().getToken())) {
      throw new IllegalStateException("Не удалось авторизатоваться системному пользователю!");
    }

    return result.getData().getToken();
  }

  private void applyNewToken() {
    this.token = fetchToken();
    this.expiry = Instant.now().plusMillis(expirationTime);
    scheduleNextRefresh(expiry.minus(REFRESH_MINUTES_MARGIN, ChronoUnit.MINUTES));
  }

  private synchronized void refreshToken() {
    try {
      applyNewToken();
    } catch (RuntimeException e) {
      log.error("Can not update user system token, try again", e);
      scheduleRetry();
    }
  }

  private void scheduleNextRefresh(Instant when) {
    cancelPrevious();
    scheduledFuture = taskScheduler.schedule(this::refreshToken, when);
  }

  private void scheduleRetry() {
    cancelPrevious();
    scheduledFuture = taskScheduler.schedule(
      this::refreshToken,
      Instant.now().plus(1, ChronoUnit.MINUTES)
    );
  }

  private void cancelPrevious() {
    if (!Objects.isNull(scheduledFuture) && !scheduledFuture.isDone()) {
      scheduledFuture.cancel(false);
    }
  }
}
