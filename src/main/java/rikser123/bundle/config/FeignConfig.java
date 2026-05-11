package rikser123.bundle.config;

import feign.Feign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.feign.SecurityClient;


@Configuration
@Slf4j
public class FeignConfig {
  @Value("${bundle.security.service.url}")
  private String securityServiceUrl;

  @Bean
  public SecurityClient securityClient() {
    return Feign.builder()
      .contract(new SpringMvcContract())
      .target(SecurityClient.class, securityServiceUrl);
  }
}