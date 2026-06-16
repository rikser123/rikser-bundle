package rikser123.bundle.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.repository.OutboxMessageRepository;
import rikser123.bundle.repository.entity.OutboxMessage;
import rikser123.bundle.repository.entity.OutboxMessageStatus;

import java.util.List;

@Slf4j
public abstract class OutboxMessageService<T extends OutboxMessage> {
  private final StatusMatrix<OutboxMessageStatus> outboxStatusMatrix;
  private final OutboxMessageRepository<T> outboxMessageRepository;

  @Value("${bundle.kafka.max-fetch-limit}")
  private int maxFetchLimit;

  public OutboxMessageService(OutboxMessageRepository outboxMessageRepository, StatusMatrix<OutboxMessageStatus> outboxStatusMatrix) {
    this.outboxMessageRepository = outboxMessageRepository;
    this.outboxStatusMatrix = outboxStatusMatrix;
  }

  @Transactional
  public List<T> saveAll(List<T> messages) {
    return outboxMessageRepository.saveAll(messages);
  }

  public T save(T message) {
    return outboxMessageRepository.save(message);
  }

  public List<T> findAllByStatus(OutboxMessageStatus status) {
    return outboxMessageRepository.findAllByStatusOrderByCreatedAsc(status, Limit.of(maxFetchLimit));
  }

  @Transactional
  public T changeStatus(T outboxMessage, OutboxMessageStatus status) {
    if (outboxMessage.getStatus() == status || !outboxStatusMatrix.isAvailable(outboxMessage.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for outboxMessage: {} from: {} to: {}",
        outboxMessage.getId(),
        outboxMessage.getStatus(),
        status);
      throw new StatusChangeException();
    }

    outboxMessage.setStatus(status);
    outboxMessageRepository.save(outboxMessage);
    return outboxMessage;
  }
}

