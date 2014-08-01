package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Класс для формирования стандартных запросов для провайдеров данных справочников.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 08.10.13 14:39
 */
@Repository
public class RefBookUtils extends AbstractDao {

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
        String okatoRegex = "\\d{11}";
        String codeTSRegex = "\\d{3}(\\d{2}|[?]{2})";
        Pattern okatoPattern = Pattern.compile(okatoRegex);
        Pattern codeTSPattern = Pattern.compile(codeTSRegex);
        for (RefBookRecord record : records) {
            Map<String, RefBookValue> values = record.getValues();
            for (RefBookAttribute a : attributes) {
                RefBookValue value = values.get(a.getAlias());
                //Должны содержать только цифры - Код валюты. Цифровой, Определяющая часть кода ОКАТО, Определяющая часть кода ОКТМО, Цифровой код валюты выпуска
                if ((a.getId() == 64L || a.getId() == 12L) &&
                        (value != null && !NumberUtils.isNumber(value.getStringValue()) || a.isRequired() && value == null)) {
                    //TODO добавить еще Определяющая часть кода ОКТМО
                    errors.add("Значение атрибута «" + a.getName() + "» должно содержать только цифры!");
                }

                //Проверка формата для кода окато
                if ((a.getId() == 7L) && !okatoPattern.matcher(values.get(a.getAlias()).getStringValue()).matches()) {
                    errors.add("Значение атрибута «" + a.getName() + "» должно быть задано в формате ×××××××××××, где × - цифра!");
                }

                //Проверка формата для кода ТС
                if ((a.getId() == 411L) && !codeTSPattern.matcher(values.get(a.getAlias()).getStringValue()).matches()) {
                    errors.add("Значение атрибута «" + a.getName() + "» должно быть задано в формате ××××× или ***??, где × - цифра!");
                }

                //Проверка для иерархичных справочников
                if (record.getRecordId() != null && a.getAlias().equals(RefBook.RECORD_PARENT_ID_ALIAS)) {
                    Long parentId = values.get(a.getAlias()).getReferenceValue();
                    if (record.getRecordId().equals(parentId)) {
                        errors.add("Элемент справочника не может быть родительским для самого себя!");
                    }
                }

                /*if (a.getId() == 164L && !Arrays.asList(DepartmentType.values()).contains(DepartmentType.fromCode(value.getNumberValue().intValue()))){
                   errors.add("Атрибута справочника \"Тип подразделенеия\" должно принимать одно из значений: 1,2,3,4,5");
                }*/

                if (a.getAttributeType().equals(RefBookAttributeType.STRING) && value.getStringValue() != null && a.getMaxLength() != null && value.getStringValue().length() > a.getMaxLength()) {
                    errors.add("\"" + a.getName() + "\": значение атрибута превышает максимально допустимое " + a.getMaxLength() + "!");
                }

                if ((a.getId() == 161L || a.getId() == 162L) && values.get(a.getAlias()).getStringValue() != null && values.get(a.getAlias()).getStringValue().contains("/")) {
                    errors.add("Значение атрибута «" + a.getName() + "» не должно содержать символ «/»!");
                }

                if (a.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                    Number number = values.get(a.getAlias()).getNumberValue();
                    if (number == null) {
                        continue;
                    }

                    BigDecimal bigDecimal;
                    if (number instanceof BigDecimal) {
                        bigDecimal = (BigDecimal) (values.get(a.getAlias()).getNumberValue());
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

                    // пердпологается, что (maxLength - precision) <= 17
                    if (fractionalPart > precision || integerPart > (maxLength - precision)) {
                        errors.add("Значение атрибута «" + a.getName() + "» не соответствует формату (" + maxLength + ", " + precision + ")");
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
}
