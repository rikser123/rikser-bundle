package rikser123.bundle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.advice.GlobalExceptionHandler;
import rikser123.bundle.component.TransactionHandler;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.impl.StatusMatrixImpl;

@Configuration
public class ComponentConfig {
  @Bean
  public GlobalExceptionHandler globalExceptionHandler() {
    return new GlobalExceptionHandler();
  }

  @Bean
  public StatusMatrix statusMatrix() {
    return new StatusMatrixImpl();
  }

  @Bean
  public TransactionHandler transactionHandler() {
    return new TransactionHandler();
  }
}
