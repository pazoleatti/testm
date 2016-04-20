package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Хэлпер для чтения csv-файлов со справочными данными
 * Данные необходимы для эмуляции справочных данных
 *
 * @author Levykin
 */
public class RefBookReadHelper {
    private static final String FILE_NAME_PREFIX = "ref_book_";
    private static final String FILE_NAME_EXT = ".csv";

    private static final ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    /**
     * Получение структуры:
     * Id справочника → record_id → Записи справочника
     */
    public static Map<Long, Map<Long, Map<String, RefBookValue>>> readFromFolder(URL folderUrl) throws IOException, ParseException {
        Map<Long, Map<Long, Map<String, RefBookValue>>> retVal = new HashMap<Long, Map<Long, Map<String, RefBookValue>>>();
        File folder = new File(folderUrl.getPath());
        for (String fileName : folder.list()) {
            String fileNameLower = fileName.toLowerCase();
            if (fileNameLower.startsWith(FILE_NAME_PREFIX) && fileNameLower.endsWith(FILE_NAME_EXT)) {
                long refBookId = Long.parseLong(fileNameLower.substring(fileNameLower.indexOf(FILE_NAME_PREFIX)
                        + FILE_NAME_PREFIX.length(), fileNameLower.indexOf(FILE_NAME_EXT)));
                retVal.put(refBookId, getRecords(folder.getPath() + "/" + fileNameLower));
            }
        }
        return retVal;
    }

    /**
     * record_id → Записи справочника
     */
    private static Map<Long, Map<String, RefBookValue>> getRecords(String path) throws IOException, ParseException {
        Map<Long, Map<String, RefBookValue>> retVal = new HashMap<Long, Map<String, RefBookValue>>();
        String string = TestScriptHelper.readFile(path, "UTF-8");
        String[] rows = string.split("\n");
        String firstRow = rows[0];
        String[] attributes = firstRow.split(";");
        Map<Integer, RefBookAttributeType> typeMap = new HashMap<Integer, RefBookAttributeType>();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].indexOf("(") == -1 || attributes[i].indexOf(")") == -1) {
                throw new ServiceException("Wrong reference book format in file \"" + path + "\"");
            }
            String nameStr = attributes[i].substring(0, attributes[i].indexOf("("));
            char typeChar = attributes[i].charAt(attributes[i].indexOf("(") + 1);
            RefBookAttributeType type;
            switch (typeChar) {
                case 'R':
                    type = RefBookAttributeType.REFERENCE;
                    break;
                case 'N':
                    type = RefBookAttributeType.NUMBER;
                    break;
                case 'S':
                    type = RefBookAttributeType.STRING;
                    break;
                case 'D':
                    type = RefBookAttributeType.DATE;
                    break;
                default:
                    throw new ServiceException("Unknown column type \"%s\"!", typeChar);
            }
            attributes[i] = nameStr;
            typeMap.put(i, type);
        }

        for (int i = 1; i < rows.length; i++) {
            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
            String[] rowValues = rows[i].split(";");
            if (rowValues.length != attributes.length) {
                throw new ServiceException("Row column count = %d, header row count = %d!", rowValues.length, attributes.length);
            }
            for (int j = 0; j < attributes.length; j++) {
                Object value;
                String row = rowValues[j].trim();
                switch (typeMap.get(j)) {
                    case REFERENCE:
                        value = Long.valueOf(row);
                        break;
                    case NUMBER:
                        value = BigDecimal.valueOf(Double.valueOf(row));
                        break;
                    case STRING:
                        value = row;
                        break;
                    case DATE:
                        // TODO Возможно потребуется поддержка других форматов (можно брать из content.xml)
                        value = simpleDateFormat.get().parse(row);
                        break;
                    default:
                        throw new ServiceException("Unknown column type \"%s\"!", row);
                }
                map.put(attributes[j], new RefBookValue(typeMap.get(j), value));
            }
            retVal.put(map.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), map);
        }
        return retVal;
    }
}
