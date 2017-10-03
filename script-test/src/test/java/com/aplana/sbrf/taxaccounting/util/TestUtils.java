package com.aplana.sbrf.taxaccounting.util;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;

/**
 * @author Andrey Drunk
 */
public class TestUtils {

    public static LocalDateTime toDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormat.forPattern("dd.MM.yyyy"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Чтение из файла в строку
     */
    public static String readFile(String path, String charset) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            Reader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[10240];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            stream.close();
        }
    }

    public static String convertToUnderlineStyle(String camelCaseString) {
        StringBuilder underlineBuilder = new StringBuilder();
        char[] asArray = camelCaseString.toCharArray();
        underlineBuilder.append(String.valueOf(asArray[0]).toLowerCase());
        for (int i = 1; i < asArray.length; i++) {
            if (Character.isUpperCase(asArray[i])) {
                underlineBuilder.append('_');
                underlineBuilder.append(String.valueOf(asArray[i]).toLowerCase());
            } else if (Character.isDigit(asArray[i])) {
                underlineBuilder.append('_');
                underlineBuilder.append(asArray[i]);
            } else {
                underlineBuilder.append(asArray[i]);
            }
        }
        return underlineBuilder.toString();
    }

}
