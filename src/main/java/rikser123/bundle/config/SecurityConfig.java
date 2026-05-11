package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactivefeign.ReactiveContract;
import reactivefeign.webclient.WebReactiveFeign;
import rikser123.bundle.component.AuthenticationEntryPoint;
import rikser123.bundle.component.JwtAuthenticationFilter;
import rikser123.bundle.component.ResponseStatusFilter;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.service.impl.UserDetailServiceImpl;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
  @Value("${bundle.security.service.url}")
  private String securityHost;

  @Bean
  @ConditionalOnProperty(name = "bundle.security.enabled", havingValue = "true", matchIfMissing = true)
  public SecurityWebFilterChain securityWebFilterChain(
    ServerHttpSecurity http,
    ReactiveAuthenticationManager reactiveAuthenticationManager,
    JwtAuthenticationFilter jwtAuthenticationFilter,
    AuthenticationEntryPoint authenticationEntryPoint

  ) {
    return http.csrf(csrf -> csrf.disable())
      .authenticationManager(reactiveAuthenticationManager)
      .httpBasic(httpBasic -> httpBasic.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .authorizeExchange(
        exchanges ->
          exchanges
            .pathMatchers("/api/v1/user/register", "/api/v1/user/login")
            .permitAll()
            .pathMatchers(
              "/swagger-ui/**",
              "/swagger-ui.html",
              "/swagger-resources/**",
              "/v3/api-docs/**",
              "/webjars/**")
            .permitAll()
            .pathMatchers("/actuator/health", "/actuator/info")
            .permitAll()
            .anyExchange()
            .permitAll())
      .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
      .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint))
      .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
      .formLogin(form -> form.disable())
      .logout(logout -> logout.disable())
      .build();
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.security.enabled", havingValue = "true", matchIfMissing = true)
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(
      Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L); // 1 час кэширования preflight запросов

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
  public ReactiveAuthenticationManager reactiveAuthenticationManager(
    PasswordEncoder passwordEncoder, UserDetailService userDetailService) {
    UserDetailsRepositoryReactiveAuthenticationManager manager =
      new UserDetailsRepositoryReactiveAuthenticationManager(userDetailService.userDetailsService());
    manager.setPasswordEncoder(passwordEncoder);
    return manager;
  }

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

  @Bean
  public ReactiveContract reactiveContract() {
    return new ReactiveContract(new SpringMvcContract());
  }

  @Bean
  SecurityClient securityClient() {
    return WebReactiveFeign.<SecurityClient>builder()
      .contract(new ReactiveContract(new SpringMvcContract()))
      .target(SecurityClient.class, securityHost);
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
    return new AuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public ResponseStatusFilter responseStatusFilter() {
    return new ResponseStatusFilter();
  }
}
