package rikser123.bundle.advice;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import rikser123.bundle.dto.response.RikserResponseItem;

@RestControllerAdvice
@Order(1)
@No
public class ResponseEntityWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> returnTypeClass = returnType.getParameterType();
        return !ResponseEntity.class.isAssignableFrom(returnTypeClass);

    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof RikserResponseItem<?> bodyResponse) {
            var httpStatus = bodyResponse.getHttpStatus();
            bodyResponse.setHttpStatus(null);
            return ResponseEntity.status(httpStatus).body(bodyResponse);
        }

        return ResponseEntity.ok().body(body);
    }
}

