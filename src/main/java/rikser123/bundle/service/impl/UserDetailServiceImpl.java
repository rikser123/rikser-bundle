package rikser123.bundle.service.impl;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import rikser123.bundle.dto.User;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.PublicKeyLoaderService;
import rikser123.bundle.service.UserDetailService;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для взаимодействия с пользователями
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {
  private final SecurityClient securityClient;
  private final PublicKeyLoaderService publicKeyLoaderService;

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
    var publicKey = publicKeyLoaderService.getPublicKey();
    var userData = Jwts.parser().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
    var birthDate = userData.get("birthDate", String.class);

    var user = new User();
    user.setLogin(userData.getSubject());
    user.setId(UUID.fromString(userData.get("id", String.class)));
    user.setEmail(userData.get("email", String.class));
    user.setStatus(userData.get("status", String.class));
    user.setFirstName(userData.get("firstName", String.class));
    user.setMiddleName(userData.get("middleName", String.class));
    user.setLastName(userData.get("lastName", String.class));
    user.setBirthDate(StringUtils.isNoneEmpty(birthDate) ? LocalDate.parse(birthDate) : null);
    user.setPrivileges(userData.get("privileges", Set.class));

    return user;
  }

  @Override
  public String updateToken(String refreshToken) {
    var response = securityClient.updateToken();
    return response.getData().getToken();
  }
}
