package rikser123.bundle.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.json.JsonHttpLogFormatter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLogger {
  private static final JsonHttpLogFormatter formatter = new JsonHttpLogFormatter();

  private final BodyFilter bodyFilter;

  public void logKafkaMessage(ProducerRecord<String, String> record) throws IOException {
    var filteredBody = bodyFilter.filter("application/json", record.value());

    var traceId = getTraceId(record.headers().lastHeader("traceparent"));


    var logMap = new LinkedHashMap<String, Object>();
    logMap.put("type", "KAFKA_PRODUCE");
    logMap.put("traceId", traceId);
    logMap.put("topic", record.topic());
    logMap.put("body", filteredBody);

    log.info(formatter.format(logMap));
  }

  public void logKafkaMessage(ConsumerRecord<String, String> record) throws IOException {
    var filteredBody = bodyFilter.filter("application/json", record.value());
    var traceId = getTraceId(record.headers().lastHeader("traceparent"));

    var logMap = new LinkedHashMap<String, Object>();
    logMap.put("type", "KAFKA_CONSUME");
    logMap.put("traceId", traceId);
    logMap.put("topic", record.topic());
    logMap.put("body", filteredBody);

    log.info(formatter.format(logMap));
  }

  private String getTraceId(Header traceHeader) {
    var traceId = MDC.get("traceId");

    if (traceId == null) {
      if (traceHeader != null) {
        String traceparent = new String(traceHeader.value(), StandardCharsets.UTF_8);
        String[] parts = traceparent.split("-");
        if (parts.length >= 2) {
          traceId = parts[1];
        }
      }
    }

    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
    }

    return traceId;
  }
}
