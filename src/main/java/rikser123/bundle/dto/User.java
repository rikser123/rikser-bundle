package rikser123.bundle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements UserDetails {
  private UUID id;
  private String login;
  private String password;
  private String email;
  private String status = "REGISTERED";
  private String firstName;
  private String middleName;
  private String lastName;
  private LocalDate birthDate;
  private Instant created;
  private Set<String> privileges;
  private Instant updated;

  @Override
  public String getUsername() {
    return login;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return privileges.stream()
      .map(userPrivilege -> new SimpleGrantedAuthority(userPrivilege))
      .toList();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
