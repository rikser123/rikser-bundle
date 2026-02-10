package rikser123.bundle.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Класс при валидации на sql инъекции
 *
 */
@UtilityClass
public class SqlSafeUtils {
    private static final List<String> SQL_REGEXPS = List.of(
        "(sleep\\(.*\\)|waitfor|delay|benchmark).*(\\#|\\'|\\\"|--)",
        "('|\")[\\s]*[\\%]+",
        "'[\\s]*(\\;|\\+|\\*|\\=)+",
        "(exec|execute)(\\(| )",
        "('|\\\\x27|\\\")+[\\s]*(or|\\\\x6Fx72|x4Fx52|union[\\s](|all)|;)[\\s]+(sql-command|abort|alter|analyze|begin|audit|checkpoint|close|cluster|comment|commit|copy|create|deallocate|declare|delete|drop|end|execute|explain|fetch|grant|insert|lock|move|noaudit|notify|prepare|reindex|rename|reset|revoke|rollback|savepoint|select|set|show|shutdown|start|truncate|unlisten|update|vacuum)",
        "\\b(tz_offset|to_timestamp_tz|bfilename)\\b",
        "(\\w*.=.*\\w*[\\s]*--)",
        "(?<![<\\[CDATA\\[\\]{2,}])(?i)(\\W\\.foo\\W|\\Wiso\\W|\\W\\.entity\\W|\\Wentity\\W|\\Wxxe\\W|\\Wxss\\W|\\Wdataformatas\\W|\\Wdatafld\\W|\\Wexpect\\w|\\Wsystem\\W|\\Wpublic\\W|\\Wsrc\\W|\\Wscript\\W|\\W<script\\W|\\Wroot\\W|\\Walert[^)]|\\Wrobots\\.\\W|\\Wmethodcall\\W|\\Wmethodname\\W|\\Wbase64\\W|\\Wexec\\W|\\Wupload\\W|\\Wspan\\W|\\Wvuln\\W|\\Wboot\\W|\\Wbash\\W|\\Wsh\\W|\\Wshadow\\W|\\Wpasswd\\W|\\Wetc\\W|\\Wencode\\W|\\Wwordpress\\W|\\Wxerosecurity\\W|\\Wcrowdshield\\W)(?![^\\[*\\[]])"
    );
    private static final List<Pattern> SQL_PATTERNS = buildSqlPatterns();

    private static List<Pattern> buildSqlPatterns() {
        return SQL_REGEXPS.stream().map(regex -> Pattern.compile(
            regex,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE))
           .toList();
    }

    /**
     * Проверка на sql инъекции
     * @param field поле для проверки
     * @return результат проверки
     */
    public static boolean isSqlSave(Object field) {
        if (StringUtils.isEmpty((CharSequence) field)) {
            return true;
        }

        return SQL_PATTERNS.stream().noneMatch(pattern -> {
            var matcher = pattern.matcher((CharSequence) field);
            return matcher.find();
        });
    }

}
