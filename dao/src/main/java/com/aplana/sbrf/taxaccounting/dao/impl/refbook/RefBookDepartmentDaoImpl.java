package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributePair;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: ekuvshinov
 */
@Repository
public class RefBookDepartmentDaoImpl extends AbstractDao implements RefBookDepartmentDao {

	private static final Log LOG = LogFactory.getLog(RefBookDepartmentDaoImpl.class);

	private static final String TABLE_NAME = "DEPARTMENT";

    @Autowired
    ReportPeriodDao reportPeriodDao;
    @Autowired
    RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookDao.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public Long getRowNum(Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(REF_BOOK_ID, TABLE_NAME, recordId, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(REF_BOOK_ID, TABLE_NAME, recordId);
    }

    private final static String CHECK_UNIQUE_MATCHES_FOR_NON_VERSION =
            "select name from department t where t.is_active = 1 AND %s %s %s";
    @Override
    public List<Pair<String, String>> getMatchedRecordsByUniqueAttributes(Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION);
        ArrayList<RefBookAttribute> uniqueAttrs = new ArrayList<RefBookAttribute>();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0){
                uniqueAttrs.add(attribute);
            }
        }
        //Если уникальных атрибутов нет, то поиск не нужен
        if (uniqueAttrs.isEmpty())
            return new ArrayList<Pair<String, String>>(0);
        String idOddsTag = recordId != null ? "t.id <> " + recordId : "";

        ArrayList<Pair<String, String>> pairList = new ArrayList<Pair<String, String>>();
        String andTag = !uniqueAttrs.isEmpty() && recordId != null ? "AND" : "";
        for (final RefBookAttribute attribute : uniqueAttrs) {
            String querySql;
            Object value = null;
            querySql = String.format(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION,
                    idOddsTag,
                    andTag,
                    "t." + attribute.getAlias() + " = ?" );
            Map<String, RefBookValue> values = records.get(0).getValues();

            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                value =  values.get(attribute.getAlias()).getStringValue();
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                value = values.get(attribute.getAlias()).getReferenceValue();
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                value = values.get(attribute.getAlias()).getNumberValue();
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                value = values.get(attribute.getAlias()).getDateValue();
            }

            try {
                pairList.addAll(getJdbcTemplate().query(querySql, new Object[]{value},
                        new RowMapper<Pair<String, String>>() {
                            @Override
                            public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                                return new Pair<String, String>(rs.getString("NAME"), attribute.getName());
                            }
                        }));
            } catch (DataAccessException e) {
                throw new DaoException("Ошибка при поиске уникальных значений в справочнике " + TABLE_NAME, e);
            }
        }

        return pairList;
    }

    private static final String UPDATE_DEPARTMENT = "update department t set %s where id = ?";

    @Override
    //@CacheEvict(value = CacheConstants.DEPARTMENT,key = "#uniqueId", beforeInvocation = true)
    public void update(int uniqueId, Map<String, RefBookValue> records,  List<RefBookAttribute> attributes) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(UPDATE_DEPARTMENT);
        StringBuilder sql = new StringBuilder();
        for (RefBookAttribute attribute : attributes) {
            sql.append(String.format("t.%s = ?,",  attribute.getAlias()));

            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                ps.addParam(records.get(attribute.getAlias()).getStringValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                ps.addParam(records.get(attribute.getAlias()).getReferenceValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                ps.addParam(records.get(attribute.getAlias()).getNumberValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                ps.addParam(records.get(attribute.getAlias()).getDateValue());
            }
        }
        ps.addParam(uniqueId);
        getJdbcTemplate().update(
                String.format(ps.getQuery().toString(), sql.toString().substring(0, sql.toString().length() - 1)),
                ps.getParams().toArray());
    }

    private static final String CREATE_DEPARTMENT = "insert into department (id, %s) values(seq_department.nextval, %s)";
    @Override
    public int create(Map<String, RefBookValue> record, List<RefBookAttribute> attributes) {
        final PreparedStatementData ps = new PreparedStatementData();
        for (RefBookAttribute attribute : attributes) {
            ps.appendQuery(attribute.getAlias() + ",");

            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                ps.addParam(record.get(attribute.getAlias()).getStringValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                ps.addParam(record.get(attribute.getAlias()).getReferenceValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                ps.addParam(record.get(attribute.getAlias()).getNumberValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                ps.addParam(record.get(attribute.getAlias()).getDateValue());
            }
        }
        final String ph = SqlUtils.preparePlaceHolders(attributes.size());
        try {
            PreparedStatementCreator psc = new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement statement = con.prepareStatement(
                            String.format(CREATE_DEPARTMENT, ps.getQuery().toString().substring(0, ps.getQuery().toString().length() - 1), ph),
                            new String[]{"ID"}
                    );
                    for (int i =0; i < ps.getParams().size(); i++)
                        statement.setObject(i+1, ps.getParams().get(i));
                    return statement;
                }
            };
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update(
                    psc, keyHolder);
            return keyHolder.getKey().intValue();
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void remove(long uniqueId) {
        try {
            getJdbcTemplate().update("DELETE FROM department WHERE id = ?", uniqueId);
        } catch (DataIntegrityViolationException e){
            throw new DaoException("Нарушение ограничения целостности. Возможно обнаружена порожденная запись.", e);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public int getRecordsCount(String filter) {
        return refBookDao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }

    private final static String GET_ATTRIBUTES_VALUES = "select \n" +
            "  attribute_id,\n" +
            "  record_id, \n" +
            "  value,\n" +
            "  data_type\n" +
            "from (\n" +
            "  with t as (\n" +
            "  select id as record_id, name, to_char(parent_id) as parent_id, to_char(type)as type, shortname, to_char(tb_index) as tb_index, sbrf_code, to_char(region_id) as region_id, to_char(is_active) as is_active, to_char(code) as code from department \n" +
            "  )\n" +
            "  select a.id as attribute_id, a.type as data_type, record_id, value from t\n" +
            "  unpivot \n" +
            "  (value for attribute_alias in (NAME, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code)) \n" +
            "  join ref_book_attribute a on attribute_alias = a.alias\n" +
            "  where ref_book_id = 30\n" +
            ") where (record_id, attribute_id) in ";

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        final Map<RefBookAttributePair, String> result = new HashMap<RefBookAttributePair, String>();
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(GET_ATTRIBUTES_VALUES);
        ps.appendQuery("(");
        for (Iterator<RefBookAttributePair> it = attributePairs.iterator(); it.hasNext();) {
            RefBookAttributePair pair = it.next();
            ps.appendQuery("(?,?)");
            ps.addParam(pair.getUniqueRecordId());
            ps.addParam(pair.getAttributeId());
            if (it.hasNext()) {
                ps.appendQuery(",");
            }
        }
        ps.appendQuery(")");
        getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        result.put(new RefBookAttributePair(rs.getLong("attribute_id"), rs.getLong("record_id")), rs.getString("value"));
                    }
                });
        return result;
    }

    @Override
    public List<Long> isRecordsActiveInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        String sql = "select id from "+ TABLE_NAME +" where %s";
        Set<Long> result = new HashSet<Long>(recordIds);
        List<Long> existRecords = new ArrayList<Long>();
        try {
            existRecords = getJdbcTemplate().query(String.format(sql, SqlUtils.transformToSqlInStatement("id", recordIds)), new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}
        result.removeAll(existRecords);
        return new ArrayList<Long>(result);
    }

    private static final String CHECK_USAGES_IN_REFBOOK =
            "WITH\n" +
                    "    recordsByVersion AS (SELECT\n" +
                    "                           r.ID,\n" +
                    "                           r.RECORD_ID,\n" +
                    "                           r.REF_BOOK_ID,\n" +
                    "                           r.VERSION,\n" +
                    "                           r.STATUS,\n" +
                    "                           row_number()\n" +
                    "                           OVER (PARTITION BY r.RECORD_ID\n" +
                    "                             ORDER BY r.version) rn\n" +
                    "                         FROM REF_BOOK_RECORD r\n" +
                    "                         WHERE r.REF_BOOK_ID in (:refBookIds)),\n" +
                    "    t AS (SELECT\n" +
                    "            rv.ID,\n" +
                    "            rv.RECORD_ID                   RECORD_ID,\n" +
                    "            rv.REF_BOOK_ID,\n" +
                    "            rv.VERSION                     version,\n" +
                    "            rv2.version - interval '1' day versionEnd\n" +
                    "          FROM recordsByVersion rv LEFT OUTER JOIN recordsByVersion rv2\n" +
                    "              ON rv.RECORD_ID = rv2.RECORD_ID AND rv.rn + 1 = rv2.rn\n" +
                    "          WHERE rv.status in (0,1))\n" +
                    "SELECT\n" +
                    "  t.id,\n" +
                    "  b.name    AS refbookName,\n" +
                    "  t.version AS versionStart,\n" +
                    "  t.versionEnd AS versionEnd,\n" +
                    "  v.string_value,\n" +
                    "  v.number_value,\n" +
                    "  v.date_value,\n" +
                    "  v.reference_value,\n" +
                    "  a.is_unique,\n" +
                    "  b.id as ref_book_id\n" +
                    "FROM ref_book b\n" +
                    "  JOIN t ON b.id = t.ref_book_id\n" +
                    "  JOIN ref_book_value v ON v.record_id = t.id AND (v.reference_value IN (:uniqueRefId))\n" +
                    "  JOIN ref_book_attribute a\n" +
                    "    ON (a.ref_book_id = b.id OR a.reference_id = b.id) AND t.ref_book_id = a.ref_book_id AND a.id = v.attribute_id";

    @Override
    public Map<Integer, Map<String, Object>> isVersionUsedInRefBooks(List<Long> refBookIds, List<Long> uniqueRecordIds) {
        Map<String, Object> params = new HashMap<String, Object>(2);
        //Проверка использования в справочниках
        try {
            params.put("refBookIds", refBookIds);
            params.put("uniqueRefId", uniqueRecordIds);

            final Map<Integer, Map<String, Object>> records = new HashMap<Integer, Map<String, Object>>();

            getNamedParameterJdbcTemplate().query(CHECK_USAGES_IN_REFBOOK, params, new RowMapper<Map<Integer, Map<String, Object>>>() {
                @Override
                public Map<Integer, Map<String, Object>> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    int id = rs.getInt("id");
                    int is_unique = rs.getInt("is_unique");
                    Map<String, Object> recordValues = new HashMap<String, Object>();
                    recordValues.put(REFBOOK_NAME_ALIAS, rs.getString(REFBOOK_NAME_ALIAS));
                    recordValues.put(VERSION_START_ALIAS, rs.getDate(VERSION_START_ALIAS));
                    recordValues.put(REFBOOK_ID_ALIAS, rs.getLong(REFBOOK_ID_ALIAS));
                    recordValues.put(VERSION_END_ALIAS, rs.getDate(VERSION_END_ALIAS));

                    if (is_unique != 0) {
                        StringBuilder attr = new StringBuilder();
                        concatAttrs(rs, attr);
                        recordValues.put(UNIQUE_ATTRIBUTES_ALIAS, attr.toString());
                    }
                    records.put(id, recordValues);

                    return records;
                }

                public void concatAttrs(ResultSet rs, StringBuilder attr) throws SQLException {
                    attr.append(rs.getString(STRING_VALUE_COLUMN_ALIAS) != null ? rs.getString(STRING_VALUE_COLUMN_ALIAS) + ", " : "");
                    attr.append(rs.getString(NUMBER_VALUE_COLUMN_ALIAS) != null ? rs.getFloat(NUMBER_VALUE_COLUMN_ALIAS) + ", " : "");
                    attr.append(rs.getDate(DATE_VALUE_COLUMN_ALIAS) != null ? rs.getDate(DATE_VALUE_COLUMN_ALIAS) + ", " : "");
                    attr.append(rs.getString(REFERENCE_VALUE_COLUMN_ALIAS) != null ? rs.getInt(REFERENCE_VALUE_COLUMN_ALIAS) + ", " : "");
                }
            });

            return records;
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<Integer, Map<String, Object>>(0);
        } catch (DataAccessException e) {
			LOG.error("Проверка использования", e);
            throw new DaoException("Проверка использования", e);
        }
    }
    
    private static final String GET_REPORT_PERIOD_NAME = 
            "WITH record_date AS (SELECT\n" +
                    "                  v.record_id AS record_id\n" +
                    "                FROM ref_book b\n" +
                    "                  JOIN ref_book_attribute a ON a.ref_book_id = b.id\n" +
                    "                  JOIN ref_book_value v ON v.attribute_id = a.id\n" +
                    "                WHERE b.id = 8 AND to_char(v.DATE_VALUE, 'DDMM') = to_char(:startDate, 'DDMM') AND a.alias = 'CALENDAR_START_DATE'),\n" +
                    "  record_type AS(SELECT\n" +
                    "                   v.record_id AS record_id\n" +
                    "                 FROM ref_book b\n" +
                    "                   JOIN ref_book_attribute a ON a.ref_book_id = b.id\n" +
                    "                   JOIN ref_book_value v ON v.attribute_id = a.id\n" +
                    "                 WHERE b.id = 8 AND a.alias = :taxCode AND v.NUMBER_VALUE = 1\n" +
                    ")\n" +
                    "SELECT\n" +
                    "  rbv.STRING_VALUE\n" +
                    "FROM ref_book_attribute a\n" +
                    "  JOIN record_date rd ON 1 = 1\n" +
                    "  JOIN record_type rt ON 1 = 1\n" +
                    "  JOIN ref_book_value rbv ON rbv.attribute_id = a.id AND rbv.RECORD_ID = rd.record_id AND rbv.RECORD_ID = rt.record_id\n" +
                    "WHERE a.alias = 'NAME'";

    @Override
    public String getReportPeriodNameByDate(TaxType taxType, Date startDate) {
        try{
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("taxCode", String.valueOf(taxType.getCode()));
            params.put("startDate", startDate);
            return getNamedParameterJdbcTemplate().queryForObject(GET_REPORT_PERIOD_NAME, params, String.class);
        } catch (EmptyResultDataAccessException e){
            return "";
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
    public boolean isRecordsExist(List<Long> uniqueRecordIds) {
        return getJdbcTemplate().queryForObject(String.format("select count (*) from department where %s", SqlUtils.transformToSqlInStatement("id", uniqueRecordIds)), Integer.class) == uniqueRecordIds.size();
    }
}
