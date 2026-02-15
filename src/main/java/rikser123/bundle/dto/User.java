package rikser123.bundle.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements UserDetails {
  private UUID id;
  private String login;
  @JsonIgnore
  private String password;
  private String email;
  private String status = "REGISTERED";
  private String firstName;
  private String middleName;
  private String lastName;
  private LocalDate birthDate;
  private LocalDateTime created;
  private Set<String> privileges;
  private LocalDateTime updated;

  @Override
  @JsonIgnore
  public String getUsername() {
    return login;
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return privileges.stream()
        .map(userPrivilege -> new SimpleGrantedAuthority(userPrivilege))
        .toList();
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return true;
  }
}
