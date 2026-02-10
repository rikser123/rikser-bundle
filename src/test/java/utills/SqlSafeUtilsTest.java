package utills;

import org.junit.jupiter.api.Test;
import rikser123.bundle.utils.SqlSafeUtils;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SqlSafeUtilsTest {
    @Test
    void testSafe() {
        var value = "DENISOV";
        var result = SqlSafeUtils.isSqlSave(value);

        assertTrue(result);
    }

    @Test
    void testNotSafe() {
        var value = "(xxe)";
        var result = SqlSafeUtils.isSqlSave(value);

        assertFalse(result);
    }
}
