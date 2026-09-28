package rikser123.bundle.validation;

import org.junit.jupiter.api.Test;
import rikser123.bundle.utils.SqlSafeUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckSqlInjection {

  @Test
  void sqlSafe() {
    var given = "DENISOV";
    var result = SqlSafeUtils.isSqlSave(given);
    assertTrue(result);
  }

  @Test
  void sqlUnsafe() {
    var given = "(xxe)";
    var result = SqlSafeUtils.isSqlSave(given);
    assertFalse(result);
  }
}
