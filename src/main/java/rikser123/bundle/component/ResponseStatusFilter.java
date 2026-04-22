package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ResponseStatusFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var originalResponse = exchange.getResponse();
    var wrappedResponse = new StatusCapturingResponse(exchange, originalResponse);

    return chain.filter(exchange.mutate().response(wrappedResponse).build());
  }

  static class StatusCapturingResponse extends ServerHttpResponseDecorator {
    private final ServerWebExchange exchange;
    private final ObjectMapper objectMapper = new ObjectMapper();

    StatusCapturingResponse(ServerWebExchange serverExchange, ServerHttpResponse delegate) {
      super(delegate);
      this.exchange = serverExchange;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
      if (body instanceof Mono) {
        var monoBody = (Mono<DataBuffer>) body;

        return monoBody.flatMap(
          buffer -> {
            try {
              var bytes = new byte[buffer.readableByteCount()];
              buffer.read(bytes);
              var json = new String(bytes, StandardCharsets.UTF_8);

              var restored = buffer.factory().wrap(bytes);

              var node = objectMapper.readTree(json);

              if (node.has("httpStatus")) {
                var status = node.get("httpStatus").toString().replaceAll("\"", "");
                getDelegate().setStatusCode(HttpStatus.valueOf(status));
              }

              return super.writeWith(Mono.just(restored));

            } catch (Exception e) {
              buffer.readPosition(0);
              return super.writeWith(Mono.just(buffer));
            }
          });
      }

      return super.writeWith(body);
    }
  }
}
