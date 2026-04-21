package rikser123.bundle.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class ResponseStatusFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

    var wrapper = new ContentCachingResponseWrapper(response);

    try {
      chain.doFilter(request, wrapper);

      byte[] responseBody = wrapper.getContentAsByteArray();

      if (responseBody.length > 0) {
        var json = new String(responseBody, wrapper.getCharacterEncoding());
        var node = objectMapper.readTree(json);

        if (node.has("httpStatus")) {
          var statusStr = node.get("httpStatus").asText();
          var status = HttpStatus.valueOf(statusStr);
          response.setStatus(status.value());
        }
      }

      wrapper.copyBodyToResponse();

    } catch (Exception e) {
      log.warn("Failed to process response status", e);
      wrapper.copyBodyToResponse();
    }
  }
}
