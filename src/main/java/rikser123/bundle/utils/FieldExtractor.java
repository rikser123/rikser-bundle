package rikser123.bundle.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

  /**
   * Инициализирует ленивое поле Hibernate (LAZY-связь/коллекцию) или загружает его данные через Supplier,
   * если поле равно {@code null} или сессия Hibernate уже закрыта.
   * <p>
   * Если данные загружаются через {@code supplier}, они автоматически записываются в сущность
   * с помощью переданного {@code setter}, что гарантирует актуальность состояния объекта.
   *
   * @param field    текущее поле сущности для проверки (может быть null, прокси-объектом или коллекцией)
   * @param setter   функция-сеттер для записи загруженных данных обратно в сущность (например, {@code entity::setFields})
   * @param supplier функция для резервной загрузки данных (например, вызов репозитория из БД)
   * @param <T>      тип данных инициализируемого поля
   * @return проинициализированный объект из поля сущности, либо результат выполнения {@code supplier}
   */
  public static <T> T initLazyFieldWithSetter(T field, Consumer<T> setter, Supplier<T> supplier) {
    try {
      if (field == null) {
        T loadField = supplier.get();
        setter.accept(loadField);
        return loadField;
      }
      if (!Hibernate.isInitialized(field)) {
        Hibernate.initialize(field);
      }
      return field;
    } catch (LazyInitializationException e) {
      // Если сессия закрыта — идем в репозиторий
      T loadField = supplier.get();
      setter.accept(loadField);
      return loadField;
    }
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