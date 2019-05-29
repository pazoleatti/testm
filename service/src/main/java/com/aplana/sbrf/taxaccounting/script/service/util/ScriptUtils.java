package com.aplana.sbrf.taxaccounting.script.service.util;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.joda.time.LocalDateTime;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString;

/**
 * Библиотека скриптовых функций
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public final class ScriptUtils {
    private static final Log LOG = LogFactory.getLog(ScriptUtils.class);

    public static final String DUL_REGEXP = "[^№]+\\s[^N№]+";
    // код страны - Россия
    public static final String COUNTRY_CODE_RUSSIA = "643";
    public static String DATE_FORMAT = "dd.MM.yyyy";

    /**
     * Запрещаем создавать экземляры класса
     */
    private ScriptUtils() {
    }

    /**
     * Округляет число до требуемой точности. Например, round(3.12345, 3) = 3.123, round(1.5, 0) = 2
     *
     * @param value     округляемое число
     * @param precision точность округления, знаки после запятой
     * @return округленное число
     */
    public static double round(double value, int precision) {
        double factor = Math.pow(10, precision);
        return Math.round(value * factor) / factor;
    }

    /**
     * Возвращает дату по строгому шаблону, иначе дата вида 01.13.2014 становится 01.01.2015
     *
     * @param format
     * @param value
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String format, String value) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(value);
        if (!simpleDateFormat.format(date).equals(value)) {
            throw new ParseException(String.format("Строка %s не соответствует формату даты %s", value, format), 0);
        }
        return date;
    }

    /**
     * Перевод даты в нужный формат
     *
     * @param date
     * @param format
     * @return
     */
    @SuppressWarnings("unused")
    public static String formatDate(Date date, String format) {
        if (date == null || format == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Получить индекс формата, для форматирования даты в Excel2007 в формат ДД.ММ.ГГГГ
     * Пример применения результата:
     * cellStyle.setDataFormat(createXlsDateFormat(workbook));
     * cell.setCellValue(new Date());
     * cell.setCellStyle(cellStyle);
     *
     * @param workbook объект книги xlsx
     * @return код формата по спецификации OpenXML
     */
    @SuppressWarnings("unused")
    public static short createXlsDateFormat(Workbook workbook) {
        String excelFormatPattern = DateFormatConverter.getJavaDatePattern(DateFormat.DEFAULT, new Locale("ru"));
        DataFormat poiFormat = workbook.createDataFormat();
        return poiFormat.getFormat(excelFormatPattern);
    }

    /**
     * Проверка формата введённых данных по регулярному выражению
     */
    public static boolean checkFormat(String enteredValue, String pat) {
        if (enteredValue == null || pat == null) {
            return false;
        }
        Pattern p = Pattern.compile(pat);
        Matcher m = p.matcher(enteredValue);
        return m.matches();
    }

    public static BigDecimal round(BigDecimal value) {
        return round(value, 0);
    }

    public static BigDecimal round(BigDecimal value, int precision) {
        if (value == null) {
            return null;
        }
        return (new BigDecimal(value.toString())).setScale(precision, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Проверка корректности СНИЛС
     */
    public static boolean checkSnils(String snils) {
        if (snils == null) {
            return false;
        }

        String number = snils.replaceAll("\\D", "");

        if (number.length() != 11) {
            return false;
        }

        Integer firstValue;
        Integer secondValue;

        try {
            firstValue = Integer.valueOf(number.substring(0, 9));
            secondValue = Integer.valueOf(number.substring(9, 11));
        } catch (NumberFormatException numberFormatException) {
            return false;
        }

        if (firstValue <= 1001998) {
            return true;
        }

        Integer controlSumm =
                9 * Integer.valueOf(number.substring(0, 1))
                        + 8 * Integer.valueOf(number.substring(1, 2))
                        + 7 * Integer.valueOf(number.substring(2, 3))
                        + 6 * Integer.valueOf(number.substring(3, 4))
                        + 5 * Integer.valueOf(number.substring(4, 5))
                        + 4 * Integer.valueOf(number.substring(5, 6))
                        + 3 * Integer.valueOf(number.substring(6, 7))
                        + 2 * Integer.valueOf(number.substring(7, 8))
                        + Integer.valueOf(number.substring(8, 9));

        Integer controlModSum = controlSumm % 101;

        if (controlModSum.equals(100)) {
            return secondValue.equals(0);
        }

        return secondValue.equals(controlSumm % 101);
    }

    /**
     * Проверка корректности ДУЛ
     */
    public static boolean checkDul(String dul) {
        if (dul == null) {
            return false;
        }

        if (!checkFormat(dul, DUL_REGEXP)) {
            return false;
        }

        return true;
    }

    /**
     * Сравнение двух значений справочника, производится преобразование числовых значений a к типу сравниваемого значения b (Integer или Long) , так как в RefBookValue все числа хранятся как BigDecimal
     * Ограничение: в текущей реализации нереализовано сравнение чисел с плавающей точкой
     *
     * @return
     */
    public static boolean equalsNullSafe(Object a, Object b) {
        boolean result;
        if (a == null && b == null) {
            result = true;
        } else if (a != null && b != null) {
            if (a instanceof Number) {
                Number anum = (Number) a;
                if (b instanceof Integer) {
                    return isEquals(anum.intValue(), b);
                } else if (b instanceof Long) {
                    return isEquals(anum.longValue(), b);
                } else {
                    throw new UnsupportedOperationException("The method 'equalsNullSafe' is not supported for arguments type " + a.getClass() + " and " + b.getClass());
                }
            } else {
                result = a.equals(b);
            }
        } else {
            result = false;
        }
        return result;
    }

    private static boolean isEquals(Object a, Object b) {
        return a.equals(b);
    }


    /**
     * Проверка заполнения графы, значение считаться незаполненным в том числе, если в ней указан "0"
     *
     * @param value
     * @return
     */
    public static boolean isEmpty(Object value) {

        if (value == null) {
            return true;
        }

        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            return bigDecimal.compareTo(BigDecimal.ZERO) == 0;
        } else if (value instanceof Integer) {
            Number number = (Number) value;
            return number.intValue() == 0;
        } else if (value instanceof Long) {
            Number number = (Number) value;
            return number.intValue() == 0;
        }
        if (value instanceof String) {
            String string = (String) value;
            return string.isEmpty();
        } else {
            throw new UnsupportedOperationException("The method 'isEmpty' is not supported for arguments type " + value.getClass());
        }
    }

    public static void checkInterrupted() {
        if (Thread.interrupted()) {
            LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
            throw new TAInterruptedException();
        }
    }

    /**
     * Проверка ИНН физического лица
     *
     * @see <a href="https://conf.aplana.com/pages/viewpage.action?pageId=27182953>Проверка ИНН физического лица</a>
     */
    public static String checkInn(String innValue) {
        if (innValue != null) {
            if (innValue.length() != 12) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") должно содержать 12 символов";
            }
            if (!checkFormat(innValue, "[0-9]{12}")) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") должно содержать только цифры";
            }
            if (checkFormat(innValue, "0+")) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") не должно содержать нули во всех разрядах";
            }
            if (Arrays.asList("00", "90", "93", "94", "95", "96", "98").contains(innValue.substring(0, 2))) {
                return "Значение гр. \"ИНН в РФ\" (\"" + innValue + "\") некорректно. Первые два разряда ИНН не могут быть равны одному из значений: \"00\",\"90\",\"93\",\"94\",\"95\",\"96\",\"98\"";
            }
            if (!RefBookUtils.checkControlSumInn(innValue)) {
                return "Некорректное контрольное число в значении гр. \"ИНН в РФ\" (\"" + innValue + "\")";
            }
        }
        return null;
    }

    /**
     * Проверка значения ИНН в РФ у данных о физлице из раздела 1 налоговой формы.
     */
    public static String checkNdflPersonInn(NdflPerson person) {
        String inn = person.getInnNp();
        String forPerson = String.format("Строка: %s, для ФЛ: %s, ИНП: %s ", person.getRowNum(), person.getFullName(), person.getInp());

        if (inn != null) {
            if (inn.length() != 12) {
                return forPerson + "значение параметра \"ИНН в РФ\" (\"" + inn + "\") должно содержать 12 символов";
            }
            if (!checkFormat(inn, "[0-9]{12}")) {
                return forPerson + "значение параметра \"ИНН в РФ\" (\"" + inn + "\") должно содержать только цифры";
            }
            if (checkFormat(inn, "0+")) {
                return forPerson + "значение параметра \"ИНН в РФ\" (\"" + inn + "\") не должно содержать нули во всех разрядах";
            }
            if (Arrays.asList("00", "90", "93", "94", "95", "96", "98").contains(inn.substring(0, 2))) {
                return forPerson + "значение параметра \"ИНН в РФ\" (\"" + inn + "\") некорректно. Первые два разряда ИНН не могут быть равны одному из значений: \"00\",\"90\",\"93\",\"94\",\"95\",\"96\",\"98\"";
            }
            if (!RefBookUtils.checkControlSumInn(inn)) {
                return forPerson + "указано некорректное контрольное число в значении \"ИНН в РФ\" (\"" + inn + "\")";
            }
        }
        return null;
    }

    public static List<String> checkFirstName(String firstName, String citizenship) {
        return checkName(firstName, citizenship, "Имя");
    }

    public static List<String> checkLastName(String lastName, String citizenship) {
        return checkName(lastName, citizenship, "Фамилия");
    }

    /**
     * Проверка допустимости имен и фамилий ФЛ.
     *
     * @param value       проверяемое имя или фамилия
     * @param citizenship код страны гражданства
     * @param attrName    что проверяем, для формирования сообщений об ошибке
     * @return если проверка не пройдена - сообщение об ошибке, иначе null
     */
    private static List<String> checkName(String value, String citizenship, String attrName) {

        List<String> errorMessages = new ArrayList<>();

        if (value != null && !value.isEmpty()) {
            if (citizenship != null && citizenship.equals(COUNTRY_CODE_RUSSIA)) {
                // для российских проверяем на кириллицу
                if (!checkFormat(value, "^[а-яА-ЯёЁ -]+")) {
                    errorMessages.add("Значение параметра \"" + attrName + "\" (\"" + value +
                            "\") содержит недопустимые символы. Значение может содержать только буквы русского алфавита (кириллица), пробелы и дефисы");
                }
            } else {
                // для иностранцев может быть латиница
                if (!checkFormat(value, "^[a-zA-Zа-яА-ЯёЁ '-]+")) {
                    errorMessages.add("Значение параметра \"" + attrName + "\" (\"" + value +
                            "\") содержит недопустимые символы. Значение может содержать только буквы русского (кириллица) или латинского алфавитов, пробелы, апострофы и дефисы");
                }
            }
            // проверяем первый символ
            String firstSymbol = value.substring(0, 1).toLowerCase();
            if (Arrays.asList(" ", "ь", "ъ", "-").contains(firstSymbol)) {
                errorMessages.add("Значение параметра \"" + attrName + "\" (\"" + value +
                        "\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел");
            }
        }
        return errorMessages;
    }

    public static String checkDul(String code, String value, String attrName) {
        String format = null;
        String formatStr = null;
        String zeroFormat = null;
        if (code.equals("01")) {
            format = "[^\\wА-яа-яЁё]*([IVXLC][^\\wА-яа-яЁё]*)([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6}";
            formatStr = "\"R-ББ 999999\", где R - римское число, заданное символами \"I\", \"V\", \"X\", \"L\", \"C\", набранными на верхнем регистре латинской клавиатуры; Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная). Представление римских чисел только через латинскую клавиатуру";
        } else if (code.equals("02")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){8,9}";
            formatStr = "\"99 0999999\", где 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        } else if (code.equals("04")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        } else if (code.equals("06")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6}";
            formatStr = "\"ББ 999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("09")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){9}";
            formatStr = "\"99 9999999\", где 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("19")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){7}";
            formatStr = "\"ББ 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
        } else if (code.equals("21")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){10}";
            formatStr = "\"99 99 999999\", где 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*(0[^\\wА-яа-яЁё]*){10}";
        } else if (code.equals("22")) {
            format = "[^\\wА-яа-яЁё]*([0-9][^\\wА-яа-яЁё]*){9}";
            formatStr = "\"99 9999999\", где 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*(0[^\\wА-яа-яЁё]*){9}";
        } else if (code.equals("07")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){6,7}";
        } else if (code.equals("18")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){10}";
            formatStr = "\"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){10}";
        } else if (code.equals("24")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){7}";
            formatStr = "\"ББ 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){7}";
        } else if (code.equals("26")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
        } else if (code.equals("27")) {
            format = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}([0-9][^\\wА-яа-яЁё]*){6,7}";
            formatStr = "\"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)";
            zeroFormat = "[^\\wА-яа-яЁё]*([А-ЯЁ][^\\wА-яа-яЁё]*){2}(0[^\\wА-яа-яЁё]*){6,7}";
        } else if (code.equals("91") && isUSSRIdDoc(value)) {
            return "Значение гр. 11 ДУЛ Номер (\"" + value + "\") содержит реквизиты паспорта гражданина СССР. Паспорт гражданина СССР не является актуальным документом, удостоверяющим личность";
        } else {
            format = ".{1,25}";
            formatStr = "";
        }
        if (format != null && !checkFormat(value, format)) {
            return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не соответствует формату " + formatStr;
        }
        if (zeroFormat != null && checkFormat(value, zeroFormat)) {
            return "Значение гр. \"" + attrName + "\" (\"" + value + "\") не должно быть нулевым";
        }
        return null;
    }

    public static boolean isUSSRIdDoc(String number) {
        return checkFormat(number, "[IiVvXxLlCcУуХхЛлСс1]*-[А-ЯЁ]{2} [0-9]{6}");
    }

    public static String formatDocNumber(String code, String value) {
        StringBuilder formattedValue = new StringBuilder(value);
        switch (code) {
            case "21": {
                return formattedValue.insert(2, " ")
                        .insert(5, " ").toString();
            }
            case "07": {
                return formattedValue.insert(2, " ").toString();
            }
            case "18": {
                return formattedValue.insert(2, "-")
                        .insert(6, " ").toString();
            }
            case "24": {
                return formattedValue.insert(2, " ").toString();
            }
        }
        return value;
    }

    ;

    /**
     * Проверяет соответствие ДУЛ формату с удаленными разделителями.
     *
     * @param code
     * @param value
     * @return
     */
    public static boolean checkDulSymbols(String code, String value) {
        boolean pass = true;
        String format = null;
        if (code.equals("21")) {
            format = "[0-9]{10}";
        } else if (code.equals("22")) {
            format = "[0-9]{9}";
        } else if (code.equals("07")) {
            format = "[А-ЯЁ]{2}[0-9]?[0-9]{6}";
        } else if (code.equals("18")) {
            format = "[А-ЯЁ]{2}[0-9]{10}";
        } else if (code.equals("24")) {
            format = "[А-ЯЁ]{2}[0-9]{7}";
        } else if (code.equals("27")) {
            format = "[А-ЯЁ]{2}[0-9]?[0-9]{6}";
        }
        if (!checkFormat(value, format)) {
            pass = false;
        }
        return pass;
    }

    public static String calcTimeMillis(long time) {
        long currTime = System.currentTimeMillis();
        return (currTime - time) + " мс)";
    }

    public static String formatDate(Object date) {
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).toString(DATE_FORMAT);
        } else {
            return ScriptUtils.formatDate((Date) date, DATE_FORMAT);
        }
    }

    /**
     * Рассчитывает UUID для объекта дохода на основе состояния полей объекта операции дохода. Данное значение UUID будет являться
     * отпечатком объекта. Архитектура UUID основана на стандарте RFC 4122. По этому стандарту UUID моет быть использована в т.ч для хеширования:
     * Мы используем версию 3 UUID - это значит что UUID генерируется на основе значения с использованием алгоритма MD5.
     * В этом методе из набора полей объекта дохода строится массив байтов. И из этого массива байтов вычисляется отпечаток,
     * который будет эквивалентен для одинаковых массивов байтов и различаться для разных массивов байтов.
     *
     * @param income объект операции дохода
     * @return строковое представление UUID объекта дохода
     */
    public static String getConsolidationIncomeUUID(ConsolidationIncome income) {
        int size = 0;
        byte[] inp = stringToByteArray(income.getInp());
        size += inp.length;
        byte[] incomeCode = stringToByteArray(income.getIncomeCode());
        size += incomeCode.length;
        byte[] incomeType = stringToByteArray(income.getIncomeType());
        size += incomeType.length;
        byte[] incomeAccruedDate = dateToByteArray(income.getIncomeAccruedDate());
        size += incomeAccruedDate.length;
        byte[] incomePayoutDate = dateToByteArray(income.getIncomePayoutDate());
        size += incomePayoutDate.length;
        byte[] kpp = stringToByteArray(income.getKpp());
        size += kpp.length;
        byte[] oktmo = stringToByteArray(income.getOktmo());
        size += oktmo.length;
        byte[] incomeAccruedSumm = bigDecimalToByteArray(income.getIncomeAccruedSumm());
        size += incomeAccruedSumm.length;
        byte[] incomePayoutSumm = bigDecimalToByteArray(income.getIncomePayoutSumm());
        size += incomePayoutSumm.length;
        byte[] totalDeductionsSumm = bigDecimalToByteArray(income.getTotalDeductionsSumm());
        size += totalDeductionsSumm.length;
        byte[] taxBase = bigDecimalToByteArray(income.getTaxBase());
        size += taxBase.length;
        byte[] taxRate = intToByteArray(income.getTaxRate());
        size += taxRate.length;
        byte[] taxDate = dateToByteArray(income.getTaxDate());
        size += taxDate.length;
        byte[] calculatedTax = bigDecimalToByteArray(income.getCalculatedTax());
        size += calculatedTax.length;
        byte[] withHoldingTax = bigDecimalToByteArray(income.getWithholdingTax());
        size += withHoldingTax.length;
        byte[] notHoldingTax = bigDecimalToByteArray(income.getNotHoldingTax());
        size += notHoldingTax.length;
        byte[] overholdingTax = bigDecimalToByteArray(income.getOverholdingTax());
        size += overholdingTax.length;
        byte[] refoundTax = longToByteArray(income.getRefoundTax());
        size += refoundTax.length;
        byte[] taxTransferDate = dateToByteArray(income.getTaxTransferDate());
        size += taxTransferDate.length;
        byte[] paymentDate = dateToByteArray(income.getPaymentDate());
        size += paymentDate.length;
        byte[] paymentNumber = stringToByteArray(income.getPaymentNumber());
        size += paymentNumber.length;
        byte[] taxSumm = bigDecimalToByteArray(income.getTaxSumm());
        size += taxSumm.length;

        int offset = 0;
        byte[] resultArray = new byte[size];
        mergeByteArrays(inp, resultArray, offset);
        offset += inp.length;
        mergeByteArrays(incomeCode, resultArray, offset);
        offset += incomeCode.length;
        mergeByteArrays(incomeType, resultArray, offset);
        offset += incomeType.length;
        mergeByteArrays(incomeAccruedDate, resultArray, offset);
        offset += incomeAccruedDate.length;
        mergeByteArrays(incomePayoutDate, resultArray, offset);
        offset += incomePayoutDate.length;
        mergeByteArrays(kpp, resultArray, offset);
        offset += kpp.length;
        mergeByteArrays(oktmo, resultArray, offset);
        offset += oktmo.length;
        mergeByteArrays(incomeAccruedSumm, resultArray, offset);
        offset += incomeAccruedSumm.length;
        mergeByteArrays(incomePayoutSumm, resultArray, offset);
        offset += incomePayoutSumm.length;
        mergeByteArrays(totalDeductionsSumm, resultArray, offset);
        offset += totalDeductionsSumm.length;
        mergeByteArrays(taxBase, resultArray, offset);
        offset += taxBase.length;
        mergeByteArrays(taxRate, resultArray, offset);
        offset += taxRate.length;
        mergeByteArrays(taxDate, resultArray, offset);
        offset += taxDate.length;
        mergeByteArrays(calculatedTax, resultArray, offset);
        offset += calculatedTax.length;
        mergeByteArrays(withHoldingTax, resultArray, offset);
        offset += withHoldingTax.length;
        mergeByteArrays(notHoldingTax, resultArray, offset);
        offset += notHoldingTax.length;
        mergeByteArrays(overholdingTax, resultArray, offset);
        offset += overholdingTax.length;
        mergeByteArrays(refoundTax, resultArray, offset);
        offset += refoundTax.length;
        mergeByteArrays(taxTransferDate, resultArray, offset);
        offset += taxTransferDate.length;
        mergeByteArrays(paymentDate, resultArray, offset);
        offset += paymentDate.length;
        mergeByteArrays(paymentNumber, resultArray, offset);
        offset += paymentNumber.length;
        mergeByteArrays(taxSumm, resultArray, offset);

        return UUID.nameUUIDFromBytes(resultArray).toString();
    }

    /**
     * Преобразует дату в массив байтов на основе её long значения
     *
     * @param d дата
     * @return массив байтов
     */
    private static byte[] dateToByteArray(Date d) {
        if (d != null) {
            return longToByteArray(d.getTime());
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует число {@code BigDecimal} в массив байтов на основе его String представления
     *
     * @param d число
     * @return массив байтов
     */
    private static byte[] bigDecimalToByteArray(BigDecimal d) {
        if (d != null) {
            return stringToByteArray(d.toString());
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует строку в массив байтов
     *
     * @param s строка
     * @return массив байтов
     */
    private static byte[] stringToByteArray(String s) {
        if (s != null) {
            return s.toLowerCase().getBytes(Charset.forName("UTF-8"));
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует число long в массив байтов, для каждый из 8 байтов числа помещается в массив
     *
     * @param l число
     * @return массив байтов
     */
    private static byte[] longToByteArray(Long l) {
        if (l != null) {
            byte[] toReturn = new byte[8];
            long primitiveL = l.longValue();
            toReturn[7] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[6] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[5] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[4] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[3] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[2] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[1] = (byte) primitiveL;
            primitiveL >>>= 8;
            toReturn[0] = (byte) primitiveL;
            return toReturn;
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Преобразует число int в массив байтов, для каждый из 4 байтов числа помещается в массив
     *
     * @return массив байтов
     */
    private static byte[] intToByteArray(Integer i) {
        if (i != null) {
            byte[] toReturn = new byte[4];
            int primitiveI = i.intValue();
            toReturn[3] = (byte) primitiveI;
            primitiveI >>>= 8;
            toReturn[2] = (byte) primitiveI;
            primitiveI >>>= 8;
            toReturn[1] = (byte) primitiveI;
            primitiveI >>>= 8;
            toReturn[0] = (byte) primitiveI;
            return toReturn;
        }
        return new byte[]{(byte) 0xFF};
    }

    /**
     * Сливает массивы байтов, таким образом, чтобы slave прицеплялся к хвосту master
     *
     * @param slave  массив источник
     * @param master массив к которому прицепляют источник
     * @param offset позиция массива master к которой прицепляют источник
     */
    private static void mergeByteArrays(byte[] slave, byte[] master, int offset) {
        System.arraycopy(slave, 0, master, offset, slave.length);
    }

    /**
     * Создает группу Cell
     *
     * @param columns
     * @param styles
     * @return
     */
    public static List<Cell> createCells(List<Column> columns, List<FormStyle> styles) {
        List<Cell> cells = new ArrayList<Cell>();
        for (Column column : columns) {
            cells.add(new Cell(column, styles));
        }
        return cells;
    }
}