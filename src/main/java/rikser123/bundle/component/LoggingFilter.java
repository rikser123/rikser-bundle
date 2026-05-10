package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rikser123.bundle.config.MaskedProperties;
import rikser123.bundle.utils.MaskedUtils;

import java.nio.charset.StandardCharsets;

@Component
@Order(0)
@Slf4j
@RequiredArgsConstructor
public class LoggingFilter implements WebFilter {
  private final ObjectMapper mapper;
  private final MaskedProperties maskedProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var traceId = MDC.get("traceId");

    return DataBufferUtils.join(exchange.getRequest().getBody())
      .flatMap(dataBuffer -> {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);

        var body = new String(bytes, StandardCharsets.UTF_8);

        log.info("\n=== REQUEST ===\ntraceId: {}\nMethod: {}\nURI: {}\nHeaders: {}\nBody: {}\n",
          traceId,
          exchange.getRequest().getMethod(),
          exchange.getRequest().getURI(),
          MaskedUtils.maskHeaders(exchange.getRequest().getHeaders().toString(), maskedProperties.getHeaders()),
          body.isEmpty() ? "(empty)" : MaskedUtils.maskBody(body, maskedProperties.getProperties(), mapper)
        );

        ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
          @Override
          public Flux<DataBuffer> getBody() {
            return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
          }
        };

        return chain.filter(exchange.mutate().request(decoratedRequest).build())
          .doOnSuccess(v -> {
            log.info("\n=== RESPONSE ===\ntraceId: {}\nStatus: {}\n",
              traceId,
              exchange.getResponse().getStatusCode()
            );
          })
          .doOnError(e -> {
            log.error("\n=== ERROR ===\ntraceId: {}\nError: {}\n",
              traceId, e.getMessage());
          });
      });
  }


}
