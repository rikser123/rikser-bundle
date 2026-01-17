package rikser123.bundle.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import rikser123.bundle.advice.GlobalExceptionHandler;
import rikser123.bundle.advice.ResponseEntityWrapperAdvice;
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

    @Bean()
    public ResponseEntityWrapperAdvice responseEntityWrapperAdvice() {
        return new ResponseEntityWrapperAdvice();
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
