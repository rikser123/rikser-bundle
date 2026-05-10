package rikser123.bundle.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ConfigurationProperties(prefix = "bundle.logging")
@Component
@RequiredArgsConstructor
@Getter
@Setter
public class MaskedProperties {
  private static final List<String> MASKED_HEADERS = List.of("Authorization");
  private static final List<String> MASKED_PROPERTIES = List.of(
    "login",
    "password",
    "confirmationPassword",
    "email",
    "birthDate",
    "privileges",
    "authorities",
    "isAccountNonExpired",
    "isAccountNonLocked",
    "isCredentialsNonExpired",
    "isEnabled"
  );

  private List<String> maskedHeaders;
  private List<String> maskedProperties;

  public Set<String> getHeaders() {
    var headers = new HashSet<String>();
    headers.addAll(MASKED_HEADERS);
    if (!Objects.isNull(maskedHeaders)) {
      headers.addAll(maskedHeaders);
    }

    return headers;
  }

  public Set<String> getProperties() {
    var properties = new HashSet<String>();
    properties.addAll(MASKED_PROPERTIES);
    
    if (!Objects.isNull(maskedProperties)) {
      properties.addAll(maskedProperties);
    }

    return properties;
  }
}
