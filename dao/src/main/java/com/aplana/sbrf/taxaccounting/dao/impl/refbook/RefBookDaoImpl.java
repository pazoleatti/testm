package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.UniversalFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 18:48
 */
@Repository
public class RefBookDaoImpl extends AbstractDao implements RefBookDao {

    private static final Log logger = LogFactory.getLog(RefBookDaoImpl.class);

    public static final String NOT_HIERARCHICAL_REF_BOOK_ERROR = "Справочник \"%s\" (id=%d) не является иерархичным";

//	public static final String UNKNOWN_ATTRIBUTE_ERROR = "Не указан атрибут справочника";
//	public static final String UNKNOWN_RECORD_ERROR = "Не указан код элемента справочника";

    private static final String DELETE_VERSION = "delete from %s where %s";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BDUtils dbUtils;

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    @Cacheable(value = "PermanentData", key = "'RefBook_'+#refBookId.toString()")
    public RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, script_id, visible, type, read_only, region_attribute_id, table_name from ref_book where id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    @Override
    public List<RefBook> getAll(Integer typeId) {
        return getAll(getJdbcTemplate().queryForList("select id from ref_book where (? is null or type = ?) order by name",
                new Object[]{typeId, typeId},
                Long.class));
    }

    private List<RefBook> getAll(List<Long> ids) {
        List<RefBook> refBookList = new ArrayList();
        for (Long id : ids) {
            refBookList.add(get(id));
        }

        return refBookList;
    }

    @Override
    public List<RefBook> getAllVisible(Integer typeId) {
        return getAll(
                getJdbcTemplate().queryForList(
                        "select id from ref_book where visible = 1 and (? is null or type = ?) order by name",
                        new Object[]{typeId, typeId},
                        Long.class));
    }

    @Override
    @Cacheable(value = "PermanentData", key = "'RefBook_attribute_'+#attributeId.toString()")
    public RefBook getByAttribute(Long attributeId) {
        try {
            return get(getJdbcTemplate().queryForLong(
                    "select r.id from ref_book r join ref_book_attribute a on a.ref_book_id = r.id where a.id = ?",
                    new Object[]{attributeId}, new int[]{Types.NUMERIC}));
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден атрибут справочника с id = %d", attributeId));
        }
    }

    /**
     * Настройка маппинга для справочника
     */
    private class RefBookRowMapper implements RowMapper<RefBook> {
        public RefBook mapRow(ResultSet rs, int index) throws SQLException {
            RefBook result = new RefBook();
            result.setId(SqlUtils.getLong(rs,"id"));
            result.setName(rs.getString("name"));
            result.setScriptId(rs.getString("script_id"));
			result.setVisible(rs.getBoolean("visible"));
            result.setAttributes(getAttributes(result.getId()));
			result.setType(SqlUtils.getInteger(rs,"type"));
			result.setReadOnly(rs.getBoolean("read_only"));
            result.setTableName(rs.getString("table_name"));
            BigDecimal regionAttributeId = (BigDecimal) rs.getObject("REGION_ATTRIBUTE_ID");
            if (regionAttributeId == null) {
                result.setRegionAttribute(null);
            } else {
                result.setRegionAttribute(getAttribute(regionAttributeId.longValue()));
            }
            return result;
        }
    }

    @Override
    public List<RefBookAttribute> getAttributes(Long refBookId) {
        try {
            return getJdbcTemplate().query(
                    "select id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
                            "is_unique, sort_order, format, read_only, max_length " +
                            "from ref_book_attribute where ref_book_id = ? order by ord",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookAttributeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдены атрибуты для справочника с id = %d", refBookId));
        }
    }

    /**
     * Настройка маппинга для атрибутов справочника
     */
    private class RefBookAttributeRowMapper implements RowMapper<RefBookAttribute> {
        public RefBookAttribute mapRow(ResultSet rs, int index) throws SQLException {

            RefBookAttribute result = new RefBookAttribute();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setName(rs.getString("name"));
            result.setAlias(rs.getString("alias"));
            result.setAttributeType(RefBookAttributeType.values()[SqlUtils.getInteger(rs, "type") - 1]);
            result.setRefBookId(SqlUtils.getLong(rs, "reference_id"));
            result.setRefBookAttributeId(SqlUtils.getLong(rs, "attribute_id"));
            result.setVisible(rs.getBoolean("visible"));
            result.setPrecision(SqlUtils.getInteger(rs, "precision"));
            result.setWidth(SqlUtils.getInteger(rs, "width"));
            result.setRequired(rs.getBoolean("required"));
            result.setReadOnly(rs.getBoolean("read_only"));
            result.setUnique(rs.getBoolean("is_unique"));
            result.setSortOrder(SqlUtils.getInteger(rs, "sort_order"));
            result.setMaxLength(SqlUtils.getInteger(rs, "max_length"));
            Integer formatId = SqlUtils.getInteger(rs, "format");
            if (formatId != null) {
                result.setFormat(Formats.getById(formatId));
            }
            return result;
        }
    }

    private void appendSortClause(PreparedStatementData ps, RefBook refBook, RefBookAttribute sortAttribute, boolean isSortAscending, boolean isHierarchical) {
        RefBookAttribute defaultSort = refBook.getSortAttribute();
        if (isSupportOver()) {
            // row_number() over (order by ... asc\desc)
            ps.appendQuery("row_number() over ( order by ");
            if (sortAttribute != null || defaultSort != null) {
                String sortAlias = sortAttribute == null ? defaultSort.getAlias() : sortAttribute.getAlias();
                RefBookAttributeType sortType = sortAttribute == null ? defaultSort.getAttributeType() : sortAttribute.getAttributeType();

                if (isHierarchical) {
                    ps.appendQuery(sortAlias);
                } else {
                    ps.appendQuery("a");
                    ps.appendQuery(sortAlias);
                    ps.appendQuery(".");
                    ps.appendQuery(sortType.toString());
                    ps.appendQuery("_value ");
                }
            } else {
                if (isHierarchical) {
                    ps.appendQuery(RefBook.RECORD_PARENT_ID_ALIAS);
                } else {
                    ps.appendQuery("id");
                }
            }
            ps.appendQuery(isSortAscending ? " ASC)" : " DESC)");
        } else {
            ps.appendQuery("rownum");
        }
        ps.appendQuery(" as ");
        ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
    }

    private RefBookAttribute getAttribute(@NotNull Long attributeId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
                            "is_unique, sort_order, format, read_only, max_length " +
                            "from ref_book_attribute where id = ?",
                    new Object[]{attributeId}, new int[]{Types.NUMERIC},
                    new RefBookAttributeRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден атрибут с id = %d", attributeId));
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PreparedStatementData ps = getRefBookSql(refBookId, null, version, sortAttribute, filter, pagingParams, isSortAscending);
        RefBook refBook = get(refBookId);
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справкочнике
        PreparedStatementData psForCount = getRefBookSql(refBookId, null, version, sortAttribute, filter, null, true);
        psForCount.setQuery(new StringBuilder("SELECT count(*) FROM (" + psForCount.getQuery() + ")"));
        result.setTotalCount(getJdbcTemplate().queryForInt(psForCount.getQuery().toString(), psForCount.getParams().toArray()));
        return result;
    }

    @Override
    public Long getRowNum(Long refBookId, Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PreparedStatementData ps = getRefBookSql(refBookId, null, version, sortAttribute, filter, null, isSortAscending);
        return getRowNum(ps, recordId);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {                       // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        RefBook refBook = get(refBookId);
        List<RefBookAttribute> attributes = refBook.getAttributes();

        PreparedStatementData filterPS = new PreparedStatementData();
        UniversalFilterTreeListener universalFilterTreeListener = applicationContext.getBean("universalFilterTreeListener", UniversalFilterTreeListener.class);
        universalFilterTreeListener.setRefBook(refBook);
        universalFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, universalFilterTreeListener);

        StringBuilder fromSql = new StringBuilder();
        fromSql.append("\nfrom ref_book_record r\n");

        if (version != null && !needAccurateVersion) {
            fromSql.append("join t on (r.version = t.version and r.record_id = t.record_id)\n");
            ps.appendQuery(WITH_STATEMENT);
            ps.appendQuery("\n");
            ps.addParam(refBookId);
            ps.addParam(version);
            ps.addParam(version);
        }
        ps.appendQuery("select\n");
        ps.appendQuery(" r.id as ID, r.record_id as RECORD_ID\n");

        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();
            ps.appendQuery(", a");
            ps.appendQuery(alias);
            ps.appendQuery(".");
            ps.appendQuery(attribute.getAttributeType().toString());
            ps.appendQuery("_value as \"");
            ps.appendQuery(alias);
            ps.appendQuery("\"");

            fromSql.append(" left join ref_book_value a");
            fromSql.append(alias);
            fromSql.append(" on a");
            fromSql.append(alias);
            fromSql.append(".record_id = r.id and a");
            fromSql.append(alias);
            fromSql.append(".attribute_id = ");
            fromSql.append(attribute.getId());
        }

        // добавляем join'ы относящиеся к фильтру
        if (filterPS.getJoinPartsOfQuery() != null) {
            fromSql.append(filterPS.getJoinPartsOfQuery());
        }

        ps.appendQuery(fromSql.toString());
        ps.appendQuery("where\n  r.ref_book_id = ?");
        ps.addParam(refBookId);
        if (version != null && needAccurateVersion) {
            ps.appendQuery(" and  r.version = ?");
            ps.addParam(version);
        }
        ps.appendQuery(" and\n  status <> -1\n");

        // обработка параметров фильтра
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" and\n ");
            ps.appendQuery("(");
            ps.appendQuery(filterPS.getQuery().toString());
            ps.appendQuery(")");
            ps.appendQuery("\n");
            ps.addParam(filterPS.getParams());
        }

        try {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                    new RowMapper<Pair<Long, Long>>() {
                        @Override
                        public Pair<Long, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Pair<Long, Long>(SqlUtils.getLong(rs, "ID"), SqlUtils.getLong(rs, "RECORD_ID"));
                        }
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int getRecordsCount(Long refBookId, Date version, String filter) {
        PreparedStatementData psForCount = getRefBookSql(refBookId, null, version, null, filter, null, true);
        psForCount.setQuery(new StringBuilder("SELECT count(*) FROM (" + psForCount.getQuery() + ")"));
        return getJdbcTemplate().queryForInt(psForCount.getQuery().toString(), psForCount.getParams().toArray());
    }

    private PagingResult<Map<String, RefBookValue>> getChildren(@NotNull Long refBookId, @NotNull Date version, PagingParams pagingParams,
                                                                String filter, RefBookAttribute sortAttribute, boolean isSortAscending, Long parentId) {
        PreparedStatementData ps = getChildrenStatement(refBookId, null, version, sortAttribute, filter, pagingParams, isSortAscending, parentId);
        RefBook refBook = get(refBookId);
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справкочнике
        PreparedStatementData psForCount = getChildrenStatement(refBookId, null, version, sortAttribute, filter, null, true, parentId);
        psForCount.setQuery(new StringBuilder("SELECT count(*) FROM (" + psForCount.getQuery() + ")"));
        result.setTotalCount(getJdbcTemplate().queryForInt(psForCount.getQuery().toString(), psForCount.getParams().toArray()));
        return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version,
                                                              PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(refBookId, version, pagingParams, filter, sortAttribute, true);
    }

    /**
     * Обработка запроса на получение строки/строк справочника
     *
     * @param rs      ResultSet
     * @param records Справочник
     * @throws SQLException
     */
    private void processRecordDataRow(ResultSet rs, RefBook refBook, Map<Long, Map<String, RefBookValue>> records) throws SQLException {
        Long id = SqlUtils.getLong(rs, "id");
        Long attributeId = SqlUtils.getLong(rs, "attribute_id");

        if (id == null || attributeId == null) {
            // Ситуауция возможна когда в ref_book_record есть запись, а в ref_book_value соответствующих записе нет
            return;
        }

        if (!records.containsKey(id)) {
            records.put(id, refBook.createRecord());
        }

        Map<String, RefBookValue> record = records.get(id);

        RefBookValue recordIdValue = record.get(RefBook.RECORD_ID_ALIAS);
        if (recordIdValue.getNumberValue() == null) {
            recordIdValue.setValue(id);
        }

        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        String columnName = attribute.getAttributeType().name() + "_value";
        if (rs.getObject(columnName) != null) {
            Object value = null;
            switch (attribute.getAttributeType()) {
                case STRING: {
                    value = rs.getString(columnName);
                }
                break;
                case NUMBER: {
                    value = rs.getBigDecimal(columnName).setScale(attribute.getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
                break;
                case DATE: {
                    value = rs.getDate(columnName);
                }
                break;
                case REFERENCE: {
                    value = SqlUtils.getLong(rs, columnName);
                }
                break;
            }
            RefBookValue attrValue = record.get(attribute.getAlias());
            attrValue.setValue(value);
        }
    }

    // Строка справочника по идентификатору строки
    private static final String SELECT_SINGLE_ROW_VALUES_QUERY =
            " SELECT r.id, v.attribute_id, " +
                    " v.string_value, v.number_value, v.date_value, v.reference_value" +
                    " FROM ref_book_record r LEFT JOIN ref_book_value v ON v.record_id = r.id " +
                    " WHERE r.id = ? AND r.ref_book_id = ?";

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        final RefBook refBook = get(refBookId);
        final Map<Long, Map<String, RefBookValue>> resultMap = new HashMap<Long, Map<String, RefBookValue>>();

        getJdbcTemplate().query(SELECT_SINGLE_ROW_VALUES_QUERY, new Object[]{recordId, refBookId}, new int[]{Types.NUMERIC, Types.NUMERIC}, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                processRecordDataRow(rs, refBook, resultMap);
            }
        });
        if (resultMap.isEmpty()) { // если элемент не найден
            throw new DaoException(String.format("В справочнике \"%s\" (id = %d) не найден элемент с id = %d", refBook.getName(), refBookId, recordId));
        }
        return resultMap.values().iterator().next();
    }

    // Строки справочника по списку идентификаторов строки
    public static final String SELECT_VALUES_BY_IDS_QUERY =
            "SELECT r.id, v.attribute_id, " +
                    "v.string_value, v.number_value, v.date_value, v.reference_value " +
                    "FROM ref_book_record r LEFT JOIN ref_book_value v ON v.record_id = r.id " +
                    "WHERE r.id in (:recordIds) AND r.ref_book_id = :refBookId";

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(Long refBookId, List<Long> recordIds) {
        final RefBook refBook = get(refBookId);
        final Map<Long, Map<String, RefBookValue>> resultMap = new HashMap<Long, Map<String, RefBookValue>>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", refBookId);
        params.put("recordIds", recordIds);
        getNamedParameterJdbcTemplate().query(SELECT_VALUES_BY_IDS_QUERY, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                processRecordDataRow(rs, refBook, resultMap);
            }
        });
        return resultMap;
    }

    private static final String WITH_STATEMENT =
            "with t as (select\n" +
                    "  max(version) version, record_id\n" +
                    "from\n" +
                    "  ref_book_record r\n" +
                    "where\n" +
                    "  r.ref_book_id = ? and r.status = 0 and r.version <= ? and\n" +
                    "  not exists (select 1 from ref_book_record r2 where r2.ref_book_id=r.ref_book_id and r2.record_id=r.record_id and r2.version between r.version + interval '1' day and ?)\n" +
                    "group by\n" +
                    "  record_id)\n";

    private static final String RECORD_VERSIONS_STATEMENT =
            "with currentRecord as (select id, record_id, version from REF_BOOK_RECORD where id=%d),\n" +
                    "recordsByVersion as (select r.ID, r.RECORD_ID, r.REF_BOOK_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r, currentRecord cr where r.REF_BOOK_ID=%d and r.RECORD_ID=cr.RECORD_ID), \n" +
                    "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";

    /**
     * Динамически формирует запрос для справочника
     *
     * @param refBookId       код справочника
     * @param uniqueRecordId  идентификатор записи справочника. Если = null, то получаем все записи справочника, иначе - получаем все версии записи справочника
     * @param version         дата актуальности данных справочника. Если = null, то версионирование не учитывается
     * @param filter          строка фильтрации
     * @param sortAttribute   сортируемый столбец. Может быть не задан
     * @param pagingParams    параметры для постраничной навигации. Может быть null, тогда возвращается весь набор данных по текущему срезу
     * @param isSortAscending порядок сортировки, по умолчанию используется сортировка по возрастанию
     * @return
     */
    private PreparedStatementData getRefBookSql(@NotNull Long refBookId, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending) {
        // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        RefBook refBook = get(refBookId);
        List<RefBookAttribute> attributes = refBook.getAttributes();

        if (sortAttribute != null && !attributes.contains(sortAttribute)) {
            throw new IllegalArgumentException(String.format("Reference book (id=%d) doesn't contains attribute \"%s\"",
                    refBookId, sortAttribute.getAlias()));
        }

        PreparedStatementData filterPS = new PreparedStatementData();
        UniversalFilterTreeListener universalFilterTreeListener = applicationContext.getBean("universalFilterTreeListener", UniversalFilterTreeListener.class);
        universalFilterTreeListener.setRefBook(refBook);
        universalFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, universalFilterTreeListener);

        StringBuilder fromSql = new StringBuilder("\nfrom\n");

        fromSql.append("  ref_book_record frb join t on (frb.version = t.version and frb.record_id = t.record_id)\n");
        if (version != null) {
            ps.appendQuery(WITH_STATEMENT);
            ps.addParam(refBookId);
            ps.addParam(version);
            ps.addParam(version);
        } else {
            ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT, uniqueRecordId, refBookId));
            ps.addParam(VersionedObjectStatus.NORMAL.getId());
        }

        ps.appendQuery("SELECT * FROM "); //TODO: заменить "select *" на полное перечисление полей (Marat Fayzullin 30.01.2014)
        ps.appendQuery("(select\n");
        ps.appendQuery(" frb.id as ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        ps.appendQuery(",\n");

        if (version == null) {
            ps.appendQuery("  t.version as \"");
            ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
            ps.appendQuery("\",\n");

            ps.appendQuery("  t.versionEnd as \"");
            ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
            ps.appendQuery("\",\n");
        }

        appendSortClause(ps, refBook, sortAttribute, isSortAscending, false);

        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();
            ps.appendQuery(", a");
            ps.appendQuery(alias);
            ps.appendQuery(".");
            ps.appendQuery(attribute.getAttributeType().toString());
            ps.appendQuery("_value as \"");
            ps.appendQuery(alias);
            ps.appendQuery("\"");

            fromSql.append(" left join ref_book_value a");
            fromSql.append(alias);
            fromSql.append(" on a");
            fromSql.append(alias);
            fromSql.append(".record_id = frb.id and a");
            fromSql.append(alias);
            fromSql.append(".attribute_id = ");
            fromSql.append(attribute.getId());
        }

        // добавляем join'ы относящиеся к фильтру
        fromSql.append("\n");
        if (filterPS.getJoinPartsOfQuery() != null) {
            fromSql.append(filterPS.getJoinPartsOfQuery());
        }

        ps.appendQuery(fromSql.toString());
        ps.appendQuery(" where\n  frb.ref_book_id = ");
        ps.appendQuery("?");
        ps.addParam(refBookId);
        ps.appendQuery(" and\n  frb.status <> -1\n");

        if (version == null) {
            ps.appendQuery("order by t.version\n");
        }

        // обработка параметров фильтра
        if (filterPS.getQuery().length() > 0
                && !filterPS.getQuery().toString().trim().equals("()")) {
            ps.appendQuery(" and\n ");
            ps.appendQuery("(");
            ps.appendQuery(filterPS.getQuery().toString());
            ps.appendQuery(")");
            ps.appendQuery("\n");
            ps.addParam(filterPS.getParams());
        }
        ps.appendQuery(")");

        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
            ps.appendQuery(" BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(String.valueOf(pagingParams.getStartIndex() + pagingParams.getCount()));
        }

        return ps;
    }

    private PreparedStatementData getChildrenStatement(@NotNull Long refBookId, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                       String filter, PagingParams pagingParams, boolean isSortAscending, Long parentId) {
        // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        RefBook refBook = get(refBookId);
        List<RefBookAttribute> attributes = refBook.getAttributes();

        if (sortAttribute != null && !attributes.contains(sortAttribute)) {
            throw new IllegalArgumentException(String.format("Reference book (id=%d) doesn't contains attribute \"%s\"",
                    refBookId, sortAttribute.getAlias()));
        }

        PreparedStatementData filterPS = new PreparedStatementData();
        UniversalFilterTreeListener universalFilterTreeListener = applicationContext.getBean("universalFilterTreeListener", UniversalFilterTreeListener.class);
        universalFilterTreeListener.setRefBook(refBook);
        universalFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, universalFilterTreeListener);

        StringBuilder fromSql = new StringBuilder("\nfrom\n");

        fromSql.append("  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)\n");
        if (version != null) {
            ps.appendQuery(WITH_STATEMENT);
            ps.addParam(refBookId);
            ps.addParam(version);
            ps.addParam(version);
        } else {
            ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT, uniqueRecordId, refBookId));
            ps.addParam(VersionedObjectStatus.NORMAL.getId());
        }

        ps.appendQuery(" SELECT ");

        appendSortClause(ps, refBook, sortAttribute, isSortAscending, true);
        ps.appendQuery(",");

        // выбираем все алиасы + row_number_over
        List<String> aliases = new ArrayList<String>(attributes.size() + 1);
        aliases.add("record_id");
        //aliases.add("rownum ");
        //aliases.add(RefBook.RECORD_SORT_ALIAS);
        for (RefBookAttribute attr : attributes) {
            aliases.add(attr.getAlias());
        }
        ps.appendQuery(StringUtils.join(aliases.toArray(), ','));
        ps.appendQuery(" FROM");
        ps.appendQuery("(select distinct \n");
        ps.appendQuery(" CONNECT_BY_ROOT  r.id as \"RECORD_ID\", \n");
        if (version == null) {
            ps.appendQuery("  t.version as \"");
            ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
            ps.appendQuery("\",\n");

            ps.appendQuery("  t.versionEnd as \"");
            ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
            ps.appendQuery("\",\n");
        }


        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();
            ps.appendQuery(" CONNECT_BY_ROOT  a");
            ps.appendQuery(alias);
            ps.appendQuery(".");
            ps.appendQuery(attribute.getAttributeType().toString());
            ps.appendQuery("_value as \"");
            ps.appendQuery(alias);
            ps.appendQuery("\"");
            if (i < attributes.size() - 1) {
                ps.appendQuery(",\n");
            }
            fromSql.append("  left join ref_book_value a");
            fromSql.append(alias);
            fromSql.append(" on a");
            fromSql.append(alias);
            fromSql.append(".record_id = r.id and a");
            fromSql.append(alias);
            fromSql.append(".attribute_id = ");
            fromSql.append(attribute.getId());
            fromSql.append("\n");
        }

        // добавляем join'ы относящиеся к фильтру
        if (filterPS.getJoinPartsOfQuery() != null) {
            fromSql.append(filterPS.getJoinPartsOfQuery());
        }

        ps.appendQuery(fromSql.toString());
        ps.appendQuery("where\n  r.ref_book_id = ");
        ps.appendQuery("?");
        ps.addParam(refBookId);
        ps.appendQuery(" and\n  status <> -1\n");

        if (version == null) {
            ps.appendQuery("order by t.version\n");
        }

        // обработка параметров фильтра
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" and\n ");
            ps.appendQuery("(");
            ps.appendQuery(filterPS.getQuery().toString());
            ps.appendQuery(")");
            ps.appendQuery("\n");
            ps.addParam(filterPS.getParams());
        }

        // выборка иерархического исправочника
        ps.appendQuery(" CONNECT BY NOCYCLE PRIOR r.id = aPARENT_ID.REFERENCE_value ");
        ps.appendQuery("START WITH aPARENT_ID.REFERENCE_value ");
        if (parentId == null) {
            ps.appendQuery(" is null ");
        } else {
            ps.appendQuery(" = ");
            ps.appendQuery(parentId.toString());
        }
        ps.appendQuery(")");

        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
            ps.appendQuery(" BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(String.valueOf(pagingParams.getStartIndex() + pagingParams.getCount()));
        }

        return ps;
    }

    /**
     * Модифицирует фильтр, добавляя в него условие фильтрации по родителю
     *
     * @param filter         исходная строка фильтра
     * @param parentRecordId код родительской записи
     * @return фильтр с учетом условия по родительской записи
     */
    public static String getParentFilter(String filter, Long parentRecordId) {
        String parentFilter = RefBook.RECORD_PARENT_ID_ALIAS + (parentRecordId == null ? " is null" : " = " + parentRecordId.toString());
        return (filter == null || filter.trim().length() == 0) ? parentFilter : filter + " AND " + parentFilter;
    }

    static boolean checkHierarchical(RefBook refBook) {
        try {
            RefBookAttribute attr = refBook.getAttribute(RefBook.RECORD_PARENT_ID_ALIAS);
            return attr != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version,
                                                                      PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        RefBook refBook = get(refBookId);

        if (!RefBookDaoImpl.checkHierarchical(refBook)) {
            throw new IllegalArgumentException(String.format(NOT_HIERARCHICAL_REF_BOOK_ERROR, refBook.getName(), refBook.getId()));
        }

        /**
         * Если фильтра нет, то выбираем как обычно, просто используя фильтр PARENT_ID = ?
         * в случае же с переданнм фильтром используем иной запрос,
         * который ищет все записи, включая вложенные, которые соответствуют условию фильтрации,
         * а затем выбираеются их родители для текущего parentRecordId
         *
         */
        if (filter == null || filter.equals("")) {
            return getRecords(refBookId, version, pagingParams, getParentFilter(filter, parentRecordId), sortAttribute, true);
        } else {
            return getChildren(refBookId, version, pagingParams, filter, sortAttribute, true, parentRecordId);
        }
    }

    private static final String INSERT_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, record_id, ref_book_id, version," +
            "status) values (?, ?, ?, ?, ?)";
    private static final String INSERT_REF_BOOK_VALUE = "insert into ref_book_value (record_id, attribute_id," +
            "string_value, number_value, date_value, reference_value) values (?, ?, ?, ?, ?, ?)";

    @Override
    public List<Long> createRecordVersion(final Long refBookId, final Date version, final VersionedObjectStatus status,
                                          final List<RefBookRecord> records) {
        List<Object[]> listValues = new ArrayList<Object[]>();

        if (records == null || records.isEmpty()) {
            return null;
        }

        RefBook refBook = get(refBookId);

        final List<Long> refBookRecordIds = dbUtils.getNextRefBookRecordIds((long) records.size());
        BatchPreparedStatementSetter batchRefBookRecordsPS = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, refBookRecordIds.get(i));
                ps.setLong(2, records.get(i).getRecordId());
                ps.setLong(3, refBookId);
                ps.setDate(4, new java.sql.Date(version.getTime()));
                ps.setInt(5, status.getId());
            }

            @Override
            public int getBatchSize() {
                return refBookRecordIds.size();
            }
        };

        for (int i = 0; i < records.size(); i++) {
            // создаем строки справочника

            // записываем значения ячеек
            Map<String, RefBookValue> record = records.get(i).getValues();

            for (Map.Entry<String, RefBookValue> entry : record.entrySet()) {
                String attributeAlias = entry.getKey();
                if (RefBook.RECORD_ID_ALIAS.equals(attributeAlias)) {
                    continue;
                }
                RefBookAttribute attribute = refBook.getAttribute(attributeAlias);
                Object[] values = new Object[6];
                values[0] = refBookRecordIds.get(i);
                values[1] = attribute.getId();
                values[2] = null;
                values[3] = null;
                values[4] = null;
                values[5] = null;
                switch (attribute.getAttributeType()) {
                    case STRING: {
                        values[2] = entry.getValue().getStringValue();
                    }
                    break;
                    case NUMBER: {
                        if (entry.getValue().getNumberValue() != null) {
                            BigDecimal v = new BigDecimal(entry.getValue().getNumberValue().toString());
                            values[3] = v.setScale(attribute.getPrecision(), RoundingMode.HALF_UP);
                        }
                    }
                    break;
                    case DATE: {
                        values[4] = entry.getValue().getDateValue();
                    }
                    break;
                    case REFERENCE: {
                        values[5] = entry.getValue().getReferenceValue();
                    }
                    break;
                }
                listValues.add(values);
            }
        }
        JdbcTemplate jt = getJdbcTemplate();
        jt.batchUpdate(INSERT_REF_BOOK_RECORD_SQL, batchRefBookRecordsPS);
        jt.batchUpdate(INSERT_REF_BOOK_VALUE, listValues);
        return refBookRecordIds;
    }

    private static final String INSERT_FAKE_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, record_id, ref_book_id, version," +
            "status) values (seq_ref_book_record.nextval, ?, ?, ?, 2)";

    @Override
    public void createFakeRecordVersion(Long refBookId, Long recordId, Date version) {
        getJdbcTemplate().update(INSERT_FAKE_REF_BOOK_RECORD_SQL, recordId, refBookId, version);
    }

    private static final String DELETE_REF_BOOK_VALUE_SQL = "delete from ref_book_value where record_id = ?";

    @Override
    public void updateRecordVersion(Long refBookId, Long uniqueRecordId, Map<String, RefBookValue> records) {
        try {
            // нет данных - нет работы
            if (records.size() == 0) {
                return;
            }
            RefBook refBook = get(refBookId);
            List<Object[]> listValues = new ArrayList<Object[]>();

            for (Map.Entry<String, RefBookValue> entry : records.entrySet()) {
                String attributeAlias = entry.getKey();
                if (RefBook.RECORD_ID_ALIAS.equals(attributeAlias)) {
                    continue;
                }
                RefBookAttribute attribute = refBook.getAttribute(attributeAlias);
                Object[] values = new Object[]{uniqueRecordId, attribute.getId(), null, null, null, null};
                switch (attribute.getAttributeType()) {
                    case STRING: {
                        values[2] = entry.getValue().getStringValue();
                    }
                    break;
                    case NUMBER: {
                        if (entry.getValue().getNumberValue() != null) {
                            BigDecimal v = new BigDecimal(entry.getValue().getNumberValue().toString());
                            values[3] = v.setScale(attribute.getPrecision(), RoundingMode.HALF_UP);
                        }
                    }
                    break;
                    case DATE: {
                        values[4] = entry.getValue().getDateValue();
                    }
                    break;
                    case REFERENCE: {
                        values[5] = entry.getValue().getReferenceValue();
                    }
                    break;
                }
                listValues.add(values);
            }

            JdbcTemplate jt = getJdbcTemplate();
            //Удаляем старые значения атрибутов
            jt.update(DELETE_REF_BOOK_VALUE_SQL, uniqueRecordId);

            //Создаем новые значения атрибутов
            jt.batchUpdate(INSERT_REF_BOOK_VALUE, listValues);
        } catch (Exception ex) {
            throw new DaoException("Не удалось обновить значения справочника", ex);
        }
    }

    @Override
    public boolean isVersionsExist(Long refBookId, List<Long> recordIds, Date version) {
        String sql = "select count(*) from ref_book_record where ref_book_id = ? and %s and version = trunc(?, 'DD')";
        return getJdbcTemplate().queryForInt(String.format(sql, SqlUtils.transformToSqlInStatement("record_id", recordIds)), refBookId, version) != 0;
    }

    private static final String IS_RECORDS_ACTIVE_IN_PERIOD = "select id from (\n" +
            "select input.id as input_id, rbr.id, rbr.record_id, rbr.version as start_version, rbr.status, lead (rbr.version) over (partition by rbr.recorD_id order by rbr.version) end_version \n" +
            "from ref_book_record input\n" +
            "join ref_book_record rbr on input.record_id = rbr.record_id and input.ref_book_id = rbr.ref_book_id \n" +
            "where %s \n" +
            ") a where input_id = id \n" +
            "and (end_version - 1 >= :periodFrom or end_version is null) \n" +
            "and (:periodTo is null or start_version <= :periodTo)";

    @Override
    public List<Long> isRecordsActiveInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, @NotNull Date periodTo) {
        String sql = String.format(IS_RECORDS_ACTIVE_IN_PERIOD, SqlUtils.transformToSqlInStatement("input.id", recordIds));
        Set<Long> result = new HashSet<Long>(recordIds);
        List<Long> existRecords = new ArrayList<Long>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodFrom", periodFrom);
        params.put("periodTo", periodTo);
        try {
            existRecords = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}
        result.removeAll(existRecords);
        return new ArrayList<Long>(result);
    }

    private static final String CHECK_REF_BOOK_RECORD_UNIQUE_SQL = "select id from ref_book_record " +
            "where ref_book_id = ? and version = trunc(?, 'DD') and record_id = ?";

    private void checkFillRequiredFields(Map<String, RefBookValue> record, @NotNull RefBook refBook) {
        List<RefBookAttribute> attributes = refBook.getAttributes();
        List<String> errors = RefBookUtils.checkFillRequiredRefBookAtributes(attributes, record);

        if (errors.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Поля ");
            for (int j = 0; j < errors.size(); j++) {
                String field = errors.get(j);
                sb.append("«").append(field).append("»");
                if (j != errors.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(" являются обязательными для заполнения.");
            throw new DaoException(sb.toString());
        }
    }

    @Override
    public Long checkRecordUnique(Long refBookId, Date version, Long rowId) {
        try {
            if (logger.isDebugEnabled()) {
                logger.trace(String.format("refBookId: %d; version: %s; rowId: %s", refBookId, version.toString(), rowId));
            }
            return getJdbcTemplate().queryForLong(CHECK_REF_BOOK_RECORD_UNIQUE_SQL,
                    new Object[]{refBookId, version, rowId},
                    new int[]{Types.BIGINT, Types.DATE, Types.BIGINT});
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        final RefBookAttribute attribute = getAttribute(attributeId);
        try {
            return getJdbcTemplate().queryForObject("select record_id, attribute_id, string_value, number_value, date_value, reference_value from ref_book_value where record_id = ? and attribute_id = ?",
                    new Object[]{
                            recordId, attributeId
                    },
                    new RowMapper<RefBookValue>() {
                        @Override
                        public RefBookValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                            Object value = null;
                            String columnName = attribute.getAttributeType() + "_VALUE";
                            switch (attribute.getAttributeType()) {
                                case STRING: {
                                    value = rs.getString(columnName);
                                }
                                break;
                                case NUMBER: {
                                    value = rs.getBigDecimal(columnName).setScale(attribute.getPrecision(), RoundingMode.HALF_UP);
                                }
                                break;
                                case DATE: {
                                    value = rs.getDate(columnName);
                                }
                                break;
                                case REFERENCE: {
                                    value = SqlUtils.getLong(rs, columnName);
                                }
                                break;
                            }
                            return new RefBookValue(attribute.getAttributeType(), value);
                        }
                    }
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private static final String GET_RECORD_VERSION = "with currentVersion as (select id, version, record_id, ref_book_id from ref_book_record where id = ?),\n" +
            "minNextVersion as (select r.ref_book_id, r.record_id, min(r.version) version from ref_book_record r, currentVersion cv where r.version > cv.version and r.record_id= cv.record_id and r.ref_book_id= cv.ref_book_id group by r.ref_book_id, r.record_id),\n" +
            "nextVersionEnd as (select mnv.ref_book_id, mnv.record_id, mnv.version, r.status from minNextVersion mnv, ref_book_record r where mnv.ref_book_id=r.ref_book_id and mnv.version=r.version and mnv.record_id=r.record_id)\n" +
            "select cv.id as %s, \n" +
            "cv.version as versionStart, \n" +
            "nve.version - interval '1' day as versionEnd, \n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from currentVersion cv \n" +
            "left join nextVersionEnd nve on (nve.record_id= cv.record_id and nve.ref_book_id= cv.ref_book_id)";

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        try {
            String sql = String.format(GET_RECORD_VERSION,
                    RefBook.RECORD_ID_ALIAS);
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{
                            uniqueRecordId
                    },
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new DaoException("Не найдены версии для указанного элемента справочника", ex);
        }
    }

    @Override
    public int getRecordVersionsCount(Long refBookId, Long uniqueRecordId) {
        return getJdbcTemplate().queryForInt("select count(*) as cnt from REF_BOOK_RECORD " +
                "where REF_BOOK_ID=? and STATUS=" + VersionedObjectStatus.NORMAL.getId() + " and RECORD_ID=(select RECORD_ID from REF_BOOK_RECORD where ID=?)",
                new Object[]{refBookId, uniqueRecordId});
    }

    private static final String RECORD_VERSION =
            "select\n" +
                    "  max(version) as version\n" +
                    "from\n" +
                    "  ref_book_record\n" +
                    "where\n" +
                    "  ref_book_id = ? and\n" +
                    "version <= ? \n" +
                    "union\n" +
                    "select\n" +
                    "  version\n" +
                    "from\n" +
                    "  ref_book_record\n" +
                    "where\n" +
                    "  ref_book_id = ? and\n" +
                    "  version >= ? and\n" +
                    "  version <= ? and" +
                    "  status = 0\n" +
                    "group by\n" +
                    "  version\n" +
                    "order by\n" +
                    "  version";

    @Override
    public List<Date> getVersions(Long refBookId, Date startDate, Date endDate) {
        return getJdbcTemplate().query(RECORD_VERSION, new RowMapper<Date>() {
            @Override
            public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getDate(1);
            }
        }, refBookId, startDate, refBookId, startDate, endDate);
    }

    private final static String GET_FIRST_RECORD_ID = "with allRecords as (select id, version from ref_book_record where record_id = (select record_id from ref_book_record where id = ?) and ref_book_id = ? and id != ?)\n" +
            "select id from allRecords where version = (select min(version) from allRecords)";

    @Override
    public Long getFirstRecordId(Long refBookId, Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForLong(GET_FIRST_RECORD_ID, uniqueRecordId, refBookId, uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        final Map<Long, Date> result = new HashMap<Long, Date>();
        getJdbcTemplate().query(String.format("select id, version from ref_book_record where %s",
                SqlUtils.transformToSqlInStatement("id", uniqueRecordIds)), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(SqlUtils.getLong(rs, "id"), rs.getDate("version"));
            }
        }
        );
        return result;
    }

    @Override
    public List<Date> hasChildren(Long refBookId, List<Long> uniqueRecordIds) {
        String sql = String.format("select distinct r.version from ref_book_value v, ref_book_record r where v.attribute_id = (select id from ref_book_attribute where ref_book_id=? and alias='%s') and %s and r.id=v.reference_value",
                RefBook.RECORD_PARENT_ID_ALIAS, SqlUtils.transformToSqlInStatement("v.reference_value", uniqueRecordIds));
        try {
            return getJdbcTemplate().query(sql, new RowMapper<Date>() {
                @Override
                public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getDate(1);
                }
            }, refBookId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        String sql = "select record_id from ref_book_value where level != 1 and attribute_id in (select id from ref_book_attribute where alias = 'PARENT_ID') " +
                "start with record_id = ? connect by prior reference_value = record_id order by level desc";
        try {
            return getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return SqlUtils.getLong(rs, "record_id");
                }
            }, uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    private static final String CHECK_PARENT_CONFLICT = "with currentRecord as (select id, ref_book_id, record_id, version from ref_book_record where %s),\n" +
            "nextVersion as (select min(r.version) as version from ref_book_record r, currentRecord cr where r.version > cr.version and r.record_id=cr.record_id and r.ref_book_id=cr.ref_book_id),\n" +
            "allRecords as (select cr.id, cr.version as versionStart, nv.version - interval '1' day as versionEnd from currentRecord cr, nextVersion nv)\n" +
            "select distinct id,\n" +
            "case\n" +
            "\twhen (versionEnd is not null and ? > versionEnd) then 1\n" +
            "\twhen ((versionEnd is null or ? <= versionEnd) and ? < versionStart) then -1\n" +
            "\telse 0\n" +
            "end as result\n" +
            "from allRecords";

    @Override
    public List<Pair<Long, Integer>> checkParentConflict(Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        List<Long> ids = new ArrayList<Long>();
        for (RefBookRecord record : records) {
            Long id = record.getValues().get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
            if (id != null) {
                ids.add(id);
            }
        }
        if (!ids.isEmpty()) {
            String sql = String.format(CHECK_PARENT_CONFLICT, SqlUtils.transformToSqlInStatement("id", ids));
            final Set<Pair<Long, Integer>> result = new HashSet<Pair<Long, Integer>>();
            getJdbcTemplate().query(sql, new RowMapper<Pair<Long, Integer>>() {
                @Override
                public Pair<Long, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    result.add(new Pair<Long, Integer>(SqlUtils.getLong(rs, "id"), SqlUtils.getInteger(rs, "result")));
                    return null;
                }
            }, versionTo, versionTo, versionFrom);
            return new ArrayList<Pair<Long, Integer>>(result);
        } else {
            return new ArrayList<Pair<Long, Integer>>();
        }
    }

    @Override
    @CacheEvict(value = "PermanentData", key = "'RefBook_'+#refBookId.toString()")
    public void setScriptId(Long refBookId, String scriptId) {
        getJdbcTemplate().update("update ref_book set script_id = ? where id = ?", scriptId, refBookId);
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        final Map<RefBookAttributePair, String> result = new HashMap<RefBookAttributePair, String>();
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("select record_id, attribute_id, coalesce(string_value, to_char(number_value),to_char(date_value)) as value from ref_book_value where");
        for (Iterator<RefBookAttributePair> it = attributePairs.iterator(); it.hasNext(); ) {
            RefBookAttributePair pair = it.next();
            ps.appendQuery(" (attribute_id = ? and record_id = ?) ");
            ps.addParam(pair.getAttributeId());
            ps.addParam(pair.getUniqueRecordId());
            if (it.hasNext()) {
                ps.appendQuery(" or ");
            }
        }
        getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        result.put(new RefBookAttributePair(rs.getLong("attribute_id"), rs.getLong("record_id")), rs.getString("value"));
                    }
                });
        return result;
    }

    private static final String CHECK_LOOPS = "SELECT CASE WHEN EXISTS (\n" +
            "  WITH value_hierarchy (id, parent_id) AS (\n" +
            "    SELECT rbr.id,rbv.reference_value AS parent_id\n" +
            "    FROM ref_book_record rbr\n" +
            "    join ref_book_attribute rba ON rba.ref_book_id = rbr.ref_book_id AND rba.alias = 'PARENT_ID'\n" +
            "    join ref_book_value rbv ON rbv.record_id = rbr.id AND rbv.attribute_id = rba.id\n" +
            "  )\n" +
            "  SELECT vh.*,LEVEL\n" +
            "  FROM   value_hierarchy vh\n" +
            "  WHERE  id = ?\n" +
            "  START WITH id = ?\n" +
            "  CONNECT BY parent_id = PRIOR id) \n" +
            "  THEN 1 ELSE 0 END AS has_cycle \n" +
            "FROM dual";

    @Override
    public boolean hasLoops(Long uniqueRecordId, Long parentRecordId) {
        return getJdbcTemplate().queryForInt(CHECK_LOOPS, parentRecordId, uniqueRecordId) == 1;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long refBookId, Long uniqueRecordId,
                                                                     PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PreparedStatementData ps = getRefBookSql(refBookId, uniqueRecordId, null, sortAttribute, filter, pagingParams, true);
        RefBook refBookClone = SerializationUtils.clone(get(refBookId));
        refBookClone.getAttributes().add(RefBook.getVersionFromAttribute());
        refBookClone.getAttributes().add(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBookClone));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справочнике
        result.setTotalCount(getRecordVersionsCount(refBookId, uniqueRecordId));
        return result;
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long refBookId, Long uniqueRecordId) {
        List<Pair<RefBookAttribute, RefBookValue>> values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
        List<RefBookAttribute> attributes = getAttributes(refBookId);
        for (RefBookAttribute attribute : attributes) {
            if (attribute.isUnique()) {
                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, getValue(uniqueRecordId, attribute.getId())));
            }
        }
        return values;
    }

    private static final String CHECK_CROSS_VERSIONS = "with allVersions as (select r.* from ref_book_record r where ref_book_id=:refBookId and record_id=:recordId and (:excludedRecordId is null or id != :excludedRecordId)),\n" +
            "recordsByVersion as (select r.*, row_number() over(partition by r.record_id order by r.version) rn from ref_book_record r, allVersions av where r.id=av.id),\n" +
            "versionInfo as (select rv.rn NUM, rv.ID, rv.VERSION, rv.status, rv2.version - interval '1' day nextVersion,rv2.status nextStatus from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn)\n" +
            "select num, id, version, status, nextversion, nextstatus, \n" +
            "case\n" +
            "  when (status=0 and (\n" +
            "  \t(:versionTo is null and (\n" +
            "  \t\t(nextversion is not null and nextversion >= :versionFrom) or \t\t-- 1, 6\n" +
            "\t\t(nextversion is null and version >= :versionFrom)\t\t\t\t\t-- 9, 10, 11, 12\n" +
            "  \t)) or (:versionTo is not null and (\n" +
            "  \t\t(version <= :versionFrom and nextversion >= :versionFrom) or \t\t-- 2, 3\n" +
            "  \t\t(version >= :versionFrom and version <= :versionTo)\t\t\t\t\t-- 4, 5\n" +
            "  \t))\n" +
            "  )) then 1\n" +
            "  when (status=0 and nextversion is null and version < :versionFrom) then 2\t\t\t\t\t\t\t--7, 8\n" +
            "  when (status=2 and (:versionTo is not null and version >= :versionFrom and version < :versionTo and nextversion is not null and nextversion > :versionTo)) then 3 \t\t-- 17\n" +
            "  when (status=2 and (\n" +
            "  \t(nextversion is not null and :versionTo is null and version > :versionFrom) or  \t-- 18\n" +
            "  \t(version = :versionFrom) or \n" +
            "  \t(nextversion is null and version >= :versionFrom)\t\t\t\t\t\t\t\t\t-- 21, 22\n" +
            "  )) then 4\n" +
            "  else 0\n" +
            "end as result\n" +
            "from versionInfo";

    @Override
    public List<CheckCrossVersionsResult> checkCrossVersions(Long refBookId, Long recordId,
                                                             Date versionFrom, Date versionTo, Long excludedRecordId) {


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", refBookId);
        params.put("recordId", recordId);
        params.put("excludedRecordId", excludedRecordId);
        params.put("versionFrom", versionFrom);
        params.put("versionTo", versionTo);

        return getNamedParameterJdbcTemplate().query(CHECK_CROSS_VERSIONS, params, new RowMapper<CheckCrossVersionsResult>() {
            @Override
            public CheckCrossVersionsResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                CheckCrossVersionsResult result = new CheckCrossVersionsResult();
                result.setNum(SqlUtils.getInteger(rs, "NUM"));
                result.setRecordId(SqlUtils.getLong(rs, "ID"));
                result.setVersion(rs.getDate("VERSION"));
                result.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "STATUS")));
                result.setNextVersion(rs.getDate("NEXTVERSION"));
                if (SqlUtils.getInteger(rs, "NEXTSTATUS") != null)
                    result.setNextStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "NEXTSTATUS")));
                else
                    result.setNextStatus(null);
                result.setResult(CrossResult.getResultById(SqlUtils.getInteger(rs, "RESULT")));
                return result;
            }
        });
    }

    private final static String CHECK_UNIQUE_MATCHES = "select distinct v.RECORD_ID as ID, a.NAME as NAME from REF_BOOK_VALUE v, REF_BOOK_RECORD r, REF_BOOK_ATTRIBUTE a \n" +
            "where r.ID = v.RECORD_ID and r.STATUS=0 and a.ID=v.ATTRIBUTE_ID and r.REF_BOOK_ID = ?";

    private final static String CHECK_COMBINED_UNIQUE_MATCHES = "select ID from (\n" +
            "\tselect v.record_id as ID, count(v.record_id) as cnt from ref_book_value v\n" +
            "\tjoin ref_book_record r on (r.id = v.record_id and r.ref_book_id = ? and r.status = 0)\n" +
            "\twhere (\n" +
            "%s" +
            "\n\t)\t\n" +
            "\tgroup by v.record_id\n" +
            ") where cnt = ?";

    @Override
    public List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(Long refBookId, Long uniqueRecordId,
                                                                        List<RefBookAttribute> attributes,
                                                                        List<RefBookRecord> records) {
        boolean hasUniqueAttributes = false;
        PreparedStatementData ps = new PreparedStatementData();

        //Проверяем нет ли особой обработки уникальных атрибутов
        if (!UniqueAttributeCombination.getRefBookIds().contains(refBookId)) {
            ps.appendQuery(CHECK_UNIQUE_MATCHES);
            ps.addParam(refBookId);
            for (RefBookAttribute attribute : attributes) {
                if (attribute.isUnique()) {
                    if (!hasUniqueAttributes) {
                        ps.appendQuery(" and (");
                    } else {
                        ps.appendQuery(" or ");
                    }
                    for (int i = 0; i < records.size(); i++) {
                        RefBookRecord record = records.get(i);
                        Map<String, RefBookValue> values = record.getValues();
                        ps.appendQuery("(v.ATTRIBUTE_ID = ?");
                        ps.addParam(attribute.getId());

                        ps.appendQuery(" and v.");
                        ps.appendQuery(attribute.getAttributeType() + "_VALUE = ?");
                        if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                            ps.addParam(values.get(attribute.getAlias()).getStringValue());
                        }
                        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                            ps.addParam(values.get(attribute.getAlias()).getReferenceValue());
                        }
                        if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                            ps.addParam(values.get(attribute.getAlias()).getNumberValue());
                        }
                        if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                            ps.addParam(values.get(attribute.getAlias()).getDateValue());
                        }
                        ps.appendQuery(" and (? is null or r.RECORD_ID != ?)");
                        ps.addParam(record.getRecordId());
                        ps.addParam(record.getRecordId());
                        ps.appendQuery(")");

                        if (i < records.size() - 1) {
                            ps.appendQuery(" or ");
                        }
                    }
                    hasUniqueAttributes = true;
                }
            }
            if (hasUniqueAttributes) {
                ps.appendQuery(")");
            }
            if (uniqueRecordId != null) {
                ps.appendQuery(" and v.record_id != ?");
                ps.addParam(uniqueRecordId);
            }

            if (hasUniqueAttributes) {
                return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RowMapper<Pair<Long, String>>() {
                    @Override
                    public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Long, String>(SqlUtils.getLong(rs, "ID"), rs.getString("NAME"));
                    }
                });
            } else {
                return null;
            }
        } else {
            List<Long> uniqueAttributes = UniqueAttributeCombination.getByRefBookId(refBookId).getAttributeIds();

            Map<Long, RefBookAttribute> attrMap = new HashMap<Long, RefBookAttribute>();
            for (RefBookAttribute attribute : attributes) {
                attrMap.put(attribute.getId(), attribute);
            }

            ps.appendQuery(CHECK_COMBINED_UNIQUE_MATCHES);
            ps.addParam(refBookId);

            StringBuilder attrQuery = new StringBuilder();

            for (int i = 0; i < records.size(); i++) {
                Map<String, RefBookValue> values = records.get(i).getValues();
                attrQuery.append("\t\t(");
                for (int j = 0; j < uniqueAttributes.size(); j++) {
                    RefBookAttribute attribute = attrMap.get(uniqueAttributes.get(j));
                    String type = attribute.getAttributeType().toString() + "_VALUE";
                    attrQuery.append("(v.attribute_id = ? and v.").append(type).append(" = ?)");
                    ps.addParam(attribute.getId());
                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        ps.addParam(values.get(attribute.getAlias()).getStringValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        ps.addParam(values.get(attribute.getAlias()).getReferenceValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                        ps.addParam(values.get(attribute.getAlias()).getNumberValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                        ps.addParam(values.get(attribute.getAlias()).getDateValue());
                    }
                    if (j < uniqueAttributes.size() - 1) {
                        attrQuery.append(" or ");
                    }
                }
                attrQuery.append(")");
                if (i < records.size() - 1) {
                    attrQuery.append(" or \n");
                } else {
                    if (uniqueRecordId != null) {
                        attrQuery.append(" and v.record_id != ?");
                        ps.addParam(uniqueRecordId);
                    }
                }
            }
            ps.addParam(uniqueAttributes.size());
            String sql = String.format(ps.getQuery().toString(), attrQuery);
            List<Long> matchedIds = getJdbcTemplate().query(sql, ps.getParams().toArray(), new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return SqlUtils.getLong(rs, "ID");
                }
            }
            );
            List<Pair<Long, String>> result = new ArrayList<Pair<Long, String>>();
            for (Long id : matchedIds) {
                for (Long uniqueAttribute : uniqueAttributes) {
                    result.add(new Pair<Long, String>(id, attrMap.get(uniqueAttribute).getName()));
                }
            }
            return result;
        }
    }

    private final static String CHECK_CONFLICT_VALUES_VERSIONS = "with conflictRecord as (select * from REF_BOOK_RECORD where %s),\n" +
            "allRecordsInConflictGroup as (select r.* from REF_BOOK_RECORD r where exists (select 1 from conflictRecord cr where r.REF_BOOK_ID=cr.REF_BOOK_ID and r.RECORD_ID=cr.RECORD_ID)),\n" +
            "recordsByVersion as (select ar.*, row_number() over(partition by ar.RECORD_ID order by ar.version) rn from allRecordsInConflictGroup ar),\n" +
            "versionInfo as (select rv.ID, rv.VERSION versionFrom, rv2.version - interval '1' day versionTo from conflictRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.ID=cr.ID)" +
            "select ID from versionInfo where (\n" +
            "\tversionTo IS NOT NULL and (\n" +
            "\t\t(:versionTo IS NULL and versionTo >= :versionFrom) or\n" +
            "\t\t(versionFrom <= :versionFrom and versionTo >= :versionFrom) or \n" +
            "\t\t(versionFrom >= :versionFrom and versionFrom <= :versionTo)\n" +
            "\t)\n" +
            ") or (\n" +
            "\tversionTo IS NULL and (\n" +
            "\t\tversionFrom <= :versionFrom or\n" +
            "\t\t(versionFrom >= :versionFrom and (:versionTo IS NULL or versionFrom <= :versionTo))\n" +
            "\t)\n" +
            ")";

    @Override
    public List<Long> checkConflictValuesVersions(List<Pair<Long, String>> recordPairs, Date versionFrom, Date versionTo) {
        List<Long> recordIds = new ArrayList<Long>();
        for (Pair<Long, String> pair : recordPairs) {
            recordIds.add(pair.getFirst());
        }

        String sql = String.format(CHECK_CONFLICT_VALUES_VERSIONS,
                SqlUtils.transformToSqlInStatement("ID", recordIds));
        Map<String, Date> params = new HashMap<String, Date>();
        params.put("versionFrom", versionFrom);
        params.put("versionTo", versionTo);
        return getNamedParameterJdbcTemplate().queryForList(sql, params, Long.class);
    }

    @Override
    public boolean isVersionUsed(Long refBookId, Long uniqueRecordId, Date versionFrom) {
        //Проверка использования в справочниках и настройках подразделений
        boolean hasUsages = getJdbcTemplate().queryForInt("select count(r.id) from ref_book_record r, ref_book_value v " +
                "where r.id=v.record_id and v.attribute_id in (select id from ref_book_attribute where reference_id=?) and r.version >= ? and v.REFERENCE_VALUE=?",
                refBookId, versionFrom, uniqueRecordId) != 0;
        if (!hasUsages) {
            //Проверка использования в налоговых формах
            return getJdbcTemplate().queryForInt("select count(*) from report_period where id in \n" +
                    "(select report_period_id from form_data where id in \n" +
                    "(select form_data_id from data_row where id in \n" +
                    "(select row_id from numeric_value where column_id in \n" +
                    "(select id from form_column where attribute_id in \n" +
                    "(select id from ref_book_attribute where ref_book_id = ?)) and value = ?))) and start_date > ?",
                    refBookId, uniqueRecordId, versionFrom) != 0;
        } else {
            return hasUsages;
        }
    }

    private static final String CHECK_USAGES_IN_REFBOOK = "select \n" +
            "  b.name as refBookName, \n" +
            "  r.version as versionStart\n" +
            "from ref_book b\n" +
            "join ref_book_record r on r.ref_book_id = b.id \n" +
            "join ref_book_value v on (v.record_id = r.id and %s)\n" +
            "join ref_book_attribute a on (a.id = v.attribute_id and a.reference_id = :refBookId)";

    private static final String CHECK_USAGES_IN_REFBOOK_WITH_PERIOD_RESTRICTION = "select refBookName, versionStart from (\n" +
            "  select r.version as versionStart, (select min(version) - interval '1' day from ref_book_record rn where rn.ref_book_id = r.ref_book_id and rn.record_id = r.record_id and rn.version > r.version) as versionEnd,\n" +
            "  b.name as refBookName\n" +
            "  from ref_book b\n" +
            "  join ref_book_record r on r.ref_book_id = b.id\n" +
            "  join ref_book_value v on (v.record_id = r.id and %s)\n" +
            "  join ref_book_attribute a on (a.id = v.attribute_id and a.reference_id = :refBookId)\n" +
            ") where (:versionTo is not null and :versionTo < versionStart) or (versionEnd is not null and versionEnd < :versionFrom)";

    private static final String CHECK_USAGES_IN_FORMS = "with forms as (\n" +
            "  select fd.* from form_data fd \n" +
            "  join data_row dr on dr.form_data_id = fd.id\n" +
            "  join numeric_value nv on nv.row_id = dr.id\n" +
            "  join form_column fc on fc.id = nv.column_id\n" +
            "  join ref_book_attribute a on a.id = fc.attribute_id\n" +
            "  join ref_book_record r on r.id = nv.value\n" +
            "  where %s\n" +
            ")" +
            "select distinct f.kind as formKind, t.name as formType, d.path as departmentPath, d.type as departmentType, rp.name as reportPeriodName, tp.year as year from forms f \n" +
            "join (select d.id, d.type, substr(sys_connect_by_path(name,'/'), 2) as path \n" +
            "\t\tfrom department d\n" +
            "\t\twhere d.id in (select department_id from forms)\n" +
            "\t\tstart with d.id = 0\n" +
            "\t\tconnect by prior d.id = d.parent_id) d on d.id=f.department_id\n" +
            "join form_type t on t.id in (select t.type_id from form_template t, forms f where t.id = f.form_template_id)\n" +
            "join report_period rp on rp.id = f.report_period_id\n" +
            "join tax_period tp on tp.id = rp.tax_period_id";

    private static final String CHECK_USAGES_IN_DEPARTMENT_CONFIG = "select * from (with checkRecords as (select * from ref_book_record r where %s),\n" +
            "periodCodes as (select a.alias, v.* from ref_book_value v, ref_book_attribute a where v.attribute_id=a.id and a.ref_book_id=8),\n" +
            "usages as (select r.* from ref_book_value v, ref_book_record r, checkRecords cr " +
            "where v.attribute_id in (select id from ref_book_attribute where ref_book_id in (31,33,37,98,99) and id not in (170,192,180)) and v.reference_value = cr.id and r.id=v.record_id)\n" +   //170,192,180 - ссылки на подразделения
            "select distinct d.name as departmentName, concat(pn.string_value, to_char(u.version,' yyyy')) as periodName, nt.number_value as isT, ni.number_value as isI, nd.number_value as isD, nv.number_value as isV, np.number_value as isP,\n" +
            "to_date(concat(to_char(ps.date_value,'dd.mm'), to_char(u.version,'.yyyy')), 'DD.MM.YYYY') as periodStart, to_date(concat(to_char(pe.date_value,'dd.mm'), to_char(u.version,'.yyyy')), 'DD.MM.YYYY') as periodEnd,\n" +
            "case\n" +
            "\twhen u.ref_book_id = 31 then 'T'\n" +        //Транспортный налог
            "\twhen u.ref_book_id = 33 then 'I'\n" +        //Налог на прибыль
            "\twhen u.ref_book_id = 37 then 'D'\n" +        //Учет контролируемых сделок
            "\twhen u.ref_book_id = 98 then 'V'\n" +        //НДС
            "\twhen u.ref_book_id = 99 then 'P'\n" +        //Налог на имущество
            "\telse null\n" +
            "end as taxCode\n" +
            "from usages u\n" +
            "join department d on d.id in (select v.reference_value from ref_book_value v, usages u where v.record_id=u.id and v.attribute_id = (select id from ref_book_attribute where ref_book_id=u.ref_book_id and alias='DEPARTMENT_ID'))\n" +
            "join (select date_value, record_id from periodCodes where alias='CALENDAR_START_DATE') ps on to_char(ps.date_value,'dd.mm')=to_char(u.version,'dd.mm')\n" +
            "join (select date_value, record_id from periodCodes where alias='END_DATE') pe on pe.record_id = ps.record_id\n" +
            "join (select string_value, record_id from periodCodes where alias='NAME') pn on pn.record_id=ps.record_id\n" +
            "join (select number_value, record_id from periodCodes where alias='T') nt on nt.record_id=ps.record_id\n" +
            "join (select number_value, record_id from periodCodes where alias='I') ni on ni.record_id=ps.record_id\n" +
            "join (select number_value, record_id from periodCodes where alias='D') nd on nd.record_id=ps.record_id\n" +
            "join (select number_value, record_id from periodCodes where alias='V') nv on nv.record_id=ps.record_id\n" +
            "join (select number_value, record_id from periodCodes where alias='P') np on np.record_id=ps.record_id)\n" +
            "where taxCode is not null and ((isI = 1 and taxCode = 'I') or (isT = 1 and taxCode = 'T') or (isD = 1 and taxCode = 'D') or (isV = 1 and taxCode = 'V') or (isP = 1 and taxCode = 'P'))";

    @Override
    public List<String> isVersionUsed(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                      boolean isValuesChanged) {
        Set<String> results = new HashSet<String>();
        String in = SqlUtils.transformToSqlInStatement("v.reference_value", uniqueRecordIds);
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        //Проверка использования в справочниках
        try {
            if (isValuesChanged) {
                sql = String.format(CHECK_USAGES_IN_REFBOOK, in);
                params.put("refBookId", refBookId);
            } else {
                /** Если атрибуты не были изменены то дополнительно фильтруем по периоду актуальности */
                sql = String.format(CHECK_USAGES_IN_REFBOOK_WITH_PERIOD_RESTRICTION, in);
                params.put("refBookId", refBookId);
                params.put("versionFrom", versionFrom);
                params.put("versionTo", versionTo);
            }
            results.addAll(getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return "Существует ссылка на запись справочника. Справочник " + rs.getString("refBookName") + ", действует с " + sdf.format(rs.getDate("versionStart")) + ".";
                }
            }));
        } catch (EmptyResultDataAccessException e) {
            //do nothing
        }

        try {
            //Проверка использования в налоговых формах
            in = SqlUtils.transformToSqlInStatement("nv.value", uniqueRecordIds);
            sql = String.format(CHECK_USAGES_IN_FORMS, in);
            params.clear();
            params.put("refBookId", refBookId);
            if (!isValuesChanged) {
                /** Если атрибуты не были изменены то дополнительно фильтруем по периоду актуальности */
                sql += "\n where (:versionTo is not null and :versionTo < rp.calendar_start_date) or (rp.end_date is not null and rp.end_date < :versionFrom)";
                params.put("versionFrom", versionFrom);
                params.put("versionTo", versionTo);
            }
            results.addAll(getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    StringBuilder result = new StringBuilder();
                    result.append("Существует экземпляр налоговой формы \"");
                    result.append(FormDataKind.fromId(SqlUtils.getInteger(rs, "formKind")).getName()).append("\" типа \"");
                    result.append(rs.getString("formType")).append("\" в подразделении \"");
                    if (SqlUtils.getInteger(rs, "departmentType") != 1) {
                        result.append(rs.getString("departmentPath").substring(rs.getString("departmentPath").indexOf("/") + 1)).append("\" периоде \"");
                    } else {
                        result.append(rs.getString("departmentPath")).append("\" периоде \"");
                    }
                    result.append(rs.getString("reportPeriodName")).append(" ").append(rs.getString("year")).append("\", который содержит ссылку на версию!");
                    return result.toString();
                }
            }));
        } catch (EmptyResultDataAccessException e) {
            //do nothing
        }

        try {
            //Проверка использования в настройках подразделений
            in = SqlUtils.transformToSqlInStatement("r.id", uniqueRecordIds);
            sql = String.format(CHECK_USAGES_IN_DEPARTMENT_CONFIG, in);
            params.clear();
            if (!isValuesChanged) {
                /** Если атрибуты не были изменены то дополнительно фильтруем по периоду актуальности */
                sql += "\n and ((:versionTo is not null and :versionTo < periodStart) or (:versionFrom > periodEnd))";
                params.put("versionFrom", versionFrom);
                params.put("versionTo", versionTo);
            }
            results.addAll(getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    StringBuilder result = new StringBuilder();
                    result.append("В настройке подразделения \"");
                    result.append(rs.getString("departmentName")).append("\" для налога \"");
                    result.append(TaxTypeCase.fromCode(rs.getString("taxCode").charAt(0)).getGenitive()).append("\" в периоде \"");
                    result.append(rs.getString("periodName")).append("\" указана ссылка на версию!");
                    return result.toString();
                }
            }));
        } catch (EmptyResultDataAccessException e) {
            //do nothing
        }
        return new ArrayList<String>(results);
    }

    @Override
    public Collection<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds) {
        Set<String> results = new HashSet<String>();
        String in = SqlUtils.transformToSqlInStatement("v.reference_value", uniqueRecordIds);
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        //Проверка использования в справочниках
        try {
            sql = String.format(CHECK_USAGES_IN_REFBOOK, in);
            params.put("refBookId", refBookId);
            results.addAll(getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return "Существует ссылка на запись справочника. Справочник " + rs.getString("refBookName") + ", действует с " + sdf.format(rs.getDate("versionStart")) + ".";
                }
            }));
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<String>(0);
        } catch (DataAccessException e) {
            logger.error("Проверка использования", e);
            throw new DaoException("Проверка использования", e);
        }
        return results;
    }

    private static final String GET_NEXT_RECORD_VERSION = "with nextVersion as (select r.* from ref_book_record r where r.ref_book_id = ? and r.record_id = ? and r.version  = \n" +
            "\t(select min(version) from ref_book_record where ref_book_id=r.ref_book_id and record_id=r.record_id and status=0 and version > ?)),\n" +
            "minNextVersion as (select r.ref_book_id, r.record_id, min(r.version) version from ref_book_record r, nextVersion nv where r.version > nv.version and r.record_id= nv.record_id and r.ref_book_id= nv.ref_book_id group by r.ref_book_id, r.record_id),\n" +
            "nextVersionEnd as (select mnv.ref_book_id, mnv.record_id, mnv.version, r.status from minNextVersion mnv, ref_book_record r where mnv.ref_book_id=r.ref_book_id and mnv.version=r.version and mnv.record_id=r.record_id)\n" +
            "select nv.id as %s, nv.version as versionStart, nve.version - interval '1' day as versionEnd,\n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from nextVersion nv \n" +
            "left join nextVersionEnd nve on (nve.record_id= nv.record_id and nve.ref_book_id= nv.ref_book_id)";

    @Override
    public RefBookRecordVersion getNextVersion(Long refBookId, Long recordId, Date versionFrom) {
        String sql = String.format(GET_NEXT_RECORD_VERSION, RefBook.RECORD_ID_ALIAS);
        try {
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{
                            refBookId, recordId, versionFrom
                    },
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String GET_PREVIOUS_RECORD_VERSION = "with previousVersion as (select r.* from ref_book_record r where r.ref_book_id = ? and r.record_id = ? and r.version  = \n" +
            "\t(select max(version) from ref_book_record where ref_book_id=r.ref_book_id and record_id=r.record_id and status=0 and version < ?)),\n" +
            "minNextVersion as (select r.ref_book_id, r.record_id, min(r.version) version from ref_book_record r, previousVersion pv where r.version > pv.version and r.record_id= pv.record_id and r.ref_book_id= pv.ref_book_id group by r.ref_book_id, r.record_id),\n" +
            "nextVersionEnd as (select mnv.ref_book_id, mnv.record_id, mnv.version, r.status from minNextVersion mnv, ref_book_record r where mnv.ref_book_id=r.ref_book_id and mnv.version=r.version and mnv.record_id=r.record_id)\n" +
            "select pv.id as %s, pv.version as versionStart, nve.version - interval '1' day as versionEnd,\n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from previousVersion pv \n" +
            "left join nextVersionEnd nve on (nve.record_id= pv.record_id and nve.ref_book_id= pv.ref_book_id)";

    @Override
    public RefBookRecordVersion getPreviousVersion(Long refBookId, Long recordId, Date versionFrom) {
        String sql = String.format(GET_PREVIOUS_RECORD_VERSION, RefBook.RECORD_ID_ALIAS);
        try {
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{
                            refBookId, recordId, versionFrom
                    },
                    new RefBookUtils.RecordVersionMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForLong("select record_id from ref_book_record where id=?", uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдена запись справочника с id = %d", uniqueRecordId));
        }
    }

    @Override
    public Long findRecord(Long refBookId, Long recordId, Date version) {
        return getJdbcTemplate().queryForLong("select id from ref_book_record where ref_book_id = ? and record_id = ? and version = ?", refBookId, recordId, version);
    }

    private static final String DELETE_ALL_VERSIONS = "delete from ref_book_record where ref_book_id=? and record_id in (select record_id from ref_book_record where %s)";

    @Override
    public void deleteAllRecordVersions(Long refBookId, List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_ALL_VERSIONS, SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql, refBookId);
    }

    private static final String GET_RELATED_VERSIONS = "with currentRecord as (select id, record_id, ref_book_id from REF_BOOK_RECORD where %s),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, STATUS, VERSION, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r, currentRecord cr where r.ref_book_id=cr.ref_book_id and r.record_id=cr.record_id) \n" +
            "select rv2.ID from currentRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where cr.id=rv.id and rv2.status=%d";

    @Override
    public List<Long> getRelatedVersions(List<Long> uniqueRecordIds) {
        try {
            String sql = String.format(GET_RELATED_VERSIONS,
                    SqlUtils.transformToSqlInStatement("id", uniqueRecordIds), VersionedObjectStatus.FAKE.getId());
            return getJdbcTemplate().queryForList(sql, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    /**
     * dloshkarev: Секция со старыми методами, оставленными для совместимости
     */

    private static final String INSERT_REF_BOOK_RECORD_SQL_OLD = "insert into ref_book_record (id, ref_book_id, version," +
            "status, record_id) values (?, %d, to_date('%s', 'DD.MM.YYYY'), 0, seq_ref_book_record_row_id.nextval)";
    private static final String INSERT_REF_BOOK_VALUE_OLD = "insert into ref_book_value (record_id, attribute_id," +
            "string_value, number_value, date_value, reference_value) values (?, ?, ?, ?, ?, ?)";

    @Override
    public void createRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records) {
        // нет данных - нет работы
        if (records.size() == 0) {
            return;
        }
        final List<Long> refBookRecordIds = dbUtils.getNextRefBookRecordIds(Long.valueOf(records.size()));

        BatchPreparedStatementSetter batchRefBookRecordsPS = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, refBookRecordIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return refBookRecordIds.size();
            }
        };

        JdbcTemplate jt = getJdbcTemplate();
        jt.batchUpdate(String.format(INSERT_REF_BOOK_RECORD_SQL_OLD, refBookId, sdf.format(version)), batchRefBookRecordsPS);

        List<Object[]> listValues = getListValuesForBatch(refBookId, records, refBookRecordIds);
        jt.batchUpdate(INSERT_REF_BOOK_VALUE_OLD, listValues);
    }

    /**
     * Преобразует данные элементов справочников в удобный формат для передачи значений в batchUpdate
     *
     * @param refBookId        код справочника, чьи данные необходимо записать
     * @param records          данные элементов справочника для преобразования
     * @param refBookRecordIds идентификаторы новых строк
     * @return данные в формате batchUpdate
     */
    private List<Object[]> getListValuesForBatch(@NotNull Long refBookId, @NotNull List<Map<String, RefBookValue>> records,
                                                 @NotNull final List<Long> refBookRecordIds) {
        RefBook refBook = get(refBookId);
        List<Object[]> listValues = new ArrayList<Object[]>();
        for (int i = 0; i < records.size(); i++) {
            // создаем строки справочника

            // записываем значения ячеек
            Map<String, RefBookValue> record = records.get(i);
            List<RefBookAttribute> attributes = refBook.getAttributes();

            // проверка обязательности заполнения записей справочника
            List<String> errors = RefBookUtils.checkFillRequiredRefBookAtributes(attributes, record);
            if (errors.size() > 0) {
                throw new DaoException("Поля " + errors.toString() + "являются обязательными для заполнения");
            }

            for (Map.Entry<String, RefBookValue> entry : record.entrySet()) {
                String attributeAlias = entry.getKey();
                if (RefBook.RECORD_ID_ALIAS.equals(attributeAlias) ||
                        RefBook.RECORD_PARENT_ID_ALIAS.equals(attributeAlias)) {
                    continue;
                }
                RefBookAttribute attribute = refBook.getAttribute(attributeAlias);
                Object[] values = new Object[6];
                values[0] = refBookRecordIds.get(i);
                values[1] = attribute.getId();
                values[2] = null;
                values[3] = null;
                values[4] = null;
                values[5] = null;
                switch (attribute.getAttributeType()) {
                    case STRING: {
                        values[2] = entry.getValue().getStringValue();
                    }
                    break;
                    case NUMBER: {
                        if (entry.getValue().getNumberValue() != null) {
                            values[3] = BigDecimal.valueOf(entry.getValue().getNumberValue().doubleValue())
                                    .setScale(attribute.getPrecision(), RoundingMode.HALF_UP).doubleValue();
                        }
                    }
                    break;
                    case DATE: {
                        values[4] = entry.getValue().getDateValue();
                    }
                    break;
                    case REFERENCE: {
                        values[5] = entry.getValue().getReferenceValue();
                    }
                    break;
                }
                listValues.add(values);
            }
        }
        return listValues;
    }

    private static final String UPDATE_REF_BOOK_RECORD_SQL_OLD = "insert into ref_book_record (id, ref_book_id, version," +
            "status, record_id) values (?, %d, to_date('%s', 'DD.MM.YYYY'), 0, ?)";

    private static final String DELETE_REF_BOOK_VALUE_SQL_OLD = "delete from ref_book_value where record_id in " +
            "(select id from ref_book_record where ref_book_id = ? and version = trunc(?, 'DD') and record_id = ?)";


    private Long getRowId(@NotNull Long recordId) {
        return getJdbcTemplate().queryForLong("select record_id from ref_book_record where id = ?", new Object[]{recordId});
    }

    @Override
    public void updateRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records) {
        try {
            //TODO: возможно стоит добавить проверку, что запись еще не удалена (Marat Fayzullin 2013-07-26)
            // нет данных - нет работы
            if (records.size() == 0) {
                return;
            }
            RefBook refBook = get(refBookId);
            List<Object[]> recordAddIds = new ArrayList<Object[]>();
            List<Object[]> listValues = new ArrayList<Object[]>();
            List<Object[]> delValues = new LinkedList<Object[]>();
            List<Long> recordsId = new ArrayList<Long>();
            int needIdsCnt = 0;
            for (int i = 0; i < records.size(); i++) {
                Map<String, RefBookValue> record = records.get(i);

                // проверка обязательности заполнения записей справочника
                checkFillRequiredFields(record, refBook);

                // создаем строки справочника
                Long rowId = getRowId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());

                Long recordId = checkRecordUnique(refBookId, version, rowId);

                if (recordId == null) {
                    needIdsCnt++;
                    recordAddIds.add(new Object[]{recordId, rowId});
                } else {
                    delValues.add(new Object[]{refBookId, version, rowId});
                }
                recordsId.add(i, recordId);
            }

            // генерация нужного количества id'шников
            List<Long> refBookRecordIds = dbUtils.getNextRefBookRecordIds(Long.valueOf(needIdsCnt));
            Iterator<Long> idsIterator = refBookRecordIds.iterator();
            for (int i = 0; i < recordAddIds.size(); i++) {
                if (recordAddIds.get(i)[0] == null) {
                    recordAddIds.set(i, new Object[]{idsIterator.next(), recordAddIds.get(i)[1]});
                }
            }

            Iterator<Long> iterator = refBookRecordIds.iterator();
            for (int i = 0; i < recordsId.size(); i++) {
                if (recordsId.get(i) == null) {
                    recordsId.set(i, iterator.next());
                }
            }

            for (int i = 0; i < records.size(); i++) {
                Map<String, RefBookValue> record = records.get(i);
                Long recordId = recordsId.get(i);
                for (Map.Entry<String, RefBookValue> entry : record.entrySet()) {
                    String attributeAlias = entry.getKey();
                    if (RefBook.RECORD_ID_ALIAS.equals(attributeAlias) ||
                            RefBook.RECORD_PARENT_ID_ALIAS.equals(attributeAlias)) {
                        continue;
                    }
                    RefBookAttribute attribute = refBook.getAttribute(attributeAlias);
                    Object[] values = new Object[]{recordId, attribute.getId(), null, null, null, null};
                    switch (attribute.getAttributeType()) {
                        case STRING: {
                            values[2] = entry.getValue().getStringValue();
                        }
                        break;
                        case NUMBER: {
                            if (entry.getValue().getNumberValue() != null) {
                                values[3] = BigDecimal.valueOf(entry.getValue().getNumberValue().doubleValue())
                                        .setScale(attribute.getPrecision(), RoundingMode.HALF_UP).doubleValue();
                            }
                        }
                        break;
                        case DATE: {
                            values[4] = entry.getValue().getDateValue();
                        }
                        break;
                        case REFERENCE: {
                            values[5] = entry.getValue().getReferenceValue();
                        }
                        break;
                    }
                    listValues.add(values);
                }
            }
            JdbcTemplate jt = getJdbcTemplate();
            // - REF_BOOK_VALUE
            if (!delValues.isEmpty()) {
                jt.batchUpdate(DELETE_REF_BOOK_VALUE_SQL_OLD, delValues);
            }
            // + REF_BOOK_RECORD
            if (!recordAddIds.isEmpty()) {
                jt.batchUpdate(String.format(UPDATE_REF_BOOK_RECORD_SQL_OLD, refBookId, sdf.format(version)), recordAddIds);
            }
            // + REF_BOOK_VALUE
            jt.batchUpdate(INSERT_REF_BOOK_VALUE_OLD, listValues);
        } catch (DaoException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DaoException("Не удалось обновить значения справочника", ex);
        }
    }

    private static final String DELETE_REF_BOOK_RECORD_SQL_I_OLD = "insert into ref_book_record (id, ref_book_id, version," +
            "status, record_id) values (seq_ref_book_record.nextval, %d, to_date('%s', 'DD.MM.YYYY'), -1, ?)";
    private static final String DELETE_REF_BOOK_RECORD_SQL_D_OLD = "delete from ref_book_record where id = ?";

    @Override
    public void deleteRecords(Long refBookId, Date version, List<Long> recordIds) {
        //TODO: возможно стоит добавить проверку, что запись еще не удалена (Marat Fayzullin 2013-07-26)
        if (refBookId == null || version == null || recordIds == null) {
            throw new IllegalArgumentException("refBookId: " + refBookId + "; version: " + version + "; recordIds: " + recordIds);
        }
        // нет данных - нет работы
        if (recordIds.size() == 0) {
            return;
        }
        List<Object[]> insertValues = new ArrayList<Object[]>();
        List<Object[]> deleteValues = new ArrayList<Object[]>();
        for (int i = 0; i < recordIds.size(); i++) {
            Long id = recordIds.get(i);
            Long rowId = getRowId(id);
            Long recordId = checkRecordUnique(refBookId, version, rowId);
            if (recordId == null) {
                insertValues.add(new Object[]{rowId});
            } else {
                deleteValues.add(new Object[]{id});
            }
        }
        JdbcTemplate jt = getJdbcTemplate();
        if (insertValues.size() > 0) {
            jt.batchUpdate(String.format(DELETE_REF_BOOK_RECORD_SQL_I_OLD, refBookId, sdf.format(version)), insertValues);
        }
        if (deleteValues.size() > 0) {
            jt.batchUpdate(String.format(DELETE_REF_BOOK_RECORD_SQL_D_OLD, refBookId, sdf.format(version)), deleteValues);
        }
    }

    private static final String DELETE_MARK_ALL_REF_BOOK_RECORD_SQL_OLD = "insert into ref_book_record (id, ref_book_id, " +
            "version, status, record_id) " +
            "select seq_ref_book_record.nextval, ref_book_id, trunc(?, 'DD'), -1, record_id " +
            "from ref_book_record " +
            "where version = (select max(version) from ref_book_record where ref_book_id = ? " +
            "and version <= trunc(?, 'DD')) " +
            "and ref_book_id = ?";

    @Override
    public void deleteAllRecords(Long refBookId, Date version) {
        if (refBookId == null || version == null) {
            return;
        }
        //TODO Отрефакторить http://jira.aplana.com/browse/SBRFACCTAX-3891 (Marat Fayzullin 2013-08-31)
        // Отметка записей ближайшей меньшей версии как удаленных
        getJdbcTemplate().update(DELETE_MARK_ALL_REF_BOOK_RECORD_SQL_OLD,
                new Object[]{version, refBookId, version, refBookId},
                new int[]{Types.TIMESTAMP, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC});
    }

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     *
     * @param tableName       название таблицы для которой формируется запрос
     * @param refBook         справочник
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @param whereClause
     * @return
     */
    @Override
    public PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
        String orderBy = "";
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT row_number_over, ");
        ps.appendQuery("id ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(" FROM (SELECT ");
        appendSortClause(ps, refBook, sortAttribute, isSortAscending, "frb.");
        ps.appendQuery(", frb.* FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" frb ");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getJoinPartsOfQuery() != null){
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }
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

    private void appendSortClause(PreparedStatementData ps, RefBook refBook, RefBookAttribute sortAttribute, boolean isSortAscending, String prefix) {
        RefBookAttribute defaultSort = refBook.getSortAttribute();
        String sortAlias = sortAttribute == null ? (defaultSort == null ? "id" : defaultSort.getAlias()) : sortAttribute.getAlias();
        if (isSupportOver()) {
            // row_number() over (order by ... asc\desc)
            ps.appendQuery("row_number() over ( order by ");
            ps.appendQuery(prefix);
            ps.appendQuery(sortAlias);
            ps.appendQuery(isSortAscending ? " ASC)" : " DESC)");
        } else {
            ps.appendQuery("rownum");
        }
        ps.appendQuery(" as ");
        ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
    }

    /**
     * Формирует простой sql-запрос по выборке данных учитывая иерархичность таблицы
     *
     * @param tableName       название таблицы для которой формируется запрос
     * @param refBook         справочник
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
        appendSortClause(ps, refBook, sortAttribute, isSortAscending, "");

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(" FROM (SELECT distinct ");
        ps.appendQuery(" CONNECT_BY_ROOT ID as \"RECORD_ID\" ");
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", CONNECT_BY_ROOT frb.");
            ps.appendQuery(attribute.getAlias());
            ps.appendQuery(" as \"");
            ps.appendQuery(attribute.getAlias());
            ps.appendQuery("\" ");
        }

        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" frb ");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getJoinPartsOfQuery() != null){
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }

        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE ");
            if (parentId == null) {
                ps.appendQuery("frb.PARENT_ID is null or (");
            }
            ps.appendQuery(filterPS.getQuery().toString());
            if (filterPS.getParams().size() > 0) {
                ps.addParam(filterPS.getParams());
            }
            if (parentId == null) {
                ps.appendQuery(")");
            }
        }

        ps.appendQuery(" CONNECT BY NOCYCLE PRIOR frb.id = frb.PARENT_ID ");
        ps.appendQuery(" START WITH ");
        ps.appendQuery(parentId == null ? " frb.PARENT_ID is null " : " frb.PARENT_ID = " + parentId);

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

    @Override
    public PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams, String whereClause) {
        return getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, true, whereClause);
    }

    /**
     * Загружает данные справочника на определенную дату актуальности
     *
     * @param tableName     название таблицы
     * @param refBookId     код справочника
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter        условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param whereClause   дополнительный фильтр для секции WHERE
     * @return
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause) {
        RefBook refBook = get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, isSortAscending, whereClause);
        List<Map<String, RefBookValue>> records = getRecordsData(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, null, isSortAscending, whereClause);
        result.setTotalCount(getRecordsCount(ps));
        return result;
    }

    @Override
    public List<Long> getUniqueRecordIds(Long refBookId, String tableName, String filter) {
        RefBook refBook = get(refBookId);

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT ");
        ps.appendQuery("id ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" t");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
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
                return SqlUtils.getLong(rs, RefBook.RECORD_ID_ALIAS);
            }
        });
    }

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     *
     * @param refBookId ид справочника
     * @param tableName название таблицы
     * @param filter    условие фильтрации строк. Может быть не задано
     * @return количество
     */
    @Override
    public int getRecordsCount(Long refBookId, String tableName, String filter) {
        RefBook refBook = get(refBookId);
        PreparedStatementData ps = getSimpleQuery(refBook, tableName, null, filter, null, false, null);
        return getRecordsCount(ps);
    }

    /**
     * Возвращает дочерние записи справочника учитывая иерархичность таблицы
     *
     * @param tableName     название таблицы
     * @param refBookId     код справочника
     * @param pagingParams  определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter        условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, String tableName, Long parentId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = get(refBookId);
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
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, String whereClause) {
        return getRecords(refBookId, tableName, pagingParams, filter, sortAttribute, true, whereClause);
    }

    /**
     * Возвращает элементы справочника
     *
     * @param ps
     * @param refBook
     * @return
     */
    @Override
    public List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook) {
        if (ps.getParams().size() > 0) {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        } else {
            return getJdbcTemplate().query(ps.getQuery().toString(), new RefBookValueMapper(refBook));
        }
    }

    @Override
    public Long getRowNum(Long refBookId, String tableName, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause) {
        RefBook refBook = get(refBookId);
        PreparedStatementData ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, null, isSortAscending, whereClause);
        return getRowNum(ps, recordId);
    }

    /**
     * Возвращает row_num для элемента справочника
     *
     * @param ps
     * @param recordId
     * @return
     */
    @Override
    public Long getRowNum(PreparedStatementData ps, Long recordId) {
        try {
            ps.addParam(recordId);
            return getJdbcTemplate().queryForLong("select " + RefBook.RECORD_SORT_ALIAS + " from (" + ps.getQuery().toString() + ") where " + RefBook.RECORD_ID_ALIAS + " = ?", ps.getParams().toArray());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Integer getRecordsCount(PreparedStatementData ps) {
        if (ps.getParams().size() > 0) {
            return getJdbcTemplate().queryForInt("select count(*) from (" + ps.getQuery().toString() + ")", ps.getParams().toArray());
        } else {
            return getJdbcTemplate().queryForInt("select count(*) from (" + ps.getQuery().toString() + ")");
        }
    }

    @Override
    public void updateVersionRelevancePeriod(String tableName, Long uniqueRecordId, Date version) {
        getJdbcTemplate().update(String.format("update %s set version=? where id=?", tableName), version, uniqueRecordId);
    }

    private final static String CHECK_REFERENCE_VERSIONS_START = "select id from %s where VERSION < ? and %s";

    private final static String CHECK_REFERENCE_VERSIONS_IN_PERIOD = "select id from (\n" +
            "  select r.id, r.version as versionStart, (select min(version) - interval '1' day from %s rn where rn.ref_book_id = r.ref_book_id and rn.record_id = r.record_id and rn.version > r.version) as versionEnd \n" +
            "  from %s r\n" +
            "  where %s\n" +
            ") where (:versionTo is not null and :versionTo < versionStart) or (versionEnd is not null and versionEnd < :versionFrom)";

    @Override
    public void isReferenceValuesCorrect(Logger logger, String tableName, @NotNull Date versionFrom, Date versionTo, @NotNull List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        if (attributes.size() > 0) {
            List<Long> in = new ArrayList<Long>();
            Map<Long, String> attributeIds = new HashMap<Long, String>();
            for (RefBookRecord record : records) {
                Map<String, RefBookValue> values = record.getValues();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE) &&
                            values.get(attribute.getAlias()) != null && !values.get(attribute.getAlias()).isEmpty() &&
                            !attribute.getAlias().equals("DEPARTMENT_ID")) {       //Подразделения не версионируются и их нет смысла проверять
                        Long id = values.get(attribute.getAlias()).getReferenceValue();
                        attributeIds.put(id, attribute.getName());
                        in.add(id);
                    }
                }
            }

            if (in.size() != 0) {
                /** Проверяем пересекаются ли периоды ссылочных атрибутов с периодом текущей записи справочника */
                String sql = String.format(CHECK_REFERENCE_VERSIONS_IN_PERIOD, tableName, tableName, SqlUtils.transformToSqlInStatement("id", in));
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
                sql = String.format(CHECK_REFERENCE_VERSIONS_START, tableName, SqlUtils.transformToSqlInStatement("id", in));
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

    @Override
    public Map<String, RefBookValue> getRecordData(final Long refBookId, final String tableName, final Long recordId) {
        RefBook refBook = get(refBookId);
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

    @Override
    public String buildUniqueRecordName(RefBook refBook, List<Pair<RefBookAttribute, RefBookValue>> values) {
        RefBookFactory refBookFactory = applicationContext.getBean("refBookFactory", RefBookFactory.class);
        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
        Map<String, String> refAliases = new HashMap<String, String>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
                RefBook refRefBook = refBookFactory.get(attribute.getRefBookId());
                RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
                refAliases.put(attribute.getAlias(), refAttribute.getAlias());
            }
        }

        StringBuilder uniqueValues = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            RefBookAttribute attribute = values.get(i).getFirst();
            RefBookValue value = values.get(i).getSecond();
            switch (attribute.getAttributeType()) {
                case NUMBER:
                    if (value.getNumberValue() != null) {
                        uniqueValues.append(value.getNumberValue().toString());
                    }
                    break;
                case DATE:
                    if (value.getDateValue() != null) {
                        uniqueValues.append(value.getDateValue().toString());
                    }
                    break;
                case STRING:
                    if (value.getStringValue() != null) {
                        uniqueValues.append(value.getStringValue());
                    }
                    break;
                case REFERENCE:
                    if (value.getReferenceValue() != null) {
                        Map<String, RefBookValue> refValue = refProviders.get(attribute.getAlias()).getRecordData(value.getReferenceValue());
                        uniqueValues.append(refValue.get(refAliases.get(attribute.getAlias())).toString());
                    }
                    break;
                default:
                    uniqueValues.append("undefined");
                    break;
            }
            if (i < values.size() - 1) {
                uniqueValues.append("/");
            }
        }
        return uniqueValues.toString();
    }

    @Override
    public void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_VERSION, tableName, SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

    @Override
    public void deleteVersion(String tableName, @NotNull Long uniqueRecordId) {
        getJdbcTemplate().update(String.format("delete from %s where id=?", tableName), uniqueRecordId);
    }
}