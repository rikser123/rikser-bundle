package rikser123.bundle.component;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Вспомогательный компонент, помогающий выполнять код в транзакциях
 */
@Component
public class TransactionHandler {

  @Transactional
  public <T> T runTransaction(Supplier<T> supplier) {
    return supplier.get();
  }

  @Transactional
  public void runTransaction(Runnable runnable) {
    runnable.run();
  }
}