package rikser123.bundle.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ContextFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var authToken = exchange.getRequest().getHeaders().getFirst("Authorization");
    log.warn("Auth {}", authToken);
    log.warn("headers {}", exchange.getRequest().getHeaders());


    return chain.filter(exchange).contextWrite(context -> context.put("Authorization", authToken));
  }
}
