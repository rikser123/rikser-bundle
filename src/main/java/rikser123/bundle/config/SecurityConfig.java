package rikser123.bundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rikser123.bundle.component.CustomAuthenticationEntryPoint;
import rikser123.bundle.component.JwtAuthenticationFilter;
import rikser123.bundle.component.ResponseStatusFilter;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.service.impl.UserDetailServiceImpl;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
  @Value("${security.service.url}")
  private String securityHost;

  @Bean
  @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    JwtAuthenticationFilter jwtAuthenticationFilter,
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint
  ) throws Exception {
    return http
      .csrf(c -> c.disable())
      .httpBasic(b -> b.disable())
      .anonymous(a -> a.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/api/v1/user/register", "/api/v1/user/login").permitAll()
        .requestMatchers(
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/swagger-resources/**",
          "/v3/api-docs/**",
          "/webjars/**").permitAll()
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(exceptions -> exceptions
        .authenticationEntryPoint(customAuthenticationEntryPoint)
      )
      .formLogin(f -> f.disable())
      .logout(l -> l.disable())
      .build();
  }

  @Bean
  public SecurityContextHolderStrategy securityContextHolderStrategy() {
    return SecurityContextHolder.getContextHolderStrategy();
  }

  @PostConstruct
  public void initSecurityContext() {
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
  }

  @Bean
  @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
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
  @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
  public DaoAuthenticationProvider daoAuthenticationProvider(
    UserDetailService userDetailService,
    PasswordEncoder passwordEncoder) {

    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailService.userDetailsService());
    provider.setPasswordEncoder(passwordEncoder);
    provider.setHideUserNotFoundExceptions(false); // чтобы видеть реальные ошибки

    return provider;
  }

  @Bean
  @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
  public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
    return new ProviderManager(provider);
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailService userDetailService, ObjectMapper objectMapper) {
    return new JwtAuthenticationFilter(userDetailService, objectMapper);
  }

  @Bean
  @ConditionalOnProperty(name = "security.service.enabled", havingValue = "true", matchIfMissing = true)
  public UserDetailService userDetailService(SecurityClient securityClient) {
    return new UserDetailServiceImpl(securityClient);
  }

  @Bean
  @ConditionalOnProperty(name = "security.service.enabled", havingValue = "true", matchIfMissing = true)
  public SecurityClient securityClient() {
    return Feign.builder()
      .contract(new SpringMvcContract())
      .encoder(new JacksonEncoder())
      .decoder(new JacksonDecoder())
      .logLevel(feign.Logger.Level.FULL)
      .target(SecurityClient.class, securityHost);
  }

  @Bean
  public CustomAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
    return new CustomAuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public ResponseStatusFilter responseStatusFilter() {
    return new ResponseStatusFilter();
  }
}
