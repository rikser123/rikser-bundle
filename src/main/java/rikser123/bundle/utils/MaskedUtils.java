package rikser123.bundle.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class MaskedUtils {
  public static String maskHeaders(String headers, Set<String> maskedHeaders) {
    String result = headers;
    for (String header : maskedHeaders) {
      result = result.replaceAll("(?i)" + header + "=\\[[^\\]]*\\]", header + "=[***]");
    }
    return result;
  }

  public static String maskBody(String body, Set<String> maskedProperties, ObjectMapper mapper) {

    if (body == null || body.isEmpty()) return body;

    try {
      JsonNode node = mapper.readTree(body);
      maskFields(node, maskedProperties);
      return mapper.writeValueAsString(node);
    } catch (Exception e) {
      String result = body;
      for (String field : maskedProperties) {
        result = result.replaceAll("\"" + field + "\"\\s*:\\s*\"[^\"]*\"", "\"" + field + "\":\"***\"");
      }
      return result;
    }
  }

  private static void maskFields(JsonNode node, Set<String> maskedFields) {
    if (node == null) return;
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      for (String field : maskedFields) {
        if (obj.has(field)) {
          obj.put(field, "***");
        }
      }
      obj.fields().forEachRemaining(entry -> maskFields(entry.getValue(), maskedFields));
    } else if (node.isArray()) {
      node.forEach(child -> maskFields(child, maskedFields));
    }
  }
}
