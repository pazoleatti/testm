package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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

	private void appendSortClause(PreparedStatementData ps, RefBook refBook, RefBookAttribute sortAttribute, boolean isSortAscending) {
		RefBookAttribute defaultSort = refBook.getSortAttribute();
		String sortAlias = sortAttribute == null ? (defaultSort == null ? "id" : defaultSort.getAlias()) : sortAttribute.getAlias();
		if (isSupportOver()) {
			// row_number() over (order by ... asc\desc)
			ps.appendQuery("row_number() over ( order by ");
			ps.appendQuery(sortAlias);
			ps.appendQuery(isSortAscending ? " ASC)" : " DESC)");
		} else {
			ps.appendQuery("rownum");
		}
		ps.appendQuery(" as ");
		ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
	}

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     * @param tableName название таблицы для которой формируется запрос
     * @param refBook справочник
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @param whereClause
     * @return
     */
    public PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
        String orderBy = "";
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT ");
        ps.appendQuery("id ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(" FROM (SELECT ");
		appendSortClause(ps, refBook, sortAttribute, isSortAscending);
        ps.appendQuery(", t.* FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" t");

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
            ps.appendQuery(orderBy);
        }

        ps.appendQuery(")");
        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
			ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
			ps.appendQuery(" BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }

        ps.appendQuery(orderBy);
        return ps;
    }

    /**
     * Формирует простой sql-запрос по выборке данных учитывая иерархичность таблицы
     * @param tableName название таблицы для которой формируется запрос
     * @param refBook справочник
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @return
     */
	//TODO вместо PARENT_ID использовать com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_PARENT_ID_ALIAS (Marat Fayzullin 26.03.2014)
    private PreparedStatementData getChildRecordsQuery(RefBook refBook, String tableName, Long parentId, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending) {
        String orderBy = "";
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT ");
        ps.appendQuery("RECORD_ID, ");
		appendSortClause(ps, refBook, sortAttribute, isSortAscending);

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(" FROM (SELECT distinct ");
        ps.appendQuery(" CONNECT_BY_ROOT ID as \"RECORD_ID\" ");
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", CONNECT_BY_ROOT ");
            ps.appendQuery(attribute.getAlias());
            ps.appendQuery(" as \"");
            ps.appendQuery(attribute.getAlias());
            ps.appendQuery("\" ");
        }

        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener =  applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE ");
            if (parentId == null) {
                ps.appendQuery("PARENT_ID is null or (");
            }
            ps.appendQuery(filterPS.getQuery().toString());
            if (filterPS.getParams().size() > 0) {
                ps.addParam(filterPS.getParams());
            }
            if (parentId == null) {
                ps.appendQuery(")");
            }
        }

        ps.appendQuery(" CONNECT BY NOCYCLE PRIOR id = PARENT_ID ");
        ps.appendQuery(" START WITH ");
        ps.appendQuery(parentId == null ? " PARENT_ID is null ":" PARENT_ID = "+parentId);

        ps.appendQuery(")");

        if (pagingParams != null) {
			ps.appendQuery(" WHERE ");
			ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
			ps.appendQuery(" BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }

        ps.appendQuery(orderBy);
        return ps;
    }

    public PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams, String whereClause) {
        return getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, true, whereClause);
    }

    /**
     * Загружает данные справочника на определенную дату актуальности
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param whereClause дополнительный фильтр для секции WHERE
     * @return
     */
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause) {
        RefBook refBook = refBookDao.get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, isSortAscending, whereClause);
        List<Map<String, RefBookValue>> records = getRecordsData(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, null, isSortAscending, whereClause);
        result.setTotalCount(getRecordsCount(ps));
        return result;
    }

    public List<Long> getUniqueRecordIds(Long refBookId, String tableName, String filter) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT ");
        ps.appendQuery("id ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" t");

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
        return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                return SqlUtils.getLong(rs,RefBook.RECORD_ID_ALIAS);
            }
        });
    }

	/**
     * Возвращает дочерние записи справочника учитывая иерархичность таблицы
     * @param tableName название таблицы
     * @param refBookId код справочника
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, String tableName, Long parentId, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getChildRecordsQuery(refBook, tableName, parentId, sortAttribute, filter, pagingParams, isSortAscending);
        List<Map<String, RefBookValue>> records = getRecordsData(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getChildRecordsQuery(refBook, tableName, parentId, sortAttribute, filter, null, isSortAscending);
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
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, String whereClause) {
        return getRecords(refBookId, tableName, pagingParams, filter, sortAttribute, true, whereClause);
    }

	/**
	 * Возвращает элементы справочника
	 * @param ps
	 * @param refBook
	 * @return
	 */
	public List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook) {
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
	public Integer getRecordsCount(PreparedStatementData ps) {
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


    private final static String CHECK_REFERENCE_VERSIONS_START = "select id from %s where VERSION < ? and ID in (%s)";

    private final static String CHECK_REFERENCE_VERSIONS_IN_PERIOD = "select id from (\n" +
            "  select r.id, r.version as versionStart, (select min(version) - interval '1' day from %s rn where rn.ref_book_id = r.ref_book_id and rn.record_id = r.record_id and rn.version > r.version) as versionEnd \n" +
            "  from %s r\n" +
            "  where id in (%s)\n" +
            ") where (:versionTo is not null and :versionTo < versionStart) or (versionEnd is not null and versionEnd < :versionFrom)";

    /**
     * Проверка ссылочных атрибутов. Их дата начала актуальности должна быть больше либо равна дате актуальности новой версии
     * @param versionFrom дата актуальности новой версии
     * @param attributes атрибуты справочника
     * @param records новые значения полей элемента справочника
     * @return ссылочные атрибуты в порядке?
     */
    public void isReferenceValuesCorrect(Logger logger, String tableName, @NotNull Date versionFrom, Date versionTo, @NotNull List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        if (attributes.size() > 0) {
            StringBuilder in = new StringBuilder();
            Map<Long, String> attributeIds = new HashMap<Long, String>();
            for (RefBookRecord record : records) {
                Map<String, RefBookValue> values = record.getValues();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE) &&
                            values.get(attribute.getAlias()) != null && !values.get(attribute.getAlias()).isEmpty() &&
                            !attribute.getAlias().equals("DEPARTMENT_ID")) {       //Подразделения не версионируются и их нет смысла проверять
                        Long id = values.get(attribute.getAlias()).getReferenceValue();
                        attributeIds.put(id, attribute.getName());
                        in.append(id).append(",");
                    }
                }
            }

            if (in.length() != 0) {
                in.deleteCharAt(in.length()-1);
                /** Проверяем пересекаются ли периоды ссылочных атрибутов с периодом текущей записи справочника */
                String sql = String.format(CHECK_REFERENCE_VERSIONS_IN_PERIOD, tableName, tableName, in);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("versionFrom", versionFrom);
                params.put("versionTo", versionTo);
                List<Long> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getLong("id");
                    }
                });
                if (result != null && !result.isEmpty()) {
                    if (logger != null) {
                        for (Long id : result) {
                            logger.error(attributeIds.get(id) + ": Период актуальности выбранного значения не пересекается с периодом актуальности версии!");
                        }
                    }
                    throw new ServiceException("Обнаружено некорректное значение атрибута");
                }

                /** Проверяем не начинается ли период актуальности ссылочного атрибута раньше чем период актуальности текущей записи справочника */
                sql = String.format(CHECK_REFERENCE_VERSIONS_START, tableName, in);
                result = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getLong("id");
                    }
                }, versionFrom);
                if (result != null && !result.isEmpty()) {
                    if (logger != null) {
                        for (Long id : result) {
                            logger.info(attributeIds.get(id) + ": Период актуальности выбранного значения меньше периода актуальности версии!");
                        }

                    }
                }
            }
        }
    }

    /**
     *
     * @param attributes
     * @param record
     * @return
     */
    public List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, Map<String, RefBookValue> record){
        List<String> errors = new ArrayList<String>();
        for (RefBookAttribute a : attributes){
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
                RefBookValue value = values.get(a.getAlias());
                //Должны содержать только цифры - Код валюты. Цифровой, Определяющая часть кода ОКАТО, Определяющая часть кода ОКТМО, Цифровой код валюты выпуска
                if ((a.getId() == 64L || a.getId() == 12L || a.getId() == 810L) &&
                        (value != null && !NumberUtils.isNumber(value.getStringValue()) || a.isRequired() && value == null)){
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

                //Проверка для иерархичных справочников
                if (record.getRecordId() != null && a.getAlias().equals(RefBook.RECORD_PARENT_ID_ALIAS)) {
                    Long parentId = values.get(a.getAlias()).getReferenceValue();
                    if (record.getRecordId().equals(parentId)) {
                        errors.add("Элемент справочника не может быть родительским для самого себя");
                    }
                }

                if (a.getId() == 164L && !Arrays.asList(DepartmentType.values()).contains(DepartmentType.fromCode(value.getNumberValue().intValue()))){
                   errors.add("Атрибута справочника \"Тип подразделенеия\" должно принимать одно из значений: 1,2,3,4,5");
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
                    return SqlUtils.getLong(rs,"ID");
                }
            }, uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
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
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RefBookValueMapper(refBook));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
	}


    /**
     * Удаление версии
     * @param uniqueRecordId уникальный идентификатор версии записи
     */
    public void deleteVersion(String tableName, @NotNull Long uniqueRecordId) {
        getJdbcTemplate().update(String.format("delete from %s where id=?", tableName), uniqueRecordId);
    }

    private static final String DELETE_VERSION = "delete from %s where %s";

    /**
     * Удаляет указанные версии записи из справочника
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     */
    public void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_VERSION, tableName, SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

    public static class RecordVersionMapper implements RowMapper<RefBookRecordVersion> {

        @Override
        public RefBookRecordVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookRecordVersion result = new RefBookRecordVersion();
            result.setRecordId(SqlUtils.getLong(rs,RefBook.RECORD_ID_ALIAS));
            result.setVersionStart(rs.getDate("versionStart"));
            result.setVersionEnd(rs.getDate("versionEnd"));
            result.setVersionEndFake(rs.getBoolean("endIsFake"));
            return result;
        }
    }
}
