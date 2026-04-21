package rikser123.bundle.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.dto.request.UserGetDto;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.UserDetailService;

import java.util.Optional;

/**
 * Сервис для взаимодействия с пользователями
 */

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {
  private final SecurityClient securityClient;

  @Override
  public UserDetails getCurrentUser() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
      .map(SecurityContext::getAuthentication)
      .map(Authentication::getPrincipal)
      .map(principal -> (UserDetails) principal)
      .orElseThrow(() -> new IllegalStateException("Current user not found or not authenticated"));
  }

  @Override
  public UserDetailsService userDetailsService() {
    return this::getByUsername;
  }

  @Override
  public UserDetails getByUsername(String token) {
    var request = new RikserRequestItem<UserGetDto>();
    request.setData(new UserGetDto(token));

    var response = securityClient.getUser(request);

    if (response == null || response.getData() == null) {
      throw new EntityNotFoundException("Пользователь не найден");
    }

    return response.getData();
  }
}
