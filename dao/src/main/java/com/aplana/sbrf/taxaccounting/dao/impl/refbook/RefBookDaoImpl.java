package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookAddressValueMapper;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookCalendarValueMapper;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.QueryBuilder;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.ReferenceCheckResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    public static final String NOT_HIERARCHICAL_REF_BOOK_ERROR = "Справочник \"%s\" (id=%d) не является иерархичным";

    private static final String DELETE_VERSION = "update %s set status = -1 where %s";
    private static final String DELETE_VERSION_DELETE = "delete from %s where %s";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RefBookMapperFactory refBookMapperFactory;

    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;

    @Override
    @Cacheable(value = CacheConstants.REF_BOOK, key = "'id_'+#refBookId")
    public RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name, xsd_id FROM ref_book WHERE id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    @Override
    public List<RefBook> fetchAll() {
        return getJdbcTemplate().query(
                "SELECT id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name, xsd_id " +
                        "FROM ref_book " +
                        "ORDER BY name",
                new RefBookRowMapper()
        );
    }

    @Override
    public List<RefBook> fetchAllVisible() {
        return fetchAllByVisibility(1);
    }

    @Override
    public List<RefBook> fetchAllInvisible() {
        return fetchAllByVisibility(0);
    }

    private List<RefBook> fetchAllByVisibility(int visible) {
        return getJdbcTemplate().query(
                "select id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name, xsd_id " +
                        "from ref_book " +
                        "where visible = " + visible + " " +
                        "order by name",
                new RefBookRowMapper()
        );
    }

    @Override
    public List<RefBook> searchVisibleByName(String name) {
        if (org.apache.commons.lang3.StringUtils.isBlank(name)) {
            return fetchAllVisible();
        } else {
            return getJdbcTemplate().query(
                    "select id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name, xsd_id " +
                            "from ref_book " +
                            "where visible = 1 " +
                            "and lower(name) like '%" + name.toLowerCase() + "%' " +
                            "order by name",
                    new RefBookRowMapper()
            );
        }
    }

    @Override
    public PagingResult<RefBookConfListItem> fetchRefBookConfPage(PagingParams pagingParams) {
        String sql = "select id, name, visible, type, read_only, region_attribute_id from ref_book";
        String sqlOrdered = sql + " order by name";
        String sqlNumbered = "select rownum rn, ordered.* from (" + sqlOrdered + ") ordered";
        String sqlPaged = "SELECT * FROM (" + sqlNumbered + ") WHERE rn BETWEEN :start AND :end";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());

        List<RefBookConfListItem> page = getNamedParameterJdbcTemplate().query(sqlPaged,
                params,
                new RefBookConfRowMapper());
        int count = getJdbcTemplate().queryForObject("SELECT count(*) FROM (" + sql + ")", Integer.class);

        return new PagingResult<>(page, count);
    }

    @Override
    @Cacheable(value = CacheConstants.REF_BOOK, key = "'attribute_id_'+#attributeId")
    public RefBook getByAttribute(Long attributeId) {
        try {
            return get(getJdbcTemplate().queryForObject(
                    "SELECT r.id FROM ref_book r JOIN ref_book_attribute a ON a.ref_book_id = r.id WHERE a.id = ?",
                    new Object[]{attributeId}, new int[]{Types.NUMERIC}, Long.class));
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден атрибут справочника с id = %d", attributeId));
        }
    }

    /**
     * Настройка маппинга для справочника
     */
    private class RefBookRowMapper implements RowMapper<RefBook> {
        @Override
        public RefBook mapRow(ResultSet rs, int index) throws SQLException {
            RefBook result = new RefBook();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setName(rs.getString("name"));
            result.setScriptId(rs.getString("script_id"));
            result.setVisible(rs.getBoolean("visible"));
            result.setAttributes(getAttributes(result.getId()));
            result.setType(SqlUtils.getInteger(rs, "type"));
            result.setReadOnly(rs.getBoolean("read_only"));
            result.setTableName(rs.getString("table_name"));
            result.setVersioned(rs.getBoolean("is_versioned"));
            result.setXsdId(rs.getString("xsd_id"));
            BigDecimal regionAttributeId = (BigDecimal) rs.getObject("REGION_ATTRIBUTE_ID");
            if (regionAttributeId == null) {
                result.setRegionAttribute(null);
            } else {
                result.setRegionAttribute(getAttribute(regionAttributeId.longValue()));
            }
            return result;
        }
    }

    private class RefBookConfRowMapper implements RowMapper<RefBookConfListItem> {
        @Override
        public RefBookConfListItem mapRow(ResultSet rs, int index) throws SQLException {
            RefBookConfListItem result = new RefBookConfListItem();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setName(rs.getString("name"));
            result.setVisible(rs.getBoolean("visible"));
            result.setReadOnly(rs.getBoolean("read_only"));
            result.setRefBookType(RefBookType.get(SqlUtils.getInteger(rs, "type")));
            result.setRegionality(rs.getObject("REGION_ATTRIBUTE_ID") == null ? "Общий" : "Региональный");
            return result;
        }
    }

    @Override
    @Cacheable(value = CacheConstants.REF_BOOK_ATTRIBUTE, key = "'ref_book_id_'+#refBookId")
    public List<RefBookAttribute> getAttributes(Long refBookId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
                            "is_unique, sort_order, format, read_only, max_length " +
                            "FROM ref_book_attribute WHERE ref_book_id = ? ORDER BY ord",
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
            if (result.getRefBookAttributeId() != null)
                result.setRefBookAttribute(getAttribute(result.getRefBookAttributeId()));
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

    @Override
    @Cacheable(value = CacheConstants.REF_BOOK_ATTRIBUTE, key = "'id_'+#attributeId")
    public RefBookAttribute getAttribute(@NotNull Long attributeId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
                            "is_unique, sort_order, format, read_only, max_length " +
                            "FROM ref_book_attribute WHERE id = ?",
                    new Object[]{attributeId}, new int[]{Types.NUMERIC},
                    new RefBookAttributeRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден атрибут с id = %d", attributeId));
        }
    }

    @Override
    public RefBookAttribute getAttribute(Long refBookId, String attributeAlias) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("refBookId", refBookId);
            params.addValue("attributeAlias", attributeAlias);
            return getNamedParameterJdbcTemplate().queryForObject(
                    "SELECT id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
                            "is_unique, sort_order, format, read_only, max_length " +
                            "FROM ref_book_attribute WHERE ref_book_id = :refBookId AND alias = :attributeAlias",
                    params, new RefBookAttributeRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден атрибут с алиасом = %s для справочника с id = %s", attributeAlias, refBookId));
        }
    }

    /**
     * Разыменовывает текущую строку resultSet в графе columnName используя информацию об атрибуте справочника attribute
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
    public void updateScriptId(Long refBookId, String scriptId) {
        getJdbcTemplate().update("UPDATE ref_book SET script_id = ? WHERE id = ?", scriptId, refBookId);
    }

    @Override
    public void updateXsdId(Long refBookId, String xsdId) {
        getJdbcTemplate().update("UPDATE ref_book SET xsd_id = ? WHERE id = ?", xsdId, refBookId);
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
        ps.appendQuery("SELECT row_number_over");
        ps.appendQuery(", id ");
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
        if (filterPS.getJoinPartsOfQuery() != null) {
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
            if (filterPS.getJoinPartsOfQuery() != null) {
                ps.appendQuery("frb.");
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
     * Перегруженная функция с восходящей сортировкой по умолчанию
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, String whereClause) {
        return getRecords(refBookId, tableName, pagingParams, filter, sortAttribute, true, whereClause);
    }

    /**
     * Возвращает элементы справочника
     */
    @Override
    public List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook) {
        RowMapper<Map<String, RefBookValue>> rowMapper = getRowMapper(refBook);
        if (!ps.getParams().isEmpty()) {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), rowMapper);
        } else {
            return getJdbcTemplate().query(ps.getQuery().toString(), rowMapper);
        }
    }

    @Override
    public <T extends RefBookSimple> List<T> getMappedRecordsData(QueryBuilder q, RefBook refBook) {
        RowMapper<T> rowMapper = refBookMapperFactory.getMapper(refBook.getId());
        return getNamedParameterJdbcTemplate().query(q.getPagedQuery(), q.getNamedParams(), rowMapper);
    }

    private RowMapper<Map<String, RefBookValue>> getRowMapper(RefBook refBook) {
        if (refBook.getId().equals(RefBook.Id.CALENDAR.getId())) {
            return new RefBookCalendarValueMapper(refBook);
        } else if (refBook.getId().equals(RefBook.Id.PERSON_ADDRESS.getId())) {
            return new RefBookAddressValueMapper(refBook);
        } else {
            return new RefBookValueMapper(refBook);
        }
    }

    /**
     * Возвращает элементы справочника с вычисленным столбцом has_child
     */
    @Override
    public List<Map<String, RefBookValue>> getRecordsWithHasChild(PreparedStatementData ps, RefBook refBook) {
        return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook) {
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
     */
    @Override
    public Long getRowNum(PreparedStatementData ps, Long recordId) {
        try {
            ps.addParam(recordId);
            return getJdbcTemplate().queryForObject("select " + RefBook.RECORD_SORT_ALIAS + " from (" + ps.getQuery().toString() + ") where " + RefBook.RECORD_ID_ALIAS + " = ?",
                    ps.getParams().toArray(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Integer getRecordsCount(PreparedStatementData ps) {
        if (!ps.getParams().isEmpty()) {
            return getJdbcTemplate().queryForObject("select count(*) from (" + ps.getQuery().toString() + ")", ps.getParams().toArray(), Integer.class);
        } else {
            return getJdbcTemplate().queryForObject("select count(*) from (" + ps.getQuery().toString() + ")", Integer.class);
        }
    }

    @Override
    public void updateVersionRelevancePeriod(String tableName, Long uniqueRecordId, Date version) {
        getJdbcTemplate().update(String.format("update %s set version=? where id=?", tableName), version, uniqueRecordId);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(final Long refBookId, final String tableName, final Long id) {
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
        params.put("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, getRowMapper(refBook));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds, boolean isDelete) {
        String sql = String.format(isDelete ? DELETE_VERSION_DELETE : DELETE_VERSION, tableName, transformToSqlInStatement("id", uniqueRecordIds));
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
            Long recordId = rs.getLong(RefBook.RECORD_ID_ALIAS);
            Object value = parseRefBookValue(rs, "value", attribute);
            result.put(recordId, new RefBookValue(attribute.getAttributeType(), value));
        }

        public Map<Long, RefBookValue> getResult() {
            return result;
        }
    }

    private static int IN_CLAUSE_LIMIT = 10000;

    @Override
    public Map<Long, RefBookValue> dereferenceValues(String tableName, Long attributeId, Collection<Long> recordIds) {
        if (recordIds.isEmpty()) {
            return new HashMap<Long, RefBookValue>();
        }

        List<Long> recordList = new ArrayList<Long>(recordIds);
        Map<Long, RefBookValue> result = new HashMap<Long, RefBookValue>();
        RefBook refBook = getByAttribute(attributeId);
        final RefBookAttribute attribute = refBook.getAttribute(attributeId);
        int n = ((int) Math.floor(recordList.size() / (double) IN_CLAUSE_LIMIT) + 1);
        for (int i = 0; i < n; i++) {
            List<Long> ids = new ArrayList<Long>();
            ids.addAll(recordList.subList(i * IN_CLAUSE_LIMIT, Math.min((i + 1) * IN_CLAUSE_LIMIT, recordList.size())));
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id ")
                    .append(RefBook.RECORD_ID_ALIAS)
                    .append(", ")
                    .append(attribute.getAlias())
                    .append(" value FROM ")
                    .append(tableName)
                    .append(" WHERE ")
                    .append(transformToSqlInStatement("id", ids));

            DereferenceMapper mapper = new DereferenceMapper(attribute);
            getJdbcTemplate().query(sql.toString(), mapper);
            result.putAll(mapper.getResult());
        }
        return result;
    }

    @Override
    public boolean isRefBookExist(long refBookId) {
        return getJdbcTemplate().queryForObject("SELECT count(*) FROM ref_book WHERE id = ?", new Object[]{refBookId}, Integer.class) > 0;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(RefBook refBook, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, String direction) {
        QueryBuilder q;
        if (refBook.isVersioned()) {
            q = queryBuilder.allRecordsByVersion(refBook, version, filter, pagingParams, sortAttribute, direction);
        } else {
            q = queryBuilder.allRecords(refBook, filter, pagingParams, sortAttribute, direction);
        }
        List<Map<String, RefBookValue>> records = getNamedParameterJdbcTemplate().query(q.getPagedQuery(), q.getNamedParams(), getRowMapper(refBook));

        PagingResult<Map<String, RefBookValue>> result = new PagingResult<>(records);
        if (pagingParams != null) {
            result.setTotalCount(getNamedParameterJdbcTemplate().queryForObject(q.getCountQuery(), q.getNamedParams(), Integer.class));
        } else {
            result.setTotalCount(records.size());
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
        } catch (EmptyResultDataAccessException ignored) {
        }
        for (Iterator<Long> it = recordIds.iterator(); it.hasNext(); ) {
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