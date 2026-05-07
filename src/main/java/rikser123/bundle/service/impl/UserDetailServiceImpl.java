package rikser123.bundle.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.UserDetailService;

/**
 * Сервис для взаимодействия с пользователями
 */

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {
  public static final String BEARER_PREFIX = "Bearer ";
  private final SecurityClient securityClient;

  @Override
  public Mono<UserDetails> getCurrentUser() {
    return ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .map(data -> (UserDetails) data.getPrincipal());
  }

  @Override
  public ReactiveUserDetailsService userDetailsService() {
    return this::getByUsername;
  }

  @Override
  public Mono<UserDetails> getByUsername(String token) {
    return securityClient.getUser(BEARER_PREFIX + token)
      .map(RikserResponseItem::getData)
      .onErrorResume(e -> Mono.error(new EntityNotFoundException("Пользователь не найден")));
  }
}
