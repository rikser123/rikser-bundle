package rikser123.bundle.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import rikser123.bundle.advice.GlobalExceptionHandler;
import rikser123.bundle.component.TransactionHandler;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.impl.StatusMatrixImpl;

/**
 * Автоконфигурация бандла
 */

@AutoConfiguration
public class BundleAutoConfiguration {

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

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
    return builder -> {
      builder.featuresToDisable(
          SerializationFeature.FAIL_ON_EMPTY_BEANS,
          SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
          SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
      );

      builder.featuresToEnable(
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
      );

      builder.postConfigurer(objectMapper -> {
        objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
      });

      builder.serializationInclusion(JsonInclude.Include.NON_NULL);

      builder.modulesToInstall(
          new ParameterNamesModule(),
          new JavaTimeModule()
      );

      builder.findModulesViaServiceLoader(true);
    };
  }
}
