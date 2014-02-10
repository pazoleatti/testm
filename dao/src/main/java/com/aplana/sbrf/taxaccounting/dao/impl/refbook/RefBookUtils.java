package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для формирования стандартных запросов для провайдеров данных справочников.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 08.10.13 14:39
 */
@Repository
public class RefBookUtils extends AbstractDao {

	@Autowired
	private RefBookDao refBookDao;

    @Autowired
    private ApplicationContext applicationContext;

    private static final String WITH_STATEMENT =
            "with t as (select\n" +
                    "  max(version) version, record_id\n" +
                    "from\n" +
                    "  %s\n" +
                    "where\n" +
                    "  status = 0 and version <= ?\n" +
                    "group by\n" +
                    "  record_id)\n";

    private static final String RECORD_VERSIONS_STATEMENT =
            "with currentRecord as (select id, record_id, version from %s where id=?),\n" +
                    "recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r, currentRecord cr where r.RECORD_ID=cr.RECORD_ID), \n" +
                    "t as (select rv.rn as row_number_over, rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     * @param tableName название таблицы для которой формируется запрос
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника (фактически поле ID). Используется только при получении всех версий записи
     * @param version дата актуальности, по которой определяется период актуальности и соответственно версия записи, которая в нем действует
     *                Если = null, значит будет выполняться получение всех версий записи
     *                Иначе выполняется получение всех записей справочника, активных на указанную дату
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @param whereClause
     * @return
     */
	public PreparedStatementData getSimpleQuery(String tableName, RefBook refBook, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
			String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
        PreparedStatementData ps = new PreparedStatementData();

        if (version != null) {
            ps.appendQuery(String.format(WITH_STATEMENT, tableName));
            ps.addParam(version);
        } else {
            ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT, tableName, tableName));
            ps.addParam(uniqueRecordId);
            ps.addParam(VersionedObjectStatus.NORMAL.getId());
        }

		ps.appendQuery("SELECT ");
		ps.appendQuery("r.id ");
		ps.appendQuery(RefBook.RECORD_ID_ALIAS);

        if (version == null) {
            ps.appendQuery(",\n");
            ps.appendQuery("  t.version as ");
            ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
            ps.appendQuery(",\n");

            ps.appendQuery("  t.versionEnd as ");
            ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
        }

		for (RefBookAttribute attribute : refBook.getAttributes()) {
			ps.appendQuery(", ");
			ps.appendQuery(attribute.getAlias());
		}

        if (version != null) {
            ps.appendQuery(" FROM t, (SELECT ");
            if (isSupportOver()) {
                RefBookAttribute defaultSort = refBook.getSortAttribute();
                String sortColumn = sortAttribute == null ? (defaultSort == null ? "id" : defaultSort.getAlias()) : sortAttribute.getAlias();
                String sortDirection = isSortAscending ? "ASC" : "DESC";
                ps.appendQuery("row_number() over (order by '" + sortColumn + "' "+sortDirection+") as row_number_over");
            } else {
                ps.appendQuery("rownum row_number_over");
            }
            ps.appendQuery(", t.* FROM ");
            ps.appendQuery(tableName);
            ps.appendQuery(" t ) r");
        } else {
            ps.appendQuery(" FROM t, ");
            ps.appendQuery(tableName);
            ps.appendQuery(" r");
        }

		PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener =  applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

		Filter.getFilterQuery(filter, simpleFilterTreeListener);
		if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE ");
			ps.appendQuery(filterPS.getQuery().toString());
			if (filterPS.getParams().size() > 0) {
				ps.addParam(filterPS.getParams());
			}
		}
		if (whereClause != null && whereClause.trim().length() > 0) {
			if (filterPS.getQuery().length() > 0) {
				ps.appendQuery(" AND ");
			} else {
				ps.appendQuery(" WHERE ");
			}
			ps.appendQuery(whereClause);
		}

        ps.appendQuery(" WHERE (r.version = t.version and r.record_id = t.record_id)");

		if (pagingParams != null) {
            ps.appendQuery(" and row_number_over BETWEEN ? AND ?");
			ps.addParam(pagingParams.getStartIndex());
			ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
		}

        if (version == null) {
            ps.appendQuery(" order by t.version\n");
        }
		return ps;
	}

    public PreparedStatementData getSimpleQuery(String tableName, RefBook refBook, Date version, Long uniqueRecordId, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams, String whereClause) {
        return getSimpleQuery(tableName, refBook, uniqueRecordId, version, sortAttribute, filter, pagingParams, true, whereClause);
    }

    /**
     * Загружает данные справочника на определенную дату актуальности
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param whereClause дополнительный фильтр для секции WHERE
     * @return
     */
	public PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause) {
		RefBook refBook = refBookDao.get(refBookId);
		// получаем страницу с данными
		PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, version, sortAttribute, filter, pagingParams, isSortAscending, whereClause);
        List<Map<String, RefBookValue>> records = getRecordsData(ps, refBook);
		PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getSimpleQuery(tableName, refBook, null, version, sortAttribute, filter, null, isSortAscending, whereClause);
        result.setTotalCount(getRecordsCount(ps));
        return result;
	}

    /**
     * Перегруженная функция с восходящей сортировкой по умолчанию
     *
     * @param refBookId
     * @param tableName
     * @param pagingParams
     * @param filter
     * @param sortAttribute
	 * @param whereClause
     * @return
     */
    public PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, String whereClause) {
        return getRecords(tableName, refBookId, version, pagingParams, filter, sortAttribute, true, whereClause);
    }

	/**
	 * Возвращает элементы справочника
	 * @param ps
	 * @param refBook
	 * @return
	 */
	private List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook) {
		if (ps.getParams().size() > 0) {
			return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
		} else {
			return getJdbcTemplate().query(ps.getQuery().toString(), new RefBookValueMapper(refBook));
		}
	}

	/**
	 * Возвращает количество записей в выборке
	 * @param ps
	 * @return
	 */
	private Integer getRecordsCount(PreparedStatementData ps) {
		if (ps.getParams().size() > 0) {
			return getJdbcTemplate().queryForInt("select count(*) from (" + ps.getQuery().toString() + ")", ps.getParams().toArray());
		} else {
			return getJdbcTemplate().queryForInt("select count(*) from (" + ps.getQuery().toString() + ")");
		}
	}

    /**
     * Изменение периода актуальности для указанной версии
     * @param tableName название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @param version новая дата начала актуальности
     */
    public void updateVersionRelevancePeriod(String tableName, Long uniqueRecordId, Date version){
        getJdbcTemplate().update(String.format("update %s set version=? where id=?", tableName), version, uniqueRecordId);
    }

    private final static String CHECK_REFERENCE_VERSIONS = "select count(*) from ref_book_record where VERSION < ? and ID in (%s)";

    /**
     * Проверка ссылочных атрибутов. Их дата начала актуальности должна быть больше либо равна дате актуальности новой версии
     * @param versionFrom дата актуальности новой версии
     * @param attributes атрибуты справочника
     * @param records новые значения полей элемента справочника
     * @return ссылочные атрибуты в порядке?
     */
    public boolean isReferenceValuesCorrect(String tableName, Date versionFrom, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        if (attributes.size() > 0) {
            StringBuilder in = new StringBuilder();
            for (RefBookRecord record : records) {
                Map<String, RefBookValue> values = record.getValues();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE) &&
                            values.get(attribute.getAlias()) != null) {
                        in.append(values.get(attribute.getAlias()).getReferenceValue()).append(",");
                    }
                }
            }

            if (in.length() != 0) {
                in.deleteCharAt(in.length()-1);
                String sql = String.format(CHECK_REFERENCE_VERSIONS, in);
                System.out.println("sql: "+sql);
                return getJdbcTemplate().queryForInt(sql, versionFrom) == 0;
            }
        }
        return true;
    }

    /**
     *
     * @param attributes
     * @param record
     * @return
     */
    public List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, Map<String, RefBookValue> record){
        List<String> errors = new ArrayList<String>();
        for (RefBookAttribute a :attributes){
            if (a.isRequired() && (!record.containsKey(a.getAlias()) || record.get(a.getAlias()).isEmpty())){
                errors.add(a.getName());
            }
        }
        return errors;
    }

    public List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, List<RefBookRecord> records){
        List<String> errors = new ArrayList<String>();
        for (RefBookRecord record : records){
            errors.addAll(checkFillRequiredRefBookAtributes(attributes, record.getValues()));
        }
        return errors;
    }

    public List<String> checkRefBookAtributeValues(List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        List<String> errors = new ArrayList<String>();
        String okatoRegex = "\\d{11}";
        String codeTSRegex = "\\d{3}(\\d{2}|[?]{2})";
        Pattern okatoPattern = Pattern.compile(okatoRegex);
        Pattern codeTSPattern = Pattern.compile(codeTSRegex);
        for (RefBookRecord record : records) {
            Map<String, RefBookValue> values = record.getValues();
            for (RefBookAttribute a :attributes){
                //Должны содержать только цифры - Код валюты. Цифровой, Определяющая часть кода ОКАТО, Определяющая часть кода ОКТМО, Цифровой код валюты выпуска
                if ((a.getId() == 64L || a.getId() == 12L || a.getId() == 810L) &&
                        !NumberUtils.isNumber(values.get(a.getAlias()).getStringValue())){
                    //TODO добавить еще Определяющая часть кода ОКТМО
                    errors.add("Значение атрибута " + a.getName() + " должно содержать только цифры");
                }

                //Проверка формата для кода окато
                if ((a.getId() == 7L) && !okatoPattern.matcher(values.get(a.getAlias()).getStringValue()).matches()) {
                    errors.add("Значение атрибута " + a.getName() + " должно быть задано в формате ×××××××××××, где × - цифра");
                }

                //Проверка формата для кода ТС
                if ((a.getId() == 411L) && !codeTSPattern.matcher(values.get(a.getAlias()).getStringValue()).matches()) {
                    errors.add("Значение атрибута " + a.getName() + " должно быть задано в формате ××××× или ***??, где × - цифра");
                }
            }
        }

        return errors;
    }

	public Map<String, RefBookValue> getRecordData(final Long refBookId, final String tableName, final Long recordId) {
		RefBook refBook = refBookDao.get(refBookId);
		StringBuilder sql = new StringBuilder("SELECT id ");
		sql.append(RefBook.RECORD_ID_ALIAS);
		for (RefBookAttribute attribute : refBook.getAttributes()) {
			sql.append(", ");
			sql.append(attribute.getAlias());
		}
		sql.append(" FROM ");
		sql.append(tableName);
		sql.append(" WHERE id = :id");
		Map<String, Long> params = new HashMap<String, Long>();
		params.put("id", recordId);
		return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RefBookValueMapper(refBook));
	}


    /**
     * Удаление версии
     * @param uniqueRecordId уникальный идентификатор версии записи
     */
    public void deleteVersion(String tableName, Long uniqueRecordId) {
        getJdbcTemplate().update(String.format("delete from %s where id=?", tableName), uniqueRecordId);
    }

    private static final String DELETE_VERSION = "delete from %s where id in %s";

    /**
     * Удаляет указанные версии записи из справочника
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     */
    public void deleteRecordVersions(String tableName, List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_VERSION, tableName, SqlUtils.transformToSqlInStatement(uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

    public static class RecordVersionMapper implements RowMapper<RefBookRecordVersion> {

        @Override
        public RefBookRecordVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookRecordVersion result = new RefBookRecordVersion();
            result.setRecordId(rs.getLong(RefBook.RECORD_ID_ALIAS));
            result.setVersionStart(rs.getDate("versionStart"));
            result.setVersionEnd(rs.getDate("versionEnd"));
            return result;
        }
    }
}
