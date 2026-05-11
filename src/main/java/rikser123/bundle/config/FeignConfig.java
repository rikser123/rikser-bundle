package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Contract;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpRequestInterceptor;
import reactivefeign.webclient.WebReactiveFeign;
import reactor.core.publisher.Mono;
import rikser123.bundle.component.ContextFilter;
import rikser123.bundle.component.ReactiveFeignLoggingListener;
import rikser123.bundle.feign.SecurityClient;

import java.util.List;

@Configuration
@EnableConfigurationProperties(MaskedProperties.class)
public class FeignConfig {
  @Value("${bundle.security.service.url}")
  private String securityServiceUrl;

  @Bean
  public ContextFilter contextFilter() {
    return new ContextFilter();
  }

  @Bean(name = "AuthInterceptor")
  public ReactiveHttpRequestInterceptor authInterceptor() {
    return ((ReactiveHttpRequest request) -> Mono.deferContextual(context -> {
      if (StringUtils.isNotEmpty(context.get("Authorization"))) {
        request.headers().put("Authorization", List.of(context.get("Authorization").toString()));

      }

      return Mono.just(request);
    }));
  }

  @Bean
  public ReactiveFeignLoggingListener reactiveLoggerListener(ObjectMapper objectMapper, MaskedProperties maskedProperties) {
    return new ReactiveFeignLoggingListener(maskedProperties, objectMapper);
  }

  @Bean
  public SecurityClient securityClient(
    ReactiveFeignLoggingListener logger,
    @Qualifier("AuthInterceptor") ReactiveHttpRequestInterceptor authInterceptor
  ) {
    return WebReactiveFeign.<SecurityClient>builder()
      .contract(feignContract())
      .addRequestInterceptor(authInterceptor)
      .addLoggerListener(logger)
      .target(SecurityClient.class, securityServiceUrl);
  }

  @Bean
  public Contract feignContract() {
    return new SpringMvcContract();
  }
}
