package rikser123.bundle.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
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
  public UserDetails getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new RuntimeException("Пользователь не аутентифицирован");
    }
    return (UserDetails) authentication.getPrincipal();
  }

  @Override
  public UserDetailsService userDetailsService() {
    return this::getByUsername;
  }

  @Override
  public UserDetails getByUsername(String token) {
    var response = securityClient.getUser();
    return response.getData();
  }

  @Override
  public String updateToken(String refreshToken) {
    var response = securityClient.updateToken();
    return response.getData().getToken();
  }
}
