package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Класс для формирования стандартных запросов для провайдеров данных справочников.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 08.10.13 14:39
 */
@Repository
public class RefBookUtils extends AbstractDao {

    public static final String INN_JUR_PATTERN = "([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}";
    public static final String INN_JUR_MEANING = "Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9)";
    public static final String INN_IND_PATTERN = "([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{10}";
    public static final String INN_IND_MEANING = "Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 10 символов: (0-9)";
    public static final String KPP_PATTERN = "([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})";
    public static final String KPP_MEANING = "Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9)";
    public static final String TAX_ORGAN_PATTERN = "[0-9]{4}";
    public static final String TAX_ORGAN_MEANING = "Все 4 символа: (0-9)";

    public static List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, Map<String, RefBookValue> record) {
        List<String> errors = new ArrayList<String>();
        for (RefBookAttribute a : attributes) {
            if (a.isRequired() && (!record.containsKey(a.getAlias()) || record.get(a.getAlias()).isEmpty())) {
                errors.add(a.getName());
            }
        }
        return errors;
    }

    public static List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        List<String> errors = new ArrayList<String>();
        for (RefBookRecord record : records) {
            errors.addAll(checkFillRequiredRefBookAtributes(attributes, record.getValues()));
        }
        return errors;
    }

    public static List<String> checkRefBookAtributeValues(List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        List<String> errors = new ArrayList<String>();
        for (RefBookRecord record : records) {
            Map<String, RefBookValue> values = record.getValues();
            for (RefBookAttribute a : attributes) {
                RefBookValue value = values.get(a.getAlias());

                //Проверка для иерархичных справочников
                if (record.getUniqueRecordId() != null && a.getAlias().equals(RefBook.RECORD_PARENT_ID_ALIAS)) {
                    Long parentId = value.getReferenceValue();
                    if (record.getUniqueRecordId().equals(parentId)) {
                        errors.add("Запись справочника не может быть родительской сама для себя!");
                    }
                }

                if (a.getAttributeType().equals(RefBookAttributeType.STRING) && value.getStringValue() != null && a.getMaxLength() != null && value.getStringValue().length() > a.getMaxLength()) {
                    errors.add("\"" + a.getName() + "\": значение атрибута превышает максимально допустимое " + a.getMaxLength() + "!");
                }

                if (a.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                    Number number = value.getNumberValue();
                    if (number == null) {
                        continue;
                    }

                    BigDecimal bigDecimal;
                    if (number instanceof BigDecimal) {
                        bigDecimal = (BigDecimal) (value.getNumberValue());
                        String valStr = bigDecimal.toPlainString();
                        if (valStr.contains(".")) {
                            bigDecimal = new BigDecimal(valStr.replaceAll("()(0+)(e|$)", "$1$3"));
                        }
                    } else {
                        bigDecimal = new BigDecimal(number.toString());
                    }

                    int fractionalPart = bigDecimal.scale();
                    int integerPart = bigDecimal.precision();
                    integerPart = fractionalPart < integerPart ? (integerPart - fractionalPart) : 0;
                    fractionalPart = fractionalPart < 0 ? 0 : fractionalPart;

                    Integer maxLength = a.getMaxLength();
                    Integer precision = a.getPrecision();

                    // предполагается, что (maxLength - precision) <= 17
                    if (fractionalPart > precision || integerPart > (maxLength - precision)) {
                        errors.add("\"" + a.getName() + "\": значение атрибута не соответствует формату: максимальное количество цифр " + maxLength + ", максимальная точность " + precision);
                    }
                }
            }
        }

        return errors;
    }

    public List<Long> getParentsHierarchy(String tableName, Long uniqueRecordId) {
        String sql = String.format("select ID from %s where level != 1 start with id = ? connect by prior parent_id = id order by level desc", tableName);
        try {
            return getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return SqlUtils.getLong(rs, "ID");
                }
            }, uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    public static class RecordVersionMapper implements RowMapper<RefBookRecordVersion> {

        @Override
        public RefBookRecordVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookRecordVersion result = new RefBookRecordVersion();
            result.setRecordId(SqlUtils.getLong(rs, RefBook.RECORD_ID_ALIAS));
            result.setVersionStart(rs.getDate("versionStart"));
            result.setVersionEnd(rs.getDate("versionEnd"));
            result.setVersionEndFake(rs.getBoolean("endIsFake"));
            return result;
        }
    }

    /**
     * Проверка контрольной суммы ИНН (физлица или организации)
     * @param inn ИНН в виде строки
     * @return результат проверки (успешная или нет)
     */
    public static boolean checkControlSumInn(String inn) {
        if (inn == null) {
            return false;
        }
        if (inn.length() == 10) {
            int[] koefArray10 = new int[]{2, 4, 10, 3, 5, 9, 4, 6, 8};
            int sum10 = 0;
            for (int i = 0; i < 9; i++) {
                if (!Character.isDigit(inn.charAt(i))){
                    return false;
                }
                sum10 += koefArray10[i] * Character.getNumericValue(inn.charAt(i));
            }
            return (sum10 % 11) % 10 == Character.getNumericValue(inn.charAt(9));
        } else if (inn.length() == 12){
            int[] koefArray11 = new int[]{7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
            int[] koefArray12 = new int[]{3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
            int sum11, sum12;
            sum11 = sum12 = 0;
            for (int i = 0; i < 10; i++) {
                if (!Character.isDigit(inn.charAt(i))){
                    return false;
                }
                sum11 += koefArray11[i] * Character.getNumericValue(inn.charAt(i));
                sum12 += koefArray12[i] * Character.getNumericValue(inn.charAt(i));
            }
            sum12 += koefArray12[10] * Character.getNumericValue(inn.charAt(10));
            return (sum11 % 11) % 10 == Character.getNumericValue(inn.charAt(10)) &&
                    (sum12 % 11) % 10 == Character.getNumericValue(inn.charAt(11));
        }
        return false;
    }
}
