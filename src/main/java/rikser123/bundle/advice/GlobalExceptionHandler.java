package rikser123.bundle.advice;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.utils.RikserResponseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Класс для работы с глобальными исключениями
 *
 */

@RestControllerAdvice
@Slf4j
@Order(-2) // поднятие приоритета по сравнению с актуатором
@NoArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<RikserResponseItem> handleValidationException(WebExchangeBindException exception, ServerWebExchange exchange) {
        var errors = new HashMap<String, List<String>>();

        exception.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var message = error.getDefaultMessage();

            var fieldLastPart = getFieldLastPart(fieldName);
            errors.computeIfPresent(fieldLastPart, (key, value) -> {
                value.add(message);
                return value;
            });
            errors.putIfAbsent(fieldLastPart, new ArrayList<>(List.of(message)));
        });

        var response = RikserResponseUtils.createResponse(HttpStatus.BAD_REQUEST, errors, null);
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

        return Mono.just(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<RikserResponseItem> handleRuntimeException(RuntimeException exception, ServerWebExchange exchange) {
        log.error("Internal server error", exception);

        var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        return Mono.just(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<RikserResponseItem> handleAccessDeniedException(AccessDeniedException exception,  ServerWebExchange exchange) {
        log.warn("access forbidden", exception);
        var response = RikserResponseUtils.createResponse("Доступ к запрашиваемому ресурсу запрещен", HttpStatus.FORBIDDEN);
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

        return Mono.just(response);
    }

    @ExceptionHandler(EntityExistsException.class)
    public Mono<RikserResponseItem> handleEntityExistsException(EntityExistsException exception, ServerWebExchange exchange) {
        log.warn("entity exists", exception);
        var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);

        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return Mono.just(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<RikserResponseItem> handleEntityNotFoundException(EntityNotFoundException exception, ServerWebExchange exchange) {
        log.warn("entity exists", exception);
        var response = RikserResponseUtils.createResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

        return Mono.just(response);
    }

    private static String getFieldLastPart(String field) {
        var fieldParts = field.split("\\.");
        return fieldParts[fieldParts.length - 1];
    }
}