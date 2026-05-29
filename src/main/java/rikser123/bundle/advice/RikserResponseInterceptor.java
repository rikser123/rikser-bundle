package rikser123.bundle.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import rikser123.bundle.dto.response.RikserResponseItem;

@RestControllerAdvice
@Slf4j
public class RikserResponseInterceptor implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType,
                          Class<? extends HttpMessageConverter<?>> converterType) {
    return returnType.getParameterType().equals(RikserResponseItem.class);
  }

  @Override
  public Object beforeBodyWrite(
    Object body,
    MethodParameter returnType,
    MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType,
    ServerHttpRequest request,
    ServerHttpResponse response
  ) {
    if (body instanceof RikserResponseItem<?> responseItem) {
      var status = responseItem.getHttpStatus();

      if (status != null) {
        response.setStatusCode(status);
      }
    }

    return body;
  }
}
