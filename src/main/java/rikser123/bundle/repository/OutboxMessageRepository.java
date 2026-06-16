package rikser123.bundle.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import rikser123.bundle.repository.entity.OutboxMessage;
import rikser123.bundle.repository.entity.OutboxMessageStatus;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface OutboxMessageRepository<T extends OutboxMessage> extends JpaRepository<T, UUID> {
  List<T> findAllByStatusOrderByCreatedAsc(OutboxMessageStatus status, Limit limit);
}