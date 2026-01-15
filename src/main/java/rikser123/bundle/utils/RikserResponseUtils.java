package rikser123.bundle.utils;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import rikser123.bundle.dto.response.RikserResponseItem;

import java.util.List;
import java.util.Map;

@UtilityClass
public class RikserResponseUtils {
    public static <T> RikserResponseItem<T> createResponse(
            boolean result,
            T data,
            HttpStatus status,
            Map<String, List<String>> errors,
            Map<String, List<String>> warnings,
            String message
    ) {
        var response = new RikserResponseItem<T>();
        response.setData(data);
        response.setResult(result);
        response.setErrors(errors);
        response.setWarnings(warnings);
        response.setMessage(message);
        response.setHttpStatus(status);
        return response;
    }

    public static <T> RikserResponseItem<T> createResponse(T data, HttpStatus status) {
        return createResponse(true, data, status, null, null, null);
    }

    public static <T> RikserResponseItem<T> createResponse(T data) {
        return createResponse(true, data, HttpStatus.OK, null, null, null);
    }

    public static RikserResponseItem createResponse(
            HttpStatus status,
            Map<String, List<String>> errors,
            Map<String, List<String>> warnings
    ) {
        return createResponse(false, null, status, errors, warnings, null);
    }

    public static RikserResponseItem createResponse(String message, HttpStatus status) {
        return createResponse(false, null, status, null, null, message);
    }
}
