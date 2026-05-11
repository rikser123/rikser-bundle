package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rikser123.bundle.component.JwtAuthenticationFilter;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.service.impl.UserDetailServiceImpl;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
  @Bean
  @ConditionalOnProperty(name = "bundle.security.enabled", havingValue = "true", matchIfMissing = true)
  @Primary
  @Order(-1)
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    AuthenticationManager authenticationManager,
    JwtAuthenticationFilter jwtAuthenticationFilter
  ) throws Exception {
    return http
      .securityMatcher("/api/**", "/swagger-ui/**", "/swagger-resources/**")
      .csrf(csrf -> csrf.disable())
      .authenticationManager(authenticationManager)
      .httpBasic(httpBasic -> httpBasic.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .anonymous(anonymous -> anonymous.disable())
      .authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/api/v1/user/register", "/api/v1/user/login").permitAll()
        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/v3/api-docs/**", "/webjars/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .formLogin(form -> form.disable())
      .logout(logout -> logout.disable())
      .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.security.enabled", havingValue = "true", matchIfMissing = true)
  public AuthenticationManager authenticationManager(
    PasswordEncoder passwordEncoder, UserDetailService userDetailsService) {

    var provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder);
    provider.setUserDetailsService(userDetailsService.userDetailsService());

    return provider::authenticate;
  }

  @ConditionalOnProperty(name = "bundle.security.enabled", havingValue = "true", matchIfMissing = true)
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(
    UserDetailService userDetailService,
    ObjectMapper objectMapper
  ) {
    return new JwtAuthenticationFilter(userDetailService, objectMapper);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.security.service.enabled", havingValue = "true", matchIfMissing = true)
  public UserDetailService userDetailService(SecurityClient securityClient) {
    return new UserDetailServiceImpl(securityClient);
  }
}
