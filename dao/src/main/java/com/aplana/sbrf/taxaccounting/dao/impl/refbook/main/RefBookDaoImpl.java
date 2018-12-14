package com.aplana.sbrf.taxaccounting.dao.impl.refbook.main;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookCalendarValueMapper;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Repository
public class RefBookDaoImpl extends AbstractDao implements RefBookDao {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;


    @Override
    @Cacheable(value = CacheConstants.REF_BOOK, key = "'id_'+#refBookId")
    public RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name, xsd_id from ref_book where id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    @Override
    public List<RefBook> findAllVisible() {
        return getJdbcTemplate().query("" +
                        "select id, name, script_id, visible, type, read_only, is_versioned, region_attribute_id, table_name, xsd_id " +
                        "from ref_book " +
                        "where visible = 1 " +
                        "order by name",
                new RefBookRowMapper()
        );
    }

    @Override
    public List<RefBookShortInfo> findAllVisibleShortInfo(String name, PagingParams pagingParams) {

        String query = "" +
                "select id, name, read_only " +
                "from ref_book " +
                "where visible = 1";

        if (isNotBlank(name)) {
            query = query + "\n" + "and lower(name) like '%" + name.toLowerCase() + "%' ";
        }

        String property = "name";
        String direction = "asc";

        if (pagingParams != null) {
            String givenProperty = pagingParams.getProperty();
            String givenDirection = pagingParams.getDirection();

            if (isNotEmpty(givenProperty) && givenProperty.equals("readOnly")) {
                property = "read_only";
            }
            if (isNotEmpty(givenDirection)) {
                direction = givenDirection;
            }
        }

        query = query + "\n" + "order by " + property + " " + direction;

        return getJdbcTemplate().query(query, new RefBookShortInfoRowMapper());
    }

    @Override
    public PagingResult<RefBookConfListItem> fetchRefBookConfPage(PagingParams pagingParams) {
        String sql = "select id, name, visible, type, read_only, region_attribute_id from ref_book";
        String sqlOrdered = sql + " order by name";
        String sqlNumbered = "select rownum rn, ordered.* from (" + sqlOrdered + ") ordered";
        String sqlPaged = "select * from (" + sqlNumbered + ") where rn between :start and :end";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());

        List<RefBookConfListItem> page = getNamedParameterJdbcTemplate().query(sqlPaged,
                params,
                new RefBookConfRowMapper());
        int count = getJdbcTemplate().queryForObject("select count(*) from (" + sql + ")", Integer.class);

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

    @Override
    @Cacheable(value = CacheConstants.REF_BOOK_ATTRIBUTE, key = "'ref_book_id_'+#refBookId")
    public List<RefBookAttribute> getAttributes(Long refBookId) {
        try {
            return getJdbcTemplate().query("" +
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
    @CacheEvict(value = CacheConstants.REF_BOOK, key = "'id_'+#refBookId")
    public void updateScriptId(Long refBookId, String scriptId) {
        getJdbcTemplate().update("update ref_book set script_id = ? where id = ?", scriptId, refBookId);
    }

    @Override
    public void updateXsdId(Long refBookId, String xsdId) {
        getJdbcTemplate().update("update ref_book set xsd_id = ? where id = ?", xsdId, refBookId);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause) {
        RefBook refBook = get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, isSortAscending, whereClause);
        List<Map<String, RefBookValue>> records = getRecordsData(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, null, isSortAscending, whereClause);
        result.setTotalCount(getRecordsCount(ps));
        return result;
    }

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     *
     * @param tableName название таблицы для которой формируется запрос
     * @param refBook   справочник
     */
    private PreparedStatementData getSimpleQuery(RefBook refBook,
                                                 String tableName,
                                                 RefBookAttribute sortAttribute,
                                                 String filter,
                                                 PagingParams pagingParams,
                                                 boolean isSortAscending,
                                                 String whereClause) {
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

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, String whereClause) {
        return getRecords(refBookId, tableName, pagingParams, filter, sortAttribute, true, whereClause);
    }

    @Override
    public List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook) {
        RowMapper<Map<String, RefBookValue>> rowMapper = getRowMapper(refBook);
        if (!ps.getParams().isEmpty()) {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), rowMapper);
        } else {
            return getJdbcTemplate().query(ps.getQuery().toString(), rowMapper);
        }
    }

    private RowMapper<Map<String, RefBookValue>> getRowMapper(RefBook refBook) {
        if (refBook.getId().equals(RefBook.Id.CALENDAR.getId())) {
            return new RefBookCalendarValueMapper(refBook);
        } else {
            return new RefBookValueMapper(refBook);
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
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, getRowMapper(refBook));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void deleteRecordVersions(String tableName, @NotNull List<Long> uniqueRecordIds, boolean isDelete) {
        String setStatusDeleted = "update %s set status = -1 where %s";
        String deleteFinally = "delete from %s where %s";
        String sql = String.format(isDelete ? deleteFinally : setStatusDeleted, tableName, transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

    @Override
    public Map<Long, RefBookValue> dereferenceValues(String tableName, Long attributeId, Collection<Long> recordIds) {

        final int IN_CLAUSE_LIMIT = 10000;

        if (recordIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> recordList = new ArrayList<>(recordIds);
        Map<Long, RefBookValue> result = new HashMap<>();
        RefBook refBook = getByAttribute(attributeId);
        final RefBookAttribute attribute = refBook.getAttribute(attributeId);
        int n = ((int) Math.floor(recordList.size() / (double) IN_CLAUSE_LIMIT) + 1);
        for (int i = 0; i < n; i++) {
            List<Long> ids = new ArrayList<>(recordList.subList(i * IN_CLAUSE_LIMIT, Math.min((i + 1) * IN_CLAUSE_LIMIT, recordList.size())));

            DereferenceMapper mapper = new DereferenceMapper(attribute);
            String sql = "SELECT id " +
                    RefBook.RECORD_ID_ALIAS +
                    ", " +
                    attribute.getAlias() +
                    " value FROM " +
                    tableName +
                    " WHERE " +
                    transformToSqlInStatement("id", ids);
            getJdbcTemplate().query(sql, mapper);
            result.putAll(mapper.getResult());
        }
        return result;
    }

    @Override
    public boolean isRefBookExist(long refBookId) {
        return getJdbcTemplate().queryForObject("select count(*) from ref_book where id = ?", new Object[]{refBookId}, Integer.class) > 0;
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
        final List<ReferenceCheckResult> result = new ArrayList<>();
        Set<Long> recordIds = new HashSet<>(uniqueRecordIds);
        List<Long> existRecords = new ArrayList<>();

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
}
