package rikser123.bundle.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class FieldExtractor {

  /**
   * Извлечение всех строк в объекте рекурсивно в Map
   *
   * @param obj Объект для извлечения полей
   *            ъ    * @return Map, где ключи - это названия полей с вложенностью, а значения - строки
   */
  public static Map<String, String> extractToMap(Object obj, ObjectMapper objectMapper) {
    Map<String, String> result = new HashMap<>();

    try {
      JsonNode node = objectMapper.valueToTree(obj);
      flattenJsonNode(node, "", result);
    } catch (Exception e) {
      throw new RuntimeException("Failed to flatten object", e);
    }

    return result;
  }

  private static void flattenJsonNode(JsonNode node, String path,
                                      Map<String, String> result) {
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      objectNode.fieldNames().forEachRemaining(fieldName -> {
        String newPath = path.isEmpty() ? fieldName : path + "." + fieldName;
        flattenJsonNode(objectNode.get(fieldName), newPath, result);
      });
    } else if (node.isArray()) {
      for (int i = 0; i < node.size(); i++) {
        String newPath = path + "[" + i + "]";
        flattenJsonNode(node.get(i), newPath, result);
      }
    } else if (node.isValueNode() && node.isTextual()) {
      result.put(path, node.asText());
    }
  }
}