package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.MethodMetadata;
import feign.Target;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.client.log.ReactiveLoggerListener;
import rikser123.bundle.config.MaskedProperties;
import rikser123.bundle.utils.MaskedUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveFeignLoggingListener implements ReactiveLoggerListener<Object> {
  private final MaskedProperties maskedProperties;
  private final ObjectMapper objectMapper;


  @Override
  public Object requestStarted(ReactiveHttpRequest request, Target<?> target, MethodMetadata methodMetadata) {
    String traceId = MDC.get("traceId");

    log.info("→ FEIGN {} {} | traceId={} | headers={}",
      request.method(),
      request.uri(),
      traceId,
      MaskedUtils.maskHeaders(request.headers().toString(), maskedProperties.getHeaders())
    );

    return traceId;
  }

  @Override
  public boolean logRequestBody() {
    return true;
  }

  @Override
  public void bodySent(Object body, Object context) {
    if (body != null && context != null) {
      String bodyStr = bodyToString(body);
      if (!bodyStr.isEmpty()) {
        log.info("→ FEIGN BODY | traceId={} | body={}", context, MaskedUtils.maskBody(bodyStr, maskedProperties.getProperties(), objectMapper));
      }
    }
  }

  @Override
  public void responseReceived(ReactiveHttpResponse<?> response, Object context) {
    log.info("← FEIGN {} | traceId={} | headers={}",
      response.status(),
      context,
      MaskedUtils.maskHeaders(response.headers().toString(), maskedProperties.getHeaders())
    );
  }

  @Override
  public void errorReceived(Throwable error, Object context) {
    log.error("✗ FEIGN ERROR | traceId={} | error={}", context, error.getMessage());
  }

  @Override
  public boolean logResponseBody() {
    return true;
  }

  @Override
  public void bodyReceived(Object body, Object context) {
    if (body != null && context != null) {
      String bodyStr = bodyToString(body);
      if (!bodyStr.isEmpty()) {
        log.info("← FEIGN BODY | traceId={} | body={}", context, MaskedUtils.maskBody(bodyStr, maskedProperties.getProperties(), objectMapper));
      }
    }
  }

  private String bodyToString(Object body) {
    if (body == null) return "";
    if (body instanceof byte[]) {
      return new String((byte[]) body, StandardCharsets.UTF_8);
    }
    if (body instanceof String) {
      return (String) body;
    }
    return body.toString();
  }
}