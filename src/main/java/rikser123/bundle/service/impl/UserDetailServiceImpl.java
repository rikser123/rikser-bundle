package rikser123.bundle.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.UserDetailService;

/**
 * Сервис для взаимодействия с пользователями
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {
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
    return securityClient.getUser()
      .map(response -> (UserDetails) response.getData())
      .onErrorResume(e -> {
        log.error("error", e);
        return Mono.error(new EntityNotFoundException("Пользователь не найден"));
      });
  }
}
