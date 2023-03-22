package io.github.sinri.keel.helper;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * @since 2.6
 */
public class KeelDateTimeHelper {
    private static final KeelDateTimeHelper instance = new KeelDateTimeHelper();

    private KeelDateTimeHelper() {

    }

    static KeelDateTimeHelper getInstance() {
        return instance;
    }

    /**
     * @return current timestamp expressed in MySQL Date Time Format
     * @since 3.0.0
     */
    public String getCurrentDateExpression() {
        return getCurrentDateExpression("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * @param format "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc. if null, return null
     * @return the date string or null
     * @since 2.6
     */
    public String getCurrentDateExpression(String format) {
        Date currentTime = new Date();
        return getDateExpression(currentTime, format);
    }

    /**
     * @param format for example: yyyy-MM-ddTHH:mm:ss
     * @since 2.6
     */
    public String getDateExpression(Date date, String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * From MySQL DataTime String to Standard Expression
     *
     * @param localDateTimeExpression yyyy-MM-ddTHH:mm:ss
     * @return yyyy-MM-dd HH:mm:ss
     * @since 2.7
     */
    public String getMySQLFormatLocalDateTimeExpression(String localDateTimeExpression) {
        return LocalDateTime.parse(localDateTimeExpression)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * @return Date Time in RFC 1123: Mon, 31 Oct 2022 01:18:43 GMT
     * @since 2.9.1
     */
    public String getGMTDateTimeExpression() {
        DateTimeFormatter gmt = DateTimeFormatter.ofPattern(
                        "EEE, dd MMM yyyy HH:mm:ss z",
                        Locale.ENGLISH
                )
                .withZone(ZoneId.of("GMT"));
        return gmt.format(LocalDateTime.now(ZoneId.systemDefault()));
    }
}
