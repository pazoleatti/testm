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
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 18:48
 */
@Repository
public class RefBookDaoImpl extends AbstractDao implements RefBookDao {

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy");
        }
    };

	private static final Log LOG = LogFactory.getLog(RefBookDaoImpl.class);

    public static final String NOT_HIERARCHICAL_REF_BOOK_ERROR = "Справочник \"%s\" (id=%d) не является иерархичным";
    public static final String NOT_LINEAR_REF_BOOK_ERROR = "Справочник \"%s\" (id=%d) не является линейным";

    private static final String DELETE_VERSION = "update %s set status = -1 where %s";
    private static final String DELETE_VERSION_DELETE = "delete from %s where %s";
	private static final String STRING_VALUE_COLUMN_ALIAS = "string_value";
	private static final String NUMBER_VALUE_COLUMN_ALIAS = "number_value";
	private static final String DATE_VALUE_COLUMN_ALIAS = "date_value";
	private static final String REFERENCE_VALUE_COLUMN_ALIAS = "reference_value";

    private static final String FORM_LINK_MSG = "Существует экземпляр %sформы%s, который содержит ссылку на запись! Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s\"%s%s%s%s.";
    private static final String REF_BOOK_LINK_MSG = "Существует ссылка на запись справочника. Справочник \"%s\", запись: \"%s\"%s.";

	@Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BDUtils dbUtils;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    @Cacheable(value = "PermanentData", key = "'RefBook_'+#refBookId.toString()")
    public RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name from ref_book where id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    @Override
    public List<RefBook> getAll(Integer typeId) {
        return getAll(getJdbcTemplate().queryForList("SELECT id FROM ref_book WHERE (? IS NULL OR type = ?) ORDER BY NAME",
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
                        "SELECT id FROM ref_book WHERE visible = 1 AND (? IS NULL OR type = ?) ORDER BY NAME",
                        new Object[]{typeId, typeId},
                        Long.class));
    }

    @Override
    @Cacheable(value = "PermanentData", key = "'RefBook_attribute_'+#attributeId.toString()")
    public RefBook getByAttribute(Long attributeId) {
        try {
            return get(getJdbcTemplate().queryForLong(
                    "SELECT r.id FROM ref_book r JOIN ref_book_attribute a ON a.ref_book_id = r.id WHERE a.id = ?",
                    new Object[]{attributeId}, new int[]{Types.NUMERIC}));
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден атрибут справочника с id = %d", attributeId));
        }
    }

    @Override
    public RefBook getByRecord(@NotNull Long uniqueRecordId) {
        try {
            return get(getJdbcTemplate().queryForLong(
                    "SELECT b.id FROM ref_book b JOIN ref_book_record r ON r.ref_book_id = b.id WHERE r.id = ?",
                    new Object[]{uniqueRecordId}, new int[]{Types.NUMERIC}));
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдена запись справочника с id = %d", uniqueRecordId));
        }
    }

    /**
     * Настройка маппинга для справочника
     */
    private class RefBookRowMapper implements RowMapper<RefBook> {
        @Override
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
            result.setVersioned(rs.getBoolean("is_versioned"));
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

    @Override
    public List<RefBookAttribute> getAttributesByReferenceId(Long refBookId) {
        try {
            return getJdbcTemplate().query(
                    "select id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
                            "is_unique, sort_order, format, read_only, max_length " +
                            "from ref_book_attribute where reference_id = ?",
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
        @Override
        public RefBookAttribute mapRow(ResultSet rs, int index) throws SQLException {

            RefBookAttribute result = new RefBookAttribute();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setName(rs.getString("name"));
            result.setAlias(rs.getString("alias"));
            result.setAttributeType(RefBookAttributeType.values()[SqlUtils.getInteger(rs, "type") - 1]);
            result.setRefBookId(SqlUtils.getLong(rs, "reference_id"));
            result.setRefBookAttributeId(SqlUtils.getLong(rs, "attribute_id"));
            if (result.getRefBookAttributeId() !=  null) result.setRefBookAttribute(getAttribute(result.getRefBookAttributeId()));
            result.setVisible(rs.getBoolean("visible"));
            result.setPrecision(SqlUtils.getInteger(rs, "precision"));
            result.setWidth(SqlUtils.getInteger(rs, "width"));
            result.setRequired(rs.getBoolean("required"));
            result.setReadOnly(rs.getBoolean("read_only"));
            result.setUnique(rs.getInt("is_unique"));
            result.setSortOrder(SqlUtils.getInteger(rs, "sort_order"));
            result.setMaxLength(SqlUtils.getInteger(rs, "max_length"));
            Integer formatId = SqlUtils.getInteger(rs, "format");
            if (formatId != null) {
                result.setFormat(Formats.getById(formatId));
            }
            return result;
        }
    }

    private void appendSortClause(PreparedStatementData ps, RefBook refBook, RefBookAttribute sortAttribute, boolean isSortAscending, boolean isHierarchical, String prefix) {
        RefBookAttribute defaultSort = refBook.getSortAttribute();
        if (isSupportOver()) {
            ps.appendQuery("row_number() over ( order by ");
            if (sortAttribute != null || defaultSort != null) {
                String sortAlias = sortAttribute == null ? defaultSort.getAlias() : sortAttribute.getAlias();
                RefBookAttributeType sortType = sortAttribute == null ? defaultSort.getAttributeType() : sortAttribute.getAttributeType();

                if (isHierarchical) {
                    ps.appendQuery(prefix);
                    ps.appendQuery(sortAlias);
                } else {
                    ps.appendQuery("a");
                    ps.appendQuery(sortAlias);
                    ps.appendQuery(".");
                    ps.appendQuery(sortType.toString());
                    ps.appendQuery("_value ");
                }
            } else {
                ps.appendQuery(prefix);
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
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute, boolean isSortAscending, boolean calcHasChild, Long parentId) {
        PreparedStatementData ps = getRefBookSql(refBookId, null, null, version, sortAttribute, filter, pagingParams, isSortAscending, calcHasChild, parentId);
        RefBook refBookClone = SerializationUtils.clone(get(refBookId));
        if (version == null) {
            refBookClone.setAttributes(new ArrayList<RefBookAttribute>());
            refBookClone.getAttributes().addAll(get(refBookId).getAttributes());
            refBookClone.getAttributes().add(RefBook.getVersionFromAttribute());
            refBookClone.getAttributes().add(RefBook.getVersionToAttribute());
        }
        List<Map<String, RefBookValue>> records;

        if (calcHasChild) {
            records = getRecordsWithHasChild(ps, refBookClone);
        } else {
            records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBookClone));
        }
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справочнике
        PreparedStatementData psForCount = getRefBookSql(refBookId, null, null, version, sortAttribute, filter, null, true, false, null);
        psForCount.setQuery(new StringBuilder("SELECT count(*) FROM (" + psForCount.getQuery() + ")"));
        result.setTotalCount(getJdbcTemplate().queryForInt(psForCount.getQuery().toString(), psForCount.getParams().toArray()));
        return result;
    }

    @Override
    public List<Long> getDeletedRecords(Long refBookId, String filter) {
        PreparedStatementData ps = getRefBookSql2(refBookId, filter);
        RefBook refBookClone = SerializationUtils.clone(get(refBookId));
        List<Long> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong("record_id");
            }
        });
        return records;
    }

    @Override
    public Long getRowNum(Long refBookId, Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PreparedStatementData ps = getRefBookSql(refBookId, null, null, version, sortAttribute, filter, null, isSortAscending, false, null);
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
        ps.appendQuery("\n where\n  r.ref_book_id = ?");
        ps.addParam(refBookId);
        if (version != null && needAccurateVersion) {
            ps.appendQuery(" and  r.version = ?");
            ps.addParam(version);
        }
        ps.appendQuery(" and\n  status = 0\n");

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
    public Date getNextVersion(Long refBookId, Date version, String filter) {
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
        ps.appendQuery("select min(r.version) as version \n");

        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();

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
        ps.appendQuery(" and r.version > ?");
        ps.addParam(version);
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
            List<Date> versions = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                    new RowMapper<Date>() {
                        @Override
                        public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getDate("version");
                        }
                    });
            return versions.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int getRecordsCount(Long refBookId, Date version, String filter) {
        PreparedStatementData psForCount = getRefBookSql(refBookId, null, null, version, null, filter, null, true, false, null);
        psForCount.setQuery(new StringBuilder("SELECT count(*) FROM (" + psForCount.getQuery() + ")"));
        return getJdbcTemplate().queryForInt(psForCount.getQuery().toString(), psForCount.getParams().toArray());
    }

    private PagingResult<Map<String, RefBookValue>> getChildren(@NotNull Long refBookId, @NotNull Date version, PagingParams pagingParams,
                                                                String filter, RefBookAttribute sortAttribute, boolean isSortAscending, Long parentId) {
        PreparedStatementData ps = getChildrenStatement(refBookId, null, version, sortAttribute, filter, pagingParams, isSortAscending, parentId);
        RefBook refBook = get(refBookId);
        List<Map<String, RefBookValue>> records = getRecordsWithHasChild(ps, refBook);
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
        return getRecords(refBookId, version, pagingParams, filter, sortAttribute, true, false, null);
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
		Object value = parseRefBookValue(rs, columnName, attribute);
	    RefBookValue attrValue = record.get(attribute.getAlias());
        attrValue.setValue(value);
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
                    "WHERE %s AND r.ref_book_id = :refBookId";

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(Long refBookId, List<Long> recordIds) {
        final RefBook refBook = get(refBookId);
        final Map<Long, Map<String, RefBookValue>> resultMap = new HashMap<Long, Map<String, RefBookValue>>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", refBookId);
        String sql = String.format(SELECT_VALUES_BY_IDS_QUERY, SqlUtils.transformToSqlInStatement("r.id", recordIds));
        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
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
                    "  not exists (select 1 from ref_book_record r2 where r2.ref_book_id=r.ref_book_id and r2.record_id=r.record_id and r2.status != -1 and r2.version between r.version + interval '1' day and ?)\n" +
                    "group by\n" +
                    "  record_id)\n";

    private static final String RECORD_VERSIONS_ALL =
            "with recordsByVersion as (select r.ID, r.RECORD_ID, r.REF_BOOK_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r where r.ref_book_id = %d and r.status != -1), \n" +
                    "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=" + VersionedObjectStatus.NORMAL.getId() + ")\n";

    private static final String RECORD_VERSIONS_STATEMENT_BY_ID =
            "with currentRecord as (select id, record_id, version from REF_BOOK_RECORD where id=%d),\n" +
                    "recordsByVersion as (select r.ID, r.RECORD_ID, r.REF_BOOK_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r, currentRecord cr where r.REF_BOOK_ID=%d and r.RECORD_ID=cr.RECORD_ID and r.status != -1), \n" +
                    "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=" + VersionedObjectStatus.NORMAL.getId() + ")\n";

    private static final String RECORD_VERSIONS_STATEMENT_BY_RECORD_ID =
            "with recordsByVersion as (select r.ID, r.RECORD_ID, r.REF_BOOK_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r where r.record_id=%d and r.ref_book_id = %d and r.status != -1), \n" +
                    "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=" + VersionedObjectStatus.NORMAL.getId() + ")\n";

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
    private PreparedStatementData getRefBookSql(@NotNull Long refBookId, Long uniqueRecordId, Long recordId, Date version, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending, boolean calcHasChild, Long parentId) {
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
            if (uniqueRecordId != null){
                //Ищем все версии по уникальному идентификатору
                ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT_BY_ID, uniqueRecordId, refBookId));
            } else if (recordId != null){
                //Ищем все версии в группе версий
                ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT_BY_RECORD_ID, recordId, refBookId));
            } else {
                //Ищем вообще все версии
                ps.appendQuery(String.format(RECORD_VERSIONS_ALL, refBookId));
            }
        }

        ps.appendQuery(", res AS ");
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

        // Для видимых линейных справочников сортировка учитывает разыменование
        if (sortAttribute != null && sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE
                && !refBook.isHierarchic() && refBook.getTableName() == null) {
            appendReferenceSortClause(ps, sortAttribute, isSortAscending);
        } else {
            appendSortClause(ps, refBook, sortAttribute, isSortAscending, false, "frb.");
        }

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

        // left loin's для сортировки
        if (sortAttribute != null && sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE
                && refBook.getTableName() == null) {
            RefBookAttribute sortFinalAttribute = getAttribute(sortAttribute.getRefBookAttributeId());
            int index = 0;
            fromSql.append("left join ref_book_value srt" + index + " on srt" + index + ".record_id = a"
                    + sortAttribute.getAlias() + ".reference_value and srt" + index + ".attribute_id = "
                    + sortAttribute.getRefBookAttributeId());
            // Для вложенных зависимостей
            while (sortFinalAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                index++;
                fromSql.append("left join ref_book_value srt" + index + " on srt" + index + ".record_id = "
                        + sortFinalAttribute.getAlias() + ".reference_value and srt" + index + ".attribute_id = "
                        + sortFinalAttribute.getRefBookAttributeId());
                sortFinalAttribute = getAttribute(sortFinalAttribute.getRefBookAttributeId());
            }
        }

        ps.appendQuery(fromSql.toString());
        ps.appendQuery(" where\n  frb.ref_book_id = ");
        ps.appendQuery("?");
        ps.addParam(refBookId);
        ps.appendQuery(" and frb.status <> -1\n");

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

        if (version == null) {
            ps.appendQuery("order by t.version\n");
        }
        ps.appendQuery(")");

        ps.appendQuery("SELECT * from ("); //TODO: заменить "select *" на полное перечисление полей (Marat Fayzullin 30.01.2014)
        ps.appendQuery("SELECT res.* ");
        if (calcHasChild) {
            ps.appendQuery(", (SELECT 1 FROM dual WHERE EXISTS (SELECT 1 FROM res r1 ");
            ps.appendQuery(" WHERE r1." + RefBook.RECORD_PARENT_ID_ALIAS + " = res." + RefBook.RECORD_ID_ALIAS + ")) AS " + RefBook.RECORD_HAS_CHILD_ALIAS);
        }
        ps.appendQuery(" FROM res ");
        if (calcHasChild) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_PARENT_ID_ALIAS + (parentId == null ? " is null" : " = " + parentId.toString()));
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

    private static final String RECORD_VERSIONS_DELETED =
            "with t as (select r.ID, r.RECORD_ID, r.REF_BOOK_ID, r.VERSION, r.STATUS from REF_BOOK_RECORD r where r.ref_book_id = %d ) ";

    /**
     * Динамически формирует запрос для справочника
     *
     * @param refBookId       код справочника
     * @param filter          строка фильтрации
     * @return
     */
    private PreparedStatementData getRefBookSql2(@NotNull Long refBookId, String filter) {
        // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        RefBook refBook = get(refBookId);
        List<RefBookAttribute> attributes = refBook.getAttributes();

        PreparedStatementData filterPS = new PreparedStatementData();
        UniversalFilterTreeListener universalFilterTreeListener = applicationContext.getBean("universalFilterTreeListener", UniversalFilterTreeListener.class);
        universalFilterTreeListener.setRefBook(refBook);
        universalFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, universalFilterTreeListener);

        StringBuilder fromSql = new StringBuilder("\nfrom\n");

        fromSql.append("  ref_book_record frb join t on (frb.version = t.version and frb.record_id = t.record_id)\n");
        //Ищем вообще все версии
        ps.appendQuery(String.format(RECORD_VERSIONS_DELETED, refBookId));

        ps.appendQuery("SELECT ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        ps.appendQuery(" FROM ");
        ps.appendQuery("(select\n");
        ps.appendQuery(" frb.id as ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);

        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();

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
        ps.appendQuery(" and frb.status = -1\n");

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

        return ps;
    }

    private int appendReferenceSortClause(PreparedStatementData ps, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBookAttribute sortFinalAttribute = getAttribute(sortAttribute.getRefBookAttributeId());
        int index = 0;
        while (sortFinalAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
            index++;
            sortFinalAttribute = getAttribute(sortFinalAttribute.getRefBookAttributeId());
        }
        String postfix;
        switch (sortFinalAttribute.getAttributeType()) {
            case NUMBER:
                postfix = NUMBER_VALUE_COLUMN_ALIAS;
                break;
            case STRING:
                postfix = STRING_VALUE_COLUMN_ALIAS;
                break;
            case DATE:
                postfix = DATE_VALUE_COLUMN_ALIAS;
                break;
            default:
                throw new ServiceException("Ошибка подготовки условия сортировки. Непредусмотренный тип атрибута "
                        + sortFinalAttribute.getAttributeType().name() + "!");
        }

        String filterStrParam = "srt" + index + "." + postfix;
        ps.appendQuery("row_number() over (order by " + filterStrParam + " " + (isSortAscending ? "ASC" : "DESC") + ") as row_number_over");
        return index;
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
            ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT_BY_ID, uniqueRecordId, refBookId));
        }

        ps.appendQuery(", tc AS (select level as lvl, \n");
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
        ps.appendQuery(")\n");

        ps.appendQuery("SELECT ");

        appendSortClause(ps, refBook, sortAttribute, isSortAscending, true, "");
        ps.appendQuery(", res.* FROM (");

        ps.appendQuery("SELECT DISTINCT ");
        // выбираем все алиасы + row_number_over
        List<String> aliases = new ArrayList<String>(attributes.size() + 1);
        aliases.add(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attr : attributes) {
            aliases.add(attr.getAlias());
        }
        ps.appendQuery(StringUtils.join(aliases.toArray(), ','));
        ps.appendQuery(", (SELECT 1 FROM dual WHERE EXISTS (SELECT 1 FROM tc tc2 WHERE lvl > 1 AND tc2.record_id = tc.record_id)) as " + RefBook.RECORD_HAS_CHILD_ALIAS);
        ps.appendQuery(" FROM tc ");

        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
            ps.appendQuery(" BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(String.valueOf(pagingParams.getStartIndex() + pagingParams.getCount()));
        }
        ps.appendQuery(") res ");

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
        return (filter == null || filter.trim().isEmpty()) ? parentFilter : filter + " AND " + parentFilter;
    }

    public static boolean checkHierarchical(RefBook refBook) {
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
            return getRecords(refBookId, version, pagingParams, filter, sortAttribute, true, true, parentRecordId);
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
            if (records.isEmpty()) {
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

    private static final String CHECK_REF_BOOK_RECORD_UNIQUE_SQL = "select id from ref_book_record " +
            "where ref_book_id = ? and version = trunc(?, 'DD') and record_id = ? and status != -1";

    private void checkFillRequiredFields(Map<String, RefBookValue> record, @NotNull RefBook refBook) {
        List<RefBookAttribute> attributes = refBook.getAttributes();
        List<String> errors = RefBookUtils.checkFillRequiredRefBookAtributes(attributes, record);

        if (!errors.isEmpty()) {
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
            if (LOG.isDebugEnabled()) {
                LOG.trace(String.format("refBookId: %d; version: %s; rowId: %s", refBookId, version.toString(), rowId));
            }
            return getJdbcTemplate().queryForLong(CHECK_REF_BOOK_RECORD_UNIQUE_SQL,
                    new Object[]{refBookId, version, rowId},
                    new int[]{Types.BIGINT, Types.DATE, Types.BIGINT});
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

	/**
	 * Разыменовывает текущую строку resultSet в графе columnName используя информацию об атрибуте справочника attribute
	 * @param resultSet
	 * @param attribute
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	private Object parseRefBookValue(ResultSet resultSet, String columnName, RefBookAttribute attribute) throws SQLException {
		if (resultSet.getObject(columnName) != null) {
			switch (attribute.getAttributeType()) {
				case STRING: {
					return resultSet.getString(columnName);
				}
				case NUMBER: {
					return resultSet.getBigDecimal(columnName).setScale(attribute.getPrecision(), BigDecimal.ROUND_HALF_UP);
				}
				case DATE: {
					return resultSet.getDate(columnName);
				}
				case REFERENCE: {
					return SqlUtils.getLong(resultSet, columnName);
				}
			}
		}
		return null;
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
							String columnName = attribute.getAttributeType() + "_VALUE";
							Object value = parseRefBookValue(rs, columnName, attribute);
                            return new RefBookValue(attribute.getAttributeType(), value);
                        }
                    }
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private static final String GET_RECORD_VERSION = "with currentVersion as (select id, version, record_id, ref_book_id from ref_book_record where id = ?),\n" +
            "minNextVersion as (select r.ref_book_id, r.record_id, min(r.version) version from ref_book_record r, currentVersion cv where r.version > cv.version and r.record_id= cv.record_id and r.ref_book_id= cv.ref_book_id and r.status != -1 group by r.ref_book_id, r.record_id),\n" +
            "nextVersionEnd as (select mnv.ref_book_id, mnv.record_id, mnv.version, r.status from minNextVersion mnv, ref_book_record r where mnv.ref_book_id=r.ref_book_id and mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
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
                refBookId, uniqueRecordId);
    }

    @Override
    public int getRecordVersionsCountByRecordId(Long refBookId, Long recordId) {
        return getJdbcTemplate().queryForInt("select count(*) as cnt from REF_BOOK_RECORD " +
                        "where REF_BOOK_ID=? and STATUS=" + VersionedObjectStatus.NORMAL.getId() + " and RECORD_ID=?",
                refBookId, recordId);
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

    private final static String GET_FIRST_RECORD_ID = "with allRecords as (select id, version from ref_book_record where record_id = (select record_id from ref_book_record where id = ?) and ref_book_id = ? and id != ? and status not in (-1, 2))\n" +
            "select id from allRecords where version = (select min(version) from allRecords)";

    @Override
    public Long getFirstRecordId(Long refBookId, Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForObject(GET_FIRST_RECORD_ID,
                    new Object[]{uniqueRecordId, refBookId, uniqueRecordId},
                    Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        final Map<Long, Date> result = new HashMap<Long, Date>();
        getJdbcTemplate().query(String.format("select id, version from ref_book_record where %s",
                transformToSqlInStatement("id", uniqueRecordIds)), new RowCallbackHandler() {
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
        String sql = String.format("select distinct r.version from ref_book_value v, ref_book_record r where v.attribute_id = (select id from ref_book_attribute where ref_book_id=? and alias='%s') and %s and r.id=v.record_id and r.status != -1",
                RefBook.RECORD_PARENT_ID_ALIAS, transformToSqlInStatement("v.reference_value", uniqueRecordIds));
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

    private static final String CHECK_PARENT_CONFLICT = "with currentRecord as (select id, ref_book_id, record_id, version from ref_book_record where id = :parentId),\n" +
            "nextVersion as (select min(r.version) as version from ref_book_record r, currentRecord cr where r.version > cr.version and r.record_id=cr.record_id and r.ref_book_id=cr.ref_book_id and r.status != -1),\n" +
            "allRecords as (select cr.id, cr.version as versionStart, nv.version - interval '1' day as versionEnd from currentRecord cr, nextVersion nv)\n" +
            "select distinct id,\n" +
            "case\n" +
            "\twhen (versionEnd is not null and (:versionTo is null or :versionTo > versionEnd)) then 1\n" +
            "\twhen (:versionFrom < versionStart) then -1\n" +
            "\telse 0\n" +
            "end as result\n" +
            "from allRecords";

    @Override
    public List<Pair<Long, Integer>> checkParentConflict(Date versionFrom, List<RefBookRecord> records) {
        final Set<Pair<Long, Integer>> result = new HashSet<Pair<Long, Integer>>();
        for (RefBookRecord record : records) {
            Long parentId = record.getValues().get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
            if (parentId != null) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("parentId", parentId);
                params.put("versionFrom", versionFrom);
                params.put("versionTo", record.getVersionTo());
                getNamedParameterJdbcTemplate().query(CHECK_PARENT_CONFLICT, params, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        result.add(new Pair<Long, Integer>(SqlUtils.getLong(rs, "id"), SqlUtils.getInteger(rs, "result")));
                    }
                });
            }
        }
        return new ArrayList<Pair<Long, Integer>>(result);
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
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long refBookId, Long uniqueRecordId,
                                                                         PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PreparedStatementData ps = getRefBookSql(refBookId, uniqueRecordId, null, null, sortAttribute, filter, pagingParams, true, false, null);
        RefBook refBookClone = SerializationUtils.clone(get(refBookId));
        refBookClone.setAttributes(new ArrayList<RefBookAttribute>());
        refBookClone.getAttributes().addAll(get(refBookId).getAttributes());
        refBookClone.getAttributes().add(RefBook.getVersionFromAttribute());
        refBookClone.getAttributes().add(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBookClone));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справочнике
        result.setTotalCount(getRecordVersionsCount(refBookId, uniqueRecordId));
        return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long refBookId, Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PreparedStatementData ps = getRefBookSql(refBookId, null, recordId, null, sortAttribute, filter, pagingParams, true, false, null);
        RefBook refBookClone = SerializationUtils.clone(get(refBookId));
        refBookClone.setAttributes(new ArrayList<RefBookAttribute>());
        refBookClone.getAttributes().addAll(get(refBookId).getAttributes());
        refBookClone.getAttributes().add(RefBook.getVersionFromAttribute());
        refBookClone.getAttributes().add(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBookClone));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справочнике
        result.setTotalCount(getRecordVersionsCountByRecordId(refBookId, recordId));
        return result;
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(@NotNull Long refBookId, @NotNull Long recordId) {
        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groups = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();

        List<RefBookAttribute> attributes = getAttributes(refBookId);

        for (RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0) {

                List<Pair<RefBookAttribute, RefBookValue>> values = null;
                if (groups.get(attribute.getUnique()) != null) {
                    values = groups.get(attribute.getUnique());
                } else {
                    values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
                }

                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, getValue(recordId, attribute.getId())));
                groups.put(attribute.getUnique(), values);
            }
        }

        return groups;
    }

    private static final String CHECK_CROSS_VERSIONS = "with allVersions as (select r.* from ref_book_record r where status != -1 and ref_book_id=:refBookId and record_id=:recordId and (:excludedRecordId is null or id != :excludedRecordId)),\n" +
            "recordsByVersion as (select r.*, row_number() over(partition by r.record_id order by r.version) rn from ref_book_record r, allVersions av where r.id=av.id and r.status != -1),\n" +
            "versionInfo as (select rv.rn NUM, rv.ID, rv.VERSION, rv.status, rv2.version - interval '1' day nextVersion,rv2.status nextStatus from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn)\n" +
            "select num, id, version, status, nextversion, nextstatus, \n" +
            "case\n" +
            "  when (status=0 and (\n" +
            "  \t(:versionTo is null and (\n" +
            "  \t\t(nextversion is not null and nextversion >= :versionFrom) or \t\t-- 1, 6\n" +
            "\t\t(nextversion is null and version >= :versionFrom)\t\t\t\t\t-- 9, 10, 11, 12\n" +
            "  \t)) or (:versionTo is not null and (\n" +
            "  \t\t(version <= :versionFrom and nextversion is not null and nextversion >= :versionFrom) or \t\t-- 2, 3\n" +
            "  \t\t(version >= :versionFrom and version <= :versionTo)\t\t\t\t\t-- 4, 5\n" +
            "  \t))\n" +
            "  )) then 1\n" +
            "  when (status=0 and nextversion is null and version < :versionFrom) then 2\t\t\t\t\t\t\t--7, 8\n" +
            "  when (status=2 and (:versionTo is not null and version >= :versionFrom and version < :versionTo and nextversion is not null and nextversion > :versionTo)) then 3 \t\t-- 17\n" +
            "  when (status=2 and (\n" +
            "  \t(nextversion is not null and :versionTo is null and version > :versionFrom) or  \t-- 18\n" +
            "  \t(version = :versionFrom) or \n" +
            "  \t(nextversion is null and version >= :versionFrom and (:versionTo is null or :versionTo >= version))\t\t\t\t\t\t\t\t\t-- 21, 22\n" +
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

    @Override
    public List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(Long refBookId, Long uniqueRecordId,
                                                                        List<RefBookAttribute> attributes,
                                                                        List<RefBookRecord> records) {

        List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> recordsGroupsUniqueAttributesValues = aggregateUniqueAttributesAndValuesByRecords(attributes, records);

        if (recordsGroupsUniqueAttributesValues.isEmpty()) {
            return new ArrayList<Pair<Long, String>>();
        }

        // Параметры для case'ов
        List<Object> selectParams = new ArrayList<Object>();
        // Параметры для where
        List<Object> whereParams = new ArrayList<Object>();
        whereParams.add(refBookId);

        StringBuilder sqlCaseBuilder = new StringBuilder("CASE\n");
        StringBuilder sqlFromBuilder = new StringBuilder("FROM ref_book_record r\n");
        StringBuilder sqlWhereBuilder = new StringBuilder("WHERE r.status = 0 AND r.ref_book_id = ? AND\n");

        // Максимальное количество self-join таблиц
        int maxTablesCount = 0;
        // OR по каждой записи
        for (int i = 0; i < records.size(); i++) {
            sqlWhereBuilder.append("(");
            RefBookRecord record = records.get(i);
            Map<String, RefBookValue> recordValues = record.getValues();
            Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupsUniqueAttributesValues = recordsGroupsUniqueAttributesValues.get(i);
            // OR по группам уникальности
            for (Map.Entry<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupUniqueAttributesValues : groupsUniqueAttributesValues.entrySet()) {
                sqlCaseBuilder.append("WHEN ");
                sqlWhereBuilder.append("(");
                List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues = groupUniqueAttributesValues.getValue();
                StringBuilder clauseForGroup = new StringBuilder();
                StringBuilder attrNameBuilder = new StringBuilder();
                // AND по уникальным аттрибутам группы
                for (int j = 0; j < uniqueAttributesValues.size(); j++) {
                    // Нумерация self-join таблиц
                    int tableNumber = j + 1;
                    String valuesTableName = "v" + tableNumber;
                    String attributesTableName = "a" + tableNumber;
                    if (maxTablesCount < uniqueAttributesValues.size() && maxTablesCount < tableNumber) {
                        sqlFromBuilder.append("JOIN ref_book_value ").append(valuesTableName).append(" ON ").append(valuesTableName).append(".record_id = r.id\n");
                        sqlFromBuilder.append("JOIN ref_book_attribute ").append(attributesTableName).append(" ON ").append(attributesTableName).append(".id = ").append(valuesTableName).append(".attribute_id\n");
                    }

                    attrNameBuilder.append(attributesTableName).append(".name");
                    Pair<RefBookAttribute, RefBookValue> pair = uniqueAttributesValues.get(j);
                    RefBookAttribute attribute = pair.getFirst();
                    String type = attribute.getAttributeType().toString() + "_VALUE";
                    // Здесь проставляем номера таблиц
                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        clauseForGroup.append(valuesTableName).append(".attribute_id = ? AND (UPPER(").append(valuesTableName).append(".").append(type).append(") = UPPER(?) ");
                    } else {
                        clauseForGroup.append(valuesTableName).append(".attribute_id = ? AND (").append(valuesTableName).append(".").append(type).append(" = ? ");
                    }
                    // добавляем проверку на null для необязательных уникальных полей
                    if (!attribute.isRequired()) {
                        clauseForGroup.append("OR (").append(valuesTableName).append(".").append(type).append(" IS NULL AND ? IS NULL)");
                    }
                    clauseForGroup.append(")");

                    /*************************************Добавление параметров****************************************/
                    selectParams.add(attribute.getId());
                    whereParams.add(attribute.getId());

                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getStringValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getStringValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getReferenceValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getReferenceValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getNumberValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getNumberValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getDateValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getDateValue());
                    }
                    if (!attribute.isRequired()) {
                        selectParams.add(selectParams.get(selectParams.size() - 1));
                        whereParams.add(whereParams.get(whereParams.size() - 1));
                    }
                    /**************************************************************************************************/

                    if (j < uniqueAttributesValues.size() - 1) {
                        clauseForGroup.append(" AND ");
                        attrNameBuilder.append(" || ', ' || ");
                    } else {
                        sqlCaseBuilder.append(clauseForGroup);
                        clauseForGroup.append(" AND (? IS NULL OR r.record_id != ?)");
                        whereParams.add(record.getRecordId());
                        whereParams.add(record.getRecordId());
                        attrNameBuilder.append("\n");
                    }
                }

                sqlCaseBuilder.append("THEN ").append(attrNameBuilder);

                sqlWhereBuilder.append(clauseForGroup);
                sqlWhereBuilder.append(")");
                if (groupUniqueAttributesValues.getKey() < groupsUniqueAttributesValues.size()) {
                    sqlWhereBuilder.append(" OR ");
                }
                if (maxTablesCount < uniqueAttributesValues.size()) maxTablesCount = uniqueAttributesValues.size();
            }

            sqlWhereBuilder.append(")\n");
            if (i < records.size() - 1) sqlWhereBuilder.append(" OR ");
        }

        sqlCaseBuilder.append(" END AS name\n");

        if (uniqueRecordId != null) {
            sqlWhereBuilder.append(" AND v1.record_id != ?");
            whereParams.add(uniqueRecordId);
        }

        // Собираем куски в запрос
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT v1.record_id as id,\n");
        sqlBuilder.append(sqlCaseBuilder);
        sqlBuilder.append(sqlFromBuilder);
        sqlBuilder.append(sqlWhereBuilder);

        selectParams.addAll(whereParams);

        List<Pair<Long, String>> result = getJdbcTemplate().query(sqlBuilder.toString(), selectParams.toArray(), new RowMapper<Pair<Long, String>>() {
            @Override
            public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Pair<Long, String>(SqlUtils.getLong(rs, "id"), rs.getString("name"));
            }
        });

        return !result.isEmpty() ? aggregateUniqueAttributeNamesByRecords(result) : result;
    }

    @Override
    public List<String> getMatchedRecordsByUniqueAttributesIncome102(@NotNull List<RefBookAttribute> attributes,
                                                                     @NotNull List<Map<String, RefBookValue>> records,
                                                                     Integer accountPeriodId) {
        List<RefBookRecord> refBookRecords = new ArrayList<RefBookRecord>();
        for (Map<String, RefBookValue> map : records) {
            RefBookRecord record = new RefBookRecord();
            record.setValues(map);
            refBookRecords.add(record);
        }

        List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> recordsGroupsUniqueAttributesValues = aggregateUniqueAttributesAndValuesByRecords(attributes, refBookRecords);
        recordsGroupsUniqueAttributesValues.size();

        StringBuilder sql = new StringBuilder("SELECT opu_code FROM income_102 WHERE account_period_id = ? AND ");
        List<Object> params = new ArrayList<Object>();
        params.add(accountPeriodId);

        // OR по каждой записи
        for (int i = 0; i < records.size(); i++) {
            Map<String, RefBookValue> recordValues = records.get(i);
            Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupsUniqueAttributesValues = recordsGroupsUniqueAttributesValues.get(i);
            // OR по группам уникальности
            for (Map.Entry<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupUniqueAttributesValues : groupsUniqueAttributesValues.entrySet()) {
                sql.append("(");
                List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues = groupUniqueAttributesValues.getValue();
                // AND по уникальным аттрибутам группы
                for (int j = 0; j < uniqueAttributesValues.size(); j++) {
                    Pair<RefBookAttribute, RefBookValue> pair = uniqueAttributesValues.get(j);
                    RefBookAttribute attribute = pair.getFirst();

                    /*************************************Добавление параметров****************************************/
                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        sql.append("upper(").append(attribute.getAlias()).append(") = upper(?) ");
                        params.add(recordValues.get(attribute.getAlias()).getStringValue());
                    } else {
                        sql.append(attribute.getAlias()).append(" = ? ");
                        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                            params.add(recordValues.get(attribute.getAlias()).getReferenceValue());
                        }
                        if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                            params.add(recordValues.get(attribute.getAlias()).getNumberValue());
                        }
                        if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                            params.add(recordValues.get(attribute.getAlias()).getDateValue());
                        }
                    }
                    /**************************************************************************************************/

                    if (j < uniqueAttributesValues.size() - 1) {
                        sql.append(" AND ");
                    }
                }
                if (groupUniqueAttributesValues.getKey() < groupsUniqueAttributesValues.size()) {
                    sql.append(") OR ");
                } else {
                    sql.append(")");
                }
            }
            if (i < records.size() - 1) sql.append(" OR ");
        }

        return getJdbcTemplate().queryForList(sql.toString(), params.toArray(), String.class);
    }

    /**
     * Агрегировать названия уникальных аттрибутов по записям
     *
     * @param result список пар идентификатор записи - название уникального атрибута
     * @return список пар идентификатор записи - названия уникальных атрибутов через запятую
     */
    List<Pair<Long, String>> aggregateUniqueAttributeNamesByRecords(List<Pair<Long, String>> result) {
        List<Pair<Long, String>> matchedRecords = new ArrayList<Pair<Long, String>>();
        Long prevRecordId = 0L;
        String prevName = "";
        for (Pair<Long, String> pair : result) {

            if (!prevRecordId.equals(pair.getFirst()) && prevRecordId != 0) {
                Pair<Long, String> newPair = new Pair<Long, String>(prevRecordId, prevName);
                matchedRecords.add(newPair);
            }

            if (prevRecordId.equals(pair.getFirst())) {
                prevName = prevName + ", " + pair.getSecond();
            } else {
                prevName = pair.getSecond();
            }

            prevRecordId = pair.getFirst();
        }

        Pair<Long, String> newPair = new Pair<Long, String>(prevRecordId, prevName);
        matchedRecords.add(newPair);
        return matchedRecords;
    }

    /**
     * Агрегировать список пар уникальный атрибут - его значение по группам уникальности и по записям
     *
     * @param attributes атрибуты справочника
     * @param records    записи
     * @return список записей с группами уникальности списков пар уникальных атрибутов и значений
     */
    List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> aggregateUniqueAttributesAndValuesByRecords(List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        // Формируем список для каждой записи из групп уникальных атрибутов с их значениями
        List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> listAttributeValues = new ArrayList<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>>();

        // для каждой записи
        for (RefBookRecord record : records) {
            Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> attributeValues = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();
            // для каждого атрибута
            for (RefBookAttribute attribute : attributes) {
                // если уникальный
                if (attribute.getUnique() != 0) {
                    List<Pair<RefBookAttribute, RefBookValue>> values;
                    if (attributeValues.get(attribute.getUnique()) != null) {
                        values = attributeValues.get(attribute.getUnique());
                    } else {
                        values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
                    }

                    values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, record.getValues().get(attribute.getAlias())));
                    attributeValues.put(attribute.getUnique(), values);
                }
            }
            if (!attributeValues.isEmpty()) {
                listAttributeValues.add(attributeValues);
            }
        }
        return listAttributeValues;
    }

    private final static String CHECK_CONFLICT_VALUES_VERSIONS = "with conflictRecord as (select * from REF_BOOK_RECORD where %s),\n" +
            "allRecordsInConflictGroup as (select r.* from REF_BOOK_RECORD r where exists (select 1 from conflictRecord cr where r.REF_BOOK_ID=cr.REF_BOOK_ID and r.RECORD_ID=cr.RECORD_ID and r.status != -1)),\n" +
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
                transformToSqlInStatement("ID", recordIds));
        Map<String, Date> params = new HashMap<String, Date>();
        params.put("versionFrom", versionFrom);
        params.put("versionTo", versionTo);
        return getNamedParameterJdbcTemplate().queryForList(sql, params, Long.class);
    }

    @Override
    public List<Pair<Date, Date>> isVersionUsedLikeParent(Long refBookId, Long recordId, Date versionFrom) {
        return getJdbcTemplate().query("select r.version as version, \n" +
                        "  (SELECT\n" +
                        "  min(version) - interval '1' DAY FROM ref_book_record rn WHERE rn.ref_book_id = r.ref_book_id AND rn.record_id = r.record_id AND rn.version > r.version) AS versionEnd\n" +
                        "from ref_book_record r, ref_book_value v " +
                        "where r.id=v.record_id and v.attribute_id in (select id from ref_book_attribute where reference_id=?) " +
                        "and r.version >= ? and v.REFERENCE_VALUE=? and r.status != -1", new RowMapper<Pair<Date, Date>>() {
                    @Override
                    public Pair<Date, Date> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Date, Date>(rs.getDate("version"), rs.getDate("versionEnd"));
                    }
                },
                refBookId, versionFrom, recordId);
    }

    private static final String CHECK_USAGES_IN_DEPARTMENT_CONFIG = "select * from (with checkRecords as (select * from ref_book_record r where %s),\n" +
            "periodCodes as (select a.alias, v.* from ref_book_value v, ref_book_attribute a where v.attribute_id=a.id and a.ref_book_id=8),\n" +
            "usages as (select r.* from ref_book_value v, ref_book_record r, checkRecords cr " +
            "where v.attribute_id in (select id from ref_book_attribute where ref_book_id in (37,310,31,98,330,33,206,99) %s and alias != 'DEPARTMENT_ID') and v.reference_value = cr.id and r.id=v.record_id and r.status != -1),\n" +
            "allVersions as (select distinct r.* from ref_book_record r, usages u where r.status != -1 and r.ref_book_id=u.ref_book_id and r.record_id=u.record_id),\n" +
            "recordsByVersion as (select r.*, row_number() over(partition by r.record_id order by r.version) rn from ref_book_record r, allVersions av where r.id=av.id and r.status != -1),\n" +
            "versionInfo as (select rv.rn NUM, rv.ID, rv.ref_book_id, rv.VERSION, rv.status, rv2.version - interval '1' day nextVersion,rv2.status nextStatus from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn join usages u on u.id = rv.id  where rv.status = 0)\n" +
            "select distinct d.name as departmentName, concat(pn.string_value, to_char(u.version,' yyyy')) as periodName, nt.number_value as isT, ni.number_value as isI, nd.number_value as isD, nv.number_value as isV, np.number_value as isP,\n" +
            "trunc(u.version, 'DD') as periodStart, trunc(u.nextversion, 'DD') as periodEnd,\n" +
            "case\n" +
            "\twhen (u.ref_book_id = 31 or u.ref_book_id = 310) then 'T'\n" +           //Транспортный налог
            "\twhen (u.ref_book_id = 33 or u.ref_book_id = 330) then 'I'\n" +           //Налог на прибыль
            "\twhen u.ref_book_id = 37 then 'D'\n" +                                    //Учет контролируемых сделок
            "\twhen u.ref_book_id = 98 then 'V'\n" +                                    //НДС
            "\twhen (u.ref_book_id = 99 or u.ref_book_id = 206) then 'P'\n" +           //Налог на имущество
            "\telse null\n" +
            "end as taxCode\n" +
            "from versionInfo u\n" +
            "join ref_book_value dv on dv.record_id = u.id\n" +
            "join ref_book_attribute da on (da.id = dv.attribute_id and da.alias='DEPARTMENT_ID')\n" +
            "join department d on d.id = dv.reference_value\n" +
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
    public List<String> isVersionUsedInDepartmentConfigs(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                                         Boolean restrictPeriod, List<Long> excludedRefBooks) {
        Set<String> results = new HashSet<String>();
        Map<String, Object> params = new HashMap<String, Object>();

        try {
            //Проверка использования в настройках подразделений
            String in = transformToSqlInStatement("r.id", uniqueRecordIds);
            String inExcludedRefBook = "";
            if (excludedRefBooks != null && !excludedRefBooks.isEmpty()) {
                inExcludedRefBook = " and " + transformToSqlInStatement("ref_book_id not ", excludedRefBooks);
            }
            String sql = String.format(CHECK_USAGES_IN_DEPARTMENT_CONFIG, in, inExcludedRefBook);
            params.clear();
            if (restrictPeriod != null) {
                if (restrictPeriod) {
                    //Отбираем только ссылки пересекающиеся с указанным периодом
                    sql  += " and ((periodStart <= :versionFrom and (periodEnd is null or periodEnd > :versionFrom)) or (periodStart > :versionFrom and (:versionTo is null or periodStart <= :versionTo)))";
                    params.put("versionFrom", versionFrom);
                    params.put("versionTo", versionTo);
                } else {
                    //Отбираем только ссылки НЕ попадающие в указанный период
                    sql  += " and ((:versionTo is not null and :versionTo < periodStart) or " +
                            "(periodEnd is not null and periodEnd < :versionFrom))";
                    params.put("versionFrom", versionFrom);
                    params.put("versionTo", versionTo);
                }
            }
            results.addAll(getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    StringBuilder result = new StringBuilder();
                    result.append("В настройке подразделения \"");
                    result.append(rs.getString("departmentName")).append("\" для налога \"");
                    result.append(TaxTypeCase.fromCode(rs.getString("taxCode").charAt(0)).getNominative()).append("\" в периоде \"");
                    result.append(rs.getString("periodName")).append("\" указана ссылка на версию!");
                    return result.toString();
                }
            }));
            return new ArrayList<String>(results);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<String>(0);
        } catch (DataAccessException e) {
            LOG.error("Проверка использования в настройках подразделений", e);
            throw new DaoException("Проверка использования в настройках подразделений", e);
        }
    }

    private static final String CHECK_USAGES_IN_FORMS = "with forms as (\n" +
            "  select fd.*, drp.report_period_id as report_period_id, drp.department_id as department_id, drp.correction_date as correctionDate  from form_data fd \n" +
            "  join department_report_period drp on drp.id = fd.department_report_period_id \n" +
            "  join form_data_ref_book fdrf on fdrf.form_data_id = fd.id and fdrf.ref_book_id = :refBookId\n" +
            "  where %s\n" +
            ")" +
            "select distinct f.id as formDataId, f.state, t.id as  formTypeId, t.tax_type, f.kind as formKind, t.name as formType, d.path as departmentPath, d.type as departmentType, rp.name as reportPeriodName, tp.year as year, f.period_order as month, f.correctionDate as correctionDate from forms f \n" +
            "join (select d.id, d.type, substr(sys_connect_by_path(name,'/'), 2) as path \n" +
            "\t\tfrom department d \n" +
            "\t\twhere d.id in (select department_id from forms) \n" +
            "\t\tstart with d.id = 0 \n" +
            "\t\tconnect by prior d.id = d.parent_id) d on d.id=f.department_id \n" +
            "join form_template ft on ft.id = f.form_template_id \n" +
            "join form_type t on t.id = ft.type_id \n" +
            "join report_period rp on rp.id = f.report_period_id\n" +
            "join tax_period tp on tp.id = rp.tax_period_id";

    @Override
    public List<FormLink> isVersionUsedInForms(final Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                                            Boolean restrictPeriod) {
        Set<FormLink> results = new HashSet<FormLink>();
        Map<String, Object> params = new HashMap<String, Object>();

        try {
            String in = transformToSqlInStatement("fdrf.record_id", uniqueRecordIds);
            String sql = String.format(CHECK_USAGES_IN_FORMS, in);
            params.clear();
            params.put("refBookId", refBookId);
            if (restrictPeriod != null) {
                if (restrictPeriod) {
                    //Отбираем только ссылки пересекающиеся с указанным периодом
                    sql  += " WHERE ((rp.calendar_start_date <= :versionFrom and (rp.end_date is null or rp.end_date > :versionFrom)) or (rp.calendar_start_date > :versionFrom and (:versionTo is null or rp.calendar_start_date <= :versionTo)))";
                    params.put("versionFrom", versionFrom);
                    params.put("versionTo", versionTo);
                } else {
                    //Отбираем только ссылки НЕ попадающие в указанный период
                    sql  += " WHERE ((:versionTo is not null and :versionTo < rp.calendar_start_date) or " +
                            "(rp.end_date is not null and rp.end_date < :versionFrom))";
                    params.put("versionFrom", versionFrom);
                    params.put("versionTo", versionTo);
                }
            }
            results.addAll(getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<FormLink>() {
                @Override
                public FormLink mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Integer month = SqlUtils.getInteger(rs, "month");
                    Date correctionDate = rs.getDate("correctionDate");
                    FormLink formLink = new FormLink();
                    formLink.setFormDataId(SqlUtils.getLong(rs, "formDataId"));
                    formLink.setFormTypeId(SqlUtils.getInteger(rs, "formTypeId"));
                    formLink.setState(WorkflowState.fromId(SqlUtils.getInteger(rs, "state")));
                    char taxType = rs.getString("tax_type").charAt(0);
                    TaxType formTaxType = TaxType.fromCode(taxType);
                    formLink.setMsg(String.format(
                            FORM_LINK_MSG,
                            (formTaxType != null && formTaxType.isTax()) ? "налоговой " : " ",
                            (refBookId == RefBook.TCO && formLink.getState() != WorkflowState.CREATED) ? " в статусе отличном от \"Создана\"" : "",
                            FormDataKind.fromId(SqlUtils.getInteger(rs, "formKind")).getTitle(),
                            rs.getString("formType"),
                            (SqlUtils.getInteger(rs, "departmentType") != 1) ?
                                    rs.getString("departmentPath").substring(rs.getString("departmentPath").indexOf("/") + 1) :
                                    rs.getString("departmentPath"),
                            rs.getString("reportPeriodName") + " " + rs.getString("year"),
                            "", //TODO: позже добавить информацию по формам ЭНС
                            month != null ? "Месяц: \"" + Formats.getRussianMonthNameWithTier(month) + "\"" : "",
                            correctionDate != null ? "Дата сдачи корректировки: \"" + SDF_DD_MM_YYYY.get().format(correctionDate) + "\"" : "",
                            "" //TODO: позже добавить информацию по расчету нарастающим итогом
                    ));
                    return  formLink;
                }
            }));
            return new ArrayList<FormLink>(results);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<FormLink>(0);
        } catch (DataAccessException e) {
            LOG.error("Проверка использования в налоговых формах", e);
            throw new DaoException("Проверка использования в налоовых формах", e);
        }
    }

    private static final String CHECK_USAGES_IN_REFBOOK =
            "SELECT r.id, b.name AS refbookName, b.is_versioned as versioned, r.version AS versionStart, uv.string_value, uv.number_value, uv.date_value, uv.reference_value,\n" +
                    "  (SELECT\n" +
                    "  min(version) - interval '1' DAY FROM ref_book_record rn WHERE rn.ref_book_id = r.ref_book_id AND rn.record_id = r.record_id AND rn.version > r.version and rn.status != -1) AS versionEnd\n" +
                    "FROM ref_book_record r\n" +
                    "  JOIN ref_book b on b.id = r.REF_BOOK_ID\n" +
                    "  JOIN ref_book_value v on v.RECORD_ID = r.id\n" +
                    "  JOIN ref_book_attribute a on a.id = v.ATTRIBUTE_ID\n" +
                    "  JOIN ref_book_value uv on uv.RECORD_ID = r.id \n" +
                    "  JOIN ref_book_attribute ua on (ua.id = uv.ATTRIBUTE_ID and ua.is_unique > 0)\n" +
                    "WHERE %s and a.reference_id = :refBookId and r.status != -1 and b.id not in (37,310,31,98,330,33,206,99) %s";

    public List<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                                Boolean restrictPeriod, List<Long> excludedRefBooks) {

        String in = transformToSqlInStatement("v.reference_value", uniqueRecordIds);
        String inExcludedRefBook = "";
        if (excludedRefBooks != null && !excludedRefBooks.isEmpty()) {
            inExcludedRefBook = " and " + transformToSqlInStatement("b.id not ", excludedRefBooks);
        }
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();

        String fullSql;
        //Без ограничений по периоду
        sql = String.format(CHECK_USAGES_IN_REFBOOK, in, inExcludedRefBook);
        try {
            if (restrictPeriod == null) {
                params.put("refBookId", refBookId);
                fullSql = sql;
            } else if (restrictPeriod) {
                //Отбираем только ссылки пересекающиеся с указанным периодом
                String restrictQuery = " where ((versionStart <= :versionFrom and (versionEnd is null or versionEnd > :versionFrom)) or (versionStart > :versionFrom and (:versionTo is null or versionStart <= :versionTo)))";
                fullSql = "SELECT * FROM (\n" + sql + "\n ) " + restrictQuery;
                params.put("refBookId", refBookId);
                params.put("versionFrom", versionFrom);
                params.put("versionTo", versionTo);
            } else {
                //Отбираем только ссылки НЕ попадающие в указанный период
                String restrictQuery = " where ((:versionTo is not null and :versionTo < versionStart) or " +
                        "(versionEnd is not null and versionEnd < :versionFrom))";
                fullSql = "SELECT * FROM (\n" + sql + "\n ) " + restrictQuery;
                params.put("refBookId", refBookId);
                params.put("versionFrom", versionFrom);
                params.put("versionTo", versionTo);
            }

            //Формируем список значений уникальных атрибутов + основных параметров
            final Map<Long, RecordTemp> records = new HashMap<Long, RecordTemp>();
            getNamedParameterJdbcTemplate().query(fullSql, params, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    Long uniqueRecordId = SqlUtils.getLong(rs, "id");
                    RecordTemp attributeValues = records.get(uniqueRecordId);

                    //Если запись еще не встречалась, то заполняем основные параметры
                    if (attributeValues == null) {
                        attributeValues = new RecordTemp();
                        attributeValues.setRefbookName(rs.getString("refbookName"));
                        attributeValues.setRefbookVersioned(rs.getBoolean("versioned"));
                        attributeValues.setVersionStart(rs.getDate("versionStart"));
                        attributeValues.setVersionEnd(rs.getDate("versionEnd"));
                        records.put(uniqueRecordId, attributeValues);
                    }
                    //Заполняем значения уникальных атрибутов
                    attributeValues.setUniqueAttributes(concatAttrs(rs, attributeValues.getUniqueAttributes()));
                }

                public String concatAttrs(ResultSet rs, String attrValues) throws SQLException {
                    // TODO возможно тут стоит предусмотреть объединение по группе уникальности
                    StringBuilder attr = new StringBuilder();
                    boolean hasValue = rs.getString(STRING_VALUE_COLUMN_ALIAS) != null ||
                            rs.getString(NUMBER_VALUE_COLUMN_ALIAS) != null ||
                            rs.getDate(DATE_VALUE_COLUMN_ALIAS) != null;
                    if (attrValues != null) {
                        attr.append(attrValues);
                        if (hasValue) {
                            attr.append(", ");
                        }
                    }
                    attr.append(rs.getString(STRING_VALUE_COLUMN_ALIAS) != null ? rs.getString(STRING_VALUE_COLUMN_ALIAS): "");
                    attr.append(rs.getString(NUMBER_VALUE_COLUMN_ALIAS) != null ? rs.getLong(NUMBER_VALUE_COLUMN_ALIAS) : "");
                    attr.append(rs.getDate(DATE_VALUE_COLUMN_ALIAS) != null ? rs.getDate(DATE_VALUE_COLUMN_ALIAS) : "");
                    // TODO - разыменовать и добавить значение аттрибута ссылки
                    attr.append(rs.getString(REFERENCE_VALUE_COLUMN_ALIAS) != null ? rs.getInt(REFERENCE_VALUE_COLUMN_ALIAS) + ", " : "");
                    return attr.toString();
                }
            });

            //Формируем сообщения для списка записей имеющих ссылку на указанную версию
            Set<String> results = new HashSet<String>();
            for (RecordTemp attributes : records.values()) {
                results.add(String.format(REF_BOOK_LINK_MSG,
                        attributes.getRefbookName(),
                        attributes.getUniqueAttributes(),
                        attributes.isRefbookVersioned() ?
                                ", действует с " + SDF_DD_MM_YYYY.get().format(attributes.getVersionStart()) +
                                        " по " + (attributes.getVersionEnd() != null ? SDF_DD_MM_YYYY.get().format(attributes.getVersionEnd()) : "-")
                                : ""
                ));
            }
            return new ArrayList<String>(results);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<String>(0);
        } catch (DataAccessException e) {
            LOG.error("Проверка использования в справочниках", e);
            throw new DaoException("Проверка использования в справочниках", e);
        }
    }

    /**
     * Временная сущность для формирования сообщений
     */
    private class RecordTemp {
        /** Название справочника, к которому относится запись, в которой есть ссылка на искомую версию */
        private String refbookName;
        /** Является ли справочник со ссылкой версионируемым? */
        private boolean refbookVersioned;
        /** Список значений уникальных атриутов записи */
        private String uniqueAttributes;
        /** Дата начала действия записи */
        private Date versionStart;
        /** Дата окончания действия записи */
        private Date versionEnd;

        public String getRefbookName() {
            return refbookName;
        }

        public void setRefbookName(String refbookName) {
            this.refbookName = refbookName;
        }

        public boolean isRefbookVersioned() {
            return refbookVersioned;
        }

        public void setRefbookVersioned(boolean refbookVersioned) {
            this.refbookVersioned = refbookVersioned;
        }

        public String getUniqueAttributes() {
            return uniqueAttributes;
        }

        public void setUniqueAttributes(String uniqueAttributes) {
            this.uniqueAttributes = uniqueAttributes;
        }

        public Date getVersionStart() {
            return versionStart;
        }

        public void setVersionStart(Date versionStart) {
            this.versionStart = versionStart;
        }

        public Date getVersionEnd() {
            return versionEnd;
        }

        public void setVersionEnd(Date versionEnd) {
            this.versionEnd = versionEnd;
        }
    }

    @Override
    public List<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds) {
        return isVersionUsedInRefBooks(refBookId, uniqueRecordIds, null, null, true,
                RefBook.WithTable.getTablesIdByRefBook(refBookId) == null ?
                        Collections.<Long>emptyList() :
                        Arrays.asList(RefBook.WithTable.getTablesIdByRefBook(refBookId)));
    }

    private static final String GET_NEXT_RECORD_VERSION = "with nextVersion as (select r.* from ref_book_record r where r.ref_book_id = ? and r.record_id = ? and r.status != -1 and r.version  = \n" +
            "\t(select min(version) from ref_book_record where ref_book_id=r.ref_book_id and record_id=r.record_id and status=0 and version > ?)),\n" +
            "minNextVersion as (select r.ref_book_id, r.record_id, min(r.version) version from ref_book_record r, nextVersion nv where r.version > nv.version and r.record_id= nv.record_id and r.ref_book_id= nv.ref_book_id and r.status != -1 group by r.ref_book_id, r.record_id),\n" +
            "nextVersionEnd as (select mnv.ref_book_id, mnv.record_id, mnv.version, r.status from minNextVersion mnv, ref_book_record r where mnv.ref_book_id=r.ref_book_id and mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
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

    private static final String GET_PREVIOUS_RECORD_VERSION = "with previousVersion as (select r.* from ref_book_record r where r.ref_book_id = ? and r.record_id = ? and r.status = 0 and r.version  = \n" +
            "\t(select max(version) from ref_book_record where ref_book_id=r.ref_book_id and record_id=r.record_id and status=0 and version < ?)),\n" +
            "minNextVersion as (select r.ref_book_id, r.record_id, min(r.version) version from ref_book_record r, previousVersion pv where r.version > pv.version and r.record_id= pv.record_id and r.ref_book_id= pv.ref_book_id and r.status != -1 group by r.ref_book_id, r.record_id),\n" +
            "nextVersionEnd as (select mnv.ref_book_id, mnv.record_id, mnv.version, r.status from minNextVersion mnv, ref_book_record r where mnv.ref_book_id=r.ref_book_id and mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
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
        try {
            return getJdbcTemplate().queryForLong("select id from ref_book_record where ref_book_id = ? and record_id = ? and version = ? and status != -1", refBookId, recordId, version);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String DELETE_ALL_VERSIONS = "update ref_book_record set status = -1 where ref_book_id=? and record_id in (select record_id from ref_book_record where %s)";

    @Override
    public void deleteAllRecordVersions(Long refBookId, List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_ALL_VERSIONS, transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql, refBookId);
    }

    private static final String GET_RELATED_VERSIONS = "with currentRecord as (select id, record_id, ref_book_id from REF_BOOK_RECORD where %s),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, STATUS, VERSION, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r, currentRecord cr where r.ref_book_id=cr.ref_book_id and r.record_id=cr.record_id and r.status != -1) \n" +
            "select rv2.ID from currentRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where cr.id=rv.id and rv2.status=%d";

    @Override
    public List<Long> getRelatedVersions(List<Long> uniqueRecordIds) {
        try {
            String sql = String.format(GET_RELATED_VERSIONS,
                    transformToSqlInStatement("id", uniqueRecordIds), VersionedObjectStatus.FAKE.getId());
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
        if (records.isEmpty()) {
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
        jt.batchUpdate(String.format(INSERT_REF_BOOK_RECORD_SQL_OLD, refBookId, sdf.get().format(version)), batchRefBookRecordsPS);

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
            if (!errors.isEmpty()) {
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
            if (records.isEmpty()) {
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
                jt.batchUpdate(String.format(UPDATE_REF_BOOK_RECORD_SQL_OLD, refBookId, sdf.get().format(version)), recordAddIds);
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
        if (recordIds.isEmpty()) {
            return;
        }
        List<Object[]> insertValues = new ArrayList<Object[]>();
        List<Object[]> deleteValues = new ArrayList<Object[]>();
        for (Long id : recordIds) {
            Long rowId = getRowId(id);
            Long recordId = checkRecordUnique(refBookId, version, rowId);
            if (recordId == null) {
                insertValues.add(new Object[]{rowId});
            } else {
                deleteValues.add(new Object[]{id});
            }
        }
        JdbcTemplate jt = getJdbcTemplate();
        if (!insertValues.isEmpty()) {
            jt.batchUpdate(String.format(DELETE_REF_BOOK_RECORD_SQL_I_OLD, refBookId, sdf.get().format(version)), insertValues);
        }
        if (!deleteValues.isEmpty()) {
            jt.batchUpdate(String.format(DELETE_REF_BOOK_RECORD_SQL_D_OLD, refBookId, sdf.get().format(version)), deleteValues);
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
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
        }
        if (whereClause != null && !whereClause.trim().isEmpty()) {
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

        ps.appendQuery("WITH tc AS (SELECT level as lvl, ");
        ps.appendQuery(" CONNECT_BY_ROOT frb.ID as \"RECORD_ID\" ");
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
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
            if (parentId == null) {
                ps.appendQuery(")");
            }
        }

        ps.appendQuery(" CONNECT BY NOCYCLE PRIOR frb.id = frb.PARENT_ID ");
        ps.appendQuery(" START WITH ");
        ps.appendQuery(parentId == null ? " frb.PARENT_ID is null " : " frb.PARENT_ID = " + parentId);

        ps.appendQuery(")\n");

        ps.appendQuery("SELECT res.*, ");
        appendSortClause(ps, refBook, sortAttribute, isSortAscending, "");
        ps.appendQuery(" FROM (");
        ps.appendQuery("SELECT DISTINCT ");
        ps.appendQuery("RECORD_ID ");

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(", (SELECT 1 FROM dual WHERE EXISTS (SELECT 1 FROM tc tc2 WHERE lvl > 1 AND tc2.record_id = tc.record_id)) AS " + RefBook.RECORD_HAS_CHILD_ALIAS);
        ps.appendQuery(" FROM tc ");
        ps.appendQuery(") res ");


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
        ps.appendQuery(" frb");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
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
        List<Map<String, RefBookValue>> records = getRecordsWithHasChild(ps, refBook);

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
        if (!ps.getParams().isEmpty()) {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        } else {
            return getJdbcTemplate().query(ps.getQuery().toString(), new RefBookValueMapper(refBook));
        }
    }

    /**
     * Возвращает элементы справочника с вычисленным столбцом has_child
     *
     * @param ps
     * @param refBook
     * @return
     */
    @Override
    public List<Map<String, RefBookValue>> getRecordsWithHasChild(PreparedStatementData ps, RefBook refBook) {
        return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook){
            @Override
            public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
                Map<String, RefBookValue> result = super.mapRow(rs, index);
                result.put(RefBook.RECORD_HAS_CHILD_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, SqlUtils.getLong(rs, RefBook.RECORD_HAS_CHILD_ALIAS)));
                return result;
            }
        });
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
        if (!ps.getParams().isEmpty()) {
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

    // TODO Левыкин: можно вынести в сервис
    @Override
    public String buildUniqueRecordName(RefBook refBook, Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupValues) {
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

        for (Map.Entry<Integer, List<Pair<RefBookAttribute, RefBookValue>>> entry : groupValues.entrySet()) {
            List<Pair<RefBookAttribute, RefBookValue>> values = entry.getValue();
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
        }
        return uniqueValues.toString();
    }

    @Override
    public void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds, boolean isDelete) {
        String sql = String.format(isDelete?DELETE_VERSION_DELETE:DELETE_VERSION, tableName, transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

	private class DereferenceMapper implements RowCallbackHandler {
		private RefBookAttribute attribute;
		private Map<Long, RefBookValue> result;

		public DereferenceMapper(RefBookAttribute attribute) {
			this.attribute = attribute;
			result = new HashMap<Long, RefBookValue>(); // все атрибуты не нужны, только справочные и ссылочные
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			Long recordId = rs.getLong("record_id");
			Object value = parseRefBookValue(rs, "value", attribute);
			result.put(recordId, new RefBookValue(attribute.getAttributeType(), value));
		}

		public Map<Long, RefBookValue> getResult() {
			return result;
		}
	}

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		if (recordIds.isEmpty()) {
            return new HashMap<Long, RefBookValue>();
		}
		RefBook refBook = getByAttribute(attributeId);
 		final RefBookAttribute attribute = refBook.getAttribute(attributeId);
		String sql = String.format(
			"SELECT record_id, %s value FROM ref_book_value WHERE attribute_id = %s AND %s",
			attribute.getAttributeType() + "_value",
			attributeId,
			transformToSqlInStatement("record_id", recordIds));

		DereferenceMapper mapper = new DereferenceMapper(attribute);
		getJdbcTemplate().query(sql, mapper);
		return mapper.getResult();
	}

	@Override
	public Map<Long, RefBookValue> dereferenceValues(String tableName, Long attributeId, Collection<Long> recordIds) {
		if (recordIds.isEmpty()) {
			return new HashMap<Long, RefBookValue>();
		}
		RefBook refBook = getByAttribute(attributeId);
		final RefBookAttribute attribute = refBook.getAttribute(attributeId);
		String sql = String.format(
				"SELECT id record_id, %s value FROM %s WHERE %s",
				attribute.getAlias(),
				tableName,
				transformToSqlInStatement("id", recordIds));

		DereferenceMapper mapper = new DereferenceMapper(attribute);
		getJdbcTemplate().query(sql, mapper);
		return mapper.getResult();
	}

    @Override
    public List<Long> isRecordsExist(String tablename, Set<Long> uniqueRecordIds) {
        //Исключаем несуществующие записи
        String sql = String.format("select id from %s where %s and status != -1", tablename, SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
        Set<Long> recordIds = new HashSet<Long>(uniqueRecordIds);
        List<Long> existRecords = new ArrayList<Long>();
        try {
            //Получаем список существующих записей среди входного набора
            existRecords = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}

        for (Iterator<Long> it = recordIds.iterator(); it.hasNext();) {
            Long recordId = it.next();
            //Если запись не найдена среди существующих, то удаляем ее из списка для остальных проверок
            if (existRecords.contains(recordId)) {
                it.remove();
            }
        }
        return new ArrayList<Long>(recordIds);
    }

    @Override
    public boolean isRefBookExist(long refBookId) {
        return getJdbcTemplate().queryForObject("select count(*) from ref_book where id = ?", new Object[]{refBookId}, Integer.class) > 0;
    }

    @Override
    public boolean isVersionsExist(Long refBookId, List<Long> recordIds, Date version) {
        String sql = "select count(*) from ref_book_record where ref_book_id = ? and %s and version = trunc(?, 'DD') and status != -1";
        return getJdbcTemplate().queryForInt(String.format(sql, transformToSqlInStatement("record_id", recordIds)), refBookId, version) != 0;
    }

    private static final String GET_INACTIVE_RECORDS_IN_PERIOD = "select id, start_version as versionFrom, end_version as versionTo, \n" +
            "        case when status = -1 then 0\n" +
            "             when ((:periodTo is not null and next_version is not null and next_version <= :periodTo) or (:periodTo is null and next_version is not null)) then 2 --дата окончания ограничивающего периода\n" +
            "             when ((end_version is not null and end_version < :periodFrom) or (:periodTo is not null and start_version > :periodTo)) then 1 end state                    \n" +
            "from   (\n" +
            "       select input.id as input_id, rbr.id, rbr.record_id, rbr.version as start_version, rbr.status, lead (rbr.version) over (partition by input.id order by rbr.version) - interval '1' DAY end_version, case when input.status = 0 then lead (rbr.version) over (partition by input.id, rbr.status order by rbr.version) end next_version \n" +
            "       from %s input\n" +
            "       join %s rbr on input.record_id = rbr.record_id %s and rbr.status != -1 \n" +
            "       where %s \n" +
            "       ) a where input_id = id";

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(String tableName, @NotNull List<Long> uniqueRecordIds, @NotNull Date periodFrom, @NotNull Date periodTo, boolean onlyExistence) {
        final List<ReferenceCheckResult> result = new ArrayList<ReferenceCheckResult>();
        Set<Long> recordIds = new HashSet<Long>(uniqueRecordIds);
        List<Long> existRecords = new ArrayList<Long>();

        //Исключаем несуществующие записи
        String sql = String.format("select id from %s where %s and status != -1", tableName, SqlUtils.transformToSqlInStatement("id", recordIds));
        try {
            //Получаем список существующих записей среди входного набора
            existRecords = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}
        for (Iterator<Long> it = recordIds.iterator(); it.hasNext();) {
            Long recordId = it.next();
            //Если запись не найдена среди существующих, то проставляем статус и удаляем ее из списка для остальных проверок
            if (!existRecords.contains(recordId)) {
                result.add(new ReferenceCheckResult(recordId, CheckResult.NOT_EXISTS));
                it.remove();
            }
        }

        if (!onlyExistence) {
            if (!recordIds.isEmpty()) {
                //Проверяем оставшиеся записи
                sql = String.format(GET_INACTIVE_RECORDS_IN_PERIOD,
                        tableName, tableName,
                        tableName.equals(RefBook.REF_BOOK_RECORD_TABLE_NAME) ? "and input.ref_book_id = rbr.ref_book_id " : "",
                        transformToSqlInStatement("input.id", recordIds));
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("periodFrom", periodFrom);
                params.addValue("periodTo", periodTo);
                try {
                    getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            Integer resultCode = SqlUtils.getInteger(rs, "state");
                            if (resultCode != null) {
                                result.add(new ReferenceCheckResult(
                                        SqlUtils.getLong(rs, "id"),
                                        rs.getDate("versionFrom"),
                                        rs.getDate("versionTo"),
                                        CheckResult.getByCode(resultCode)
                                ));
                            }
                        }
                    });
                } catch (EmptyResultDataAccessException ignored) {}
            }
        }

        return result;
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecords(String tableName, @NotNull List<Long> uniqueRecordIds) {
        final List<ReferenceCheckResult> result = new ArrayList<ReferenceCheckResult>();
        Set<Long> recordIds = new HashSet<Long>(uniqueRecordIds);
        List<Long> existRecords = new ArrayList<Long>();

        //Исключаем несуществующие записи
        String sql = String.format("select id from %s where %s", tableName, SqlUtils.transformToSqlInStatement("id", recordIds));
        try {
            //Получаем список существующих записей среди входного набора
            existRecords = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}
        for (Iterator<Long> it = recordIds.iterator(); it.hasNext();) {
            Long recordId = it.next();
            //Если запись не найдена среди существующих, то проставляем статус и удаляем ее из списка для остальных проверок
            if (!existRecords.contains(recordId)) {
                result.add(new ReferenceCheckResult(recordId, CheckResult.NOT_EXISTS));
                it.remove();
            }
        }
        return result;
    }

}