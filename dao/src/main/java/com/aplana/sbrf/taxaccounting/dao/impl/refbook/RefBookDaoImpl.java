package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.BDUtils;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.UniversalFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    @Autowired
    private RefBookUtils refBookUtils;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BDUtils dbUtils;

    @Override
    @Cacheable(value = "PermanentData", key = "'RefBook_'+#refBookId.toString()")
    public RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, script_id, visible, type, read_only from ref_book where id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    @Override
	 public List<RefBook> getAll(Integer typeId) {
        return getAll(getJdbcTemplate().queryForList("select id from ref_book where (? is null or type = ?) order by name",
                            new Object[] {typeId, typeId},
                            Long.class));
     }

    private List<RefBook> getAll(List<Long> ids) {
        List<RefBook> refBookList= new ArrayList();
        for (Long id: ids){
            refBookList.add(get(id));
        }

        return refBookList;
    }

	@Override
	public List<RefBook> getAllVisible(Integer typeId) {
		return getAll(
                    getJdbcTemplate().queryForList(
				        "select id from ref_book where visible = 1 and (? is null or type = ?) order by name",
                        new Object[] {typeId, typeId},
                        Long.class));
	}

    @Override
    public RefBook getByAttribute(long attributeId) {
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
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setScriptId(rs.getString("script_id"));
			result.setVisible(rs.getBoolean("visible"));
            result.setAttributes(getAttributes(result.getId()));
			result.setType(rs.getInt("type"));
			result.setReadOnly(rs.getBoolean("read_only"));
            return result;
        }
    }

    @Override
    public List<RefBookAttribute> getAttributes(Long refBookId) {
        try {
            return getJdbcTemplate().query(
                    "select id, name, alias, type, reference_id, attribute_id, visible, precision, width, required, " +
							"is_unique, sort_order " +
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
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setAlias(rs.getString("alias"));
            result.setAttributeType(RefBookAttributeType.values()[rs.getInt("type") - 1]);
            result.setRefBookId(rs.getLong("reference_id"));
            result.setRefBookAttributeId(rs.getLong("attribute_id"));
            result.setVisible(rs.getBoolean("visible"));
            result.setPrecision(rs.getInt("precision"));
            result.setWidth(rs.getInt("width"));
            result.setRequired(rs.getBoolean("required"));
            result.setUnique(rs.getBoolean("is_unique"));
			result.setSortOrder(rs.getInt("sort_order"));
            return result;
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

    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute) {
        return getRecords(refBookId, version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        String sql = getRefBookRecordSql(refBookId, recordId);
        RefBook refBook = get(refBookId);
		try {
        	return getJdbcTemplate().queryForObject(sql, new RefBookValueMapper(refBook));
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format("В справочнике \"%s\"(id = %d) не найдена строка с id = %d", refBook.getName(), refBookId, recordId));
		}
    }

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private static final String WITH_STATEMENT =
            "with t as (select\n" +
                    "  max(version) version, record_id\n" +
                    "from\n" +
                    "  ref_book_record\n" +
                    "where\n" +
                    "  ref_book_id = ? and status = 0 and version <= ?\n" +
                    "group by\n" +
                    "  record_id)\n";

    private static final String RECORD_VERSIONS_STATEMENT =
            "with currentRecord as (select id, record_id, version from REF_BOOK_RECORD where id=%d),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, r.REF_BOOK_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r, currentRecord cr where r.REF_BOOK_ID=%d and r.RECORD_ID=cr.RECORD_ID), \n" +
            "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";

    /**
     * Динамически формирует запрос для справочника
     *
     * @param refBookId     код справочника
     * @param recordId      идентификатор записи справочника. Если = null, то получаем все записи справочника, иначе - получаем все версии записи справочника
     * @param version       дата актуальности данных справочника. Если = null, то версионирование не учитывается
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param pagingParams  параметры для постраничной навигации. Может быть null, тогда возвращается весь набор данных по текущему срезу
     * @return
     */
    private PreparedStatementData getRefBookSql(Long refBookId, Long recordId, Date version, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams, boolean isSortAscending) {
        // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        RefBook refBook = get(refBookId);
        List<RefBookAttribute> attributes = refBook.getAttributes();

        if (sortAttribute != null && !attributes.contains(sortAttribute)) {
            throw new IllegalArgumentException(String.format("Reference book (id=%d) doesn't contains attribute \"%s\"",
                    refBookId, sortAttribute.getAlias()));
        }

        PreparedStatementData filterPS = new PreparedStatementData();
        UniversalFilterTreeListener universalFilterTreeListener =  applicationContext.getBean("universalFilterTreeListener", UniversalFilterTreeListener.class);
        universalFilterTreeListener.setRefBook(refBook);
        universalFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, universalFilterTreeListener);

        StringBuilder fromSql = new StringBuilder("\nfrom\n");

        fromSql.append("  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)\n");
        if (version != null) {
            ps.appendQuery(WITH_STATEMENT);
			ps.addParam(refBookId);
			ps.addParam(version);
        } else {
            ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT, recordId, refBookId));
            ps.addParam(VersionedObjectStatus.NORMAL.getId());
        }

        ps.appendQuery("SELECT * FROM "); //TODO: заменить "select *" на полное перечисление полей (Marat Fayzullin 30.01.2014)
        ps.appendQuery("(select\n");
        ps.appendQuery("  r.id as \"");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        ps.appendQuery("\",\n");

        if (version == null) {
            ps.appendQuery("  t.version as \"");
            ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
            ps.appendQuery("\",\n");

            ps.appendQuery("  t.versionEnd as \"");
            ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
            ps.appendQuery("\",\n");
        }

        if (isSupportOver() && sortAttribute != null) {
            // эту часть кода нельзя покрыть юнит тестами с использованием hsql потому что она не поддерживает row_number()
            ps.appendQuery("row_number()");
            // Надо делать сортировку
            ps.appendQuery(" over (order by ");
            ps.appendQuery("a");
            ps.appendQuery(sortAttribute.getAlias());
            ps.appendQuery(".");
            ps.appendQuery(sortAttribute.getAttributeType().toString());
            ps.appendQuery("_value ");
            ps.appendQuery(isSortAscending ? "ASC":"DESC");
            ps.appendQuery(")");
            ps.appendQuery(" as row_number_over,\n");
        } else {
            // База тестовая и не поддерживает row_number() значит сортировка работать не будет
            ps.appendQuery("rownum row_number_over,\n");
        }
        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();
            ps.appendQuery("  a");
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
        if (filterPS.getJoinPartsOfQuery() != null){
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
        ps.appendQuery(")");

        if (pagingParams != null) {
            ps.appendQuery(" where row_number_over between ? and ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(String.valueOf(pagingParams.getStartIndex() + pagingParams.getCount()));
        }

        return ps;
    }

    /**
     * Динамически формирует запрос для справочника
     *
     * @param refBookId код справочника
     * @param recordId  код строки справочника
     * @return
     */
    private String getRefBookRecordSql(Long refBookId, Long recordId) {
        RefBook refBook = get(refBookId);
        StringBuilder fromSql = new StringBuilder("\nfrom\n");
        fromSql.append("  ref_book_record r\n");

        StringBuilder sql = new StringBuilder();
        sql.append("select\n");
        sql.append("  r.id as \"");
        sql.append(RefBook.RECORD_ID_ALIAS);
        sql.append("\",\n");
        List<RefBookAttribute> attributes = refBook.getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            String alias = attribute.getAlias();
            sql.append("  a");
            sql.append(alias);
            sql.append(".");
            sql.append(attribute.getAttributeType().toString());
            sql.append("_value as \"");
            sql.append(alias);
            sql.append("\"");
            if (i < attributes.size() - 1) {
                sql.append(",\n");
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
        sql.append(fromSql);
        sql.append("where\n  r.id = ");
        sql.append(recordId);
        sql.append(" and\n");
        sql.append("  r.ref_book_id =");
        sql.append(refBookId);
        return sql.toString();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return null; //TODO: не реализовано (Marat Fayzullin 2013-07-10)
    }

    private static final String INSERT_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, record_id, ref_book_id, version," +
            "status) values (?, ?, %d, to_date('%s', 'DD.MM.YYYY'), %d)";
    private static final String INSERT_REF_BOOK_VALUE = "insert into ref_book_value (record_id, attribute_id," +
            "string_value, number_value, date_value, reference_value) values (?, ?, ?, ?, ?, ?)";

    @Override
    public void createRecordVersion(Long refBookId, Date version, VersionedObjectStatus status, final List<RefBookRecord> records) {
        List<Object[]> listValues = new ArrayList<Object[]>();

        if (records == null || records.isEmpty()) {
            return;
        }

        RefBook refBook = get(refBookId);

        final List<Long> refBookRecordIds  = dbUtils.getNextRefBookRecordIds((long) records.size());
        BatchPreparedStatementSetter batchRefBookRecordsPS = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, refBookRecordIds.get(i));
                ps.setLong(2, records.get(i).getRecordId());
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
        JdbcTemplate jt = getJdbcTemplate();
        jt.batchUpdate(String.format(INSERT_REF_BOOK_RECORD_SQL,
                refBookId,
                sdf.format(version),
                status.getId()
        ), batchRefBookRecordsPS);
        jt.batchUpdate(INSERT_REF_BOOK_VALUE, listValues);
    }

    private static final String INSERT_FAKE_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, record_id, ref_book_id, version," +
            "status) values (seq_ref_book_record.nextval, ?, ?, to_date('%s', 'DD.MM.YYYY'), 2)";

    @Override
    public void createFakeRecordVersion(Long refBookId, Long recordId, Date version) {
        getJdbcTemplate().update(String.format(INSERT_FAKE_REF_BOOK_RECORD_SQL, sdf.format(version)),
                recordId, refBookId);
    }

    private static final String DELETE_REF_BOOK_VALUE_SQL = "delete from ref_book_value where record_id = ?";

    @Override
    public void updateRecordVersion(Long refBookId, Long uniqueRecordId, Map<String, RefBookValue> records) {
        if (uniqueRecordId == null || records == null) {
            throw new IllegalArgumentException("uniqueRecordId: " + uniqueRecordId + "; records: " + records);
        }
        try {
            // нет данных - нет работы
            if (records.size() == 0) {
                return;
            }
            RefBook refBook = get(refBookId);
            List<Object[]> listValues = new ArrayList<Object[]>();

            for (Map.Entry<String, RefBookValue> entry : records.entrySet()) {
                String attributeAlias = entry.getKey();
                if (RefBook.RECORD_ID_ALIAS.equals(attributeAlias) ||
                        RefBook.RECORD_PARENT_ID_ALIAS.equals(attributeAlias)) {
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

            JdbcTemplate jt = getJdbcTemplate();
            //Удаляем старые значения атрибутов
            jt.update(DELETE_REF_BOOK_VALUE_SQL, uniqueRecordId);

            //Создаем новые значения атрибутов
            jt.batchUpdate(INSERT_REF_BOOK_VALUE, listValues);
        }
        catch (Exception ex) {
            throw new DaoException("Не удалось обновить значения справочника", ex);
        }
    }

    @Override
    public boolean isVersionsExist(Long refBookId, List<Long> recordIds, Date version) {
        String sql = "select count(*) from ref_book_record where ref_book_id = ? and record_id in %s and version = trunc(?, 'DD')";
        return getJdbcTemplate().queryForInt(String.format(sql, SqlUtils.transformToSqlInStatement(recordIds)), refBookId, version) != 0;
    }

    private static final String CHECK_REF_BOOK_RECORD_UNIQUE_SQL = "select id from ref_book_record " +
            "where ref_book_id = ? and version = trunc(?, 'DD') and record_id = ?";


    private void checkFillRequiredFields(Map<String, RefBookValue> record, RefBook refBook){
        List<RefBookAttribute> attributes = refBook.getAttributes();
        List<String> errors = refBookUtils.checkFillRequiredRefBookAtributes(attributes, record);

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
		if (refBookId == null || version == null || rowId == null) {
			throw new IllegalArgumentException("refBookId: " + refBookId + "; version: " + version + "; rowId: " + rowId);
		}
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
        String errStr = "Не найдено значение атрибута справочника с id = %d, соответствующее записи с id = %d";

        if (recordId == null || attributeId == null) {
            throw new DaoException(String.format(errStr, attributeId, recordId));
        }
        RefBook rb = getByAttribute(attributeId);
        if (rb == null) {
            throw new DaoException(String.format(errStr, attributeId, recordId));
        }
        //TODO: лучше одним запросом получать значение и тип атрибута (Marat Fayzullin 2013-08-06)
        RefBookAttribute attribute = rb.getAttribute(attributeId);
        if (attribute == null) {
            throw new DaoException(String.format(errStr, attributeId, recordId));
        }

        String q1 = "select";
        String q2 = "from ref_book_value where record_id = ? and attribute_id = ?";
        String q3 = null;
        Class cs = null;

        switch (attribute.getAttributeType()) {
            case STRING:
                q3 = " string_value ";
                cs = String.class;
                break;
            case NUMBER:
                q3 = " number_value ";
                cs = Number.class;
                break;
            case DATE:
                q3 = " date_value ";
                cs = Date.class;
                break;
            case REFERENCE:
                q3 = " reference_value ";
                cs = Long.class;
                break;
        }
        if (q3 == null) {
            throw new DaoException(String.format(errStr, attributeId, recordId));
        }
        try {
            return new RefBookValue(attribute.getAttributeType(), getJdbcTemplate().queryForObject(q1 + q3 + q2,
                    new Object[]{recordId, attributeId}, cs));
        } catch (EmptyResultDataAccessException ex) {
            throw new DaoException(String.format(errStr, attributeId, recordId));
        }
    }

    private static final String GET_RECORD_VERSION = "with currentRecord as (select r.id, r.ref_book_id, r.record_id, r.version from ref_book_record r where r.id=?),\n" +
            "nextVersion as (select min(r.version) as version from ref_book_record r, currentRecord cr where r.version > cr.version and r.record_id=cr.record_id and r.ref_book_id=cr.ref_book_id)\n" +
            "select cr.id as %s, cr.version as versionStart, nv.version as versionEnd from currentRecord cr, nextVersion nv";

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        try {
            String sql = String.format(GET_RECORD_VERSION,
                    RefBook.RECORD_ID_ALIAS);
            return getJdbcTemplate().queryForObject(sql,
                    new Object[] {
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
                    "  ref_book_id = %d and\n" +
                    "version <= to_date('%s', 'DD.MM.YYYY')\n" +
                    "union\n" +
                    "select\n" +
                    "  version\n" +
                    "from\n" +
                    "  ref_book_record\n" +
                    "where\n" +
                    "  ref_book_id = %d and\n" +
                    "  version >= to_date('%s', 'DD.MM.YYYY') and\n" +
                    "  version <= to_date('%s', 'DD.MM.YYYY') and" +
                    "  status = 0\n" +
                    "group by\n" +
                    "  version\n" +
                    "order by\n" +
                    "  version";

    @Override
    public List<Date> getVersions(Long refBookId, Date startDate, Date endDate) {
        String sql = String.format(RECORD_VERSION, refBookId, sdf.format(startDate), refBookId, sdf.format(startDate), sdf.format(endDate));
        return getJdbcTemplate().query(sql, new RowMapper<Date>() {

            @Override
            public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getDate(1);
            }
        });
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
        getJdbcTemplate().query(String.format("select id, version from ref_book_record where id in %s",
                SqlUtils.transformToSqlInStatement(uniqueRecordIds)), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(rs.getLong("id"), rs.getDate("version"));
            }
        });
        return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long refBookId, Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PreparedStatementData ps = getRefBookSql(refBookId, uniqueRecordId, null, sortAttribute, filter, pagingParams, true);
        RefBook refBook = get(refBookId);
        refBook.getAttributes().add(RefBook.getVersionFromAttribute());
        refBook.getAttributes().add(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // Получение количества данных в справкочнике
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

    private static final String CHECK_CROSS_VERSIONS = "with allVersions as (select r.* from ref_book_record r where ref_book_id=? and record_id=? and (? is null or id=?)),\n" +
            "recordsByVersion as (select r.*, row_number() over(partition by r.record_id order by r.version) rn from ref_book_record r, allVersions av where r.id=av.id),\n" +
            "versionInfo as (select rv.rn NUM, rv.ID, rv.VERSION, rv.status, rv2.version - interval '1' day nextVersion,rv2.status nextStatus from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn)\n" +
            "select num, id, version, status, nextversion, nextstatus, \n" +
            "case\n" +
            "  when (status=0 and (\n" +
            "  \t(%s is null and (\n" +
            "  \t\t(nextversion is not null and nextversion > to_date('%s', 'DD.MM.YYYY')) or \n" +
            "\t\t(nextversion is null and version > to_date('%s', 'DD.MM.YYYY'))\n" +
            "  \t)) or (%s is not null and (\n" +
            "  \t\t(version < to_date('%s', 'DD.MM.YYYY') and nextversion > to_date('%s', 'DD.MM.YYYY')) or \n" +
            "  \t\t(version > to_date('%s', 'DD.MM.YYYY') and version < to_date(%s, 'DD.MM.YYYY'))\n" +
            "  \t))\n" +
            "  )) then 1\n" +
            "  when (status=0 and nextversion is null and version < to_date('%s', 'DD.MM.YYYY')) then 2\n" +
            "  when (status=2 and (%s is not null and version > to_date('%s', 'DD.MM.YYYY') and version < to_date(%s, 'DD.MM.YYYY') and nextversion > to_date(%s, 'DD.MM.YYYY'))) then 3\n" +
            "  when (status=2 and (\n" +
            "  \t(nextversion is not null and %s is null and version > to_date('%s', 'DD.MM.YYYY')) or \n" +
            "  \t(nextversion is null and version > to_date('%s', 'DD.MM.YYYY'))\n" +
            "  )) then 4\n" +
            "  else 0\n" +
            "end as result\n" +
            "from versionInfo";

    @Override
    public List<CheckCrossVersionsResult> checkCrossVersions(Long refBookId, Long recordId, Date versionFrom, Date versionTo, Long excludedRecordId) {
        String sVersionFrom = sdf.format(versionFrom);
        String sVersionTo = versionTo != null ? "'"+sdf.format(versionTo)+"'" : null;
        String sql = String.format(CHECK_CROSS_VERSIONS,
                sVersionTo, sVersionFrom, sVersionFrom, sVersionTo, sVersionFrom, sVersionFrom, sVersionFrom, sVersionTo,
                sVersionFrom, sVersionTo, sVersionFrom, sVersionTo, sVersionTo, sVersionTo, sVersionFrom, sVersionFrom);

        return getJdbcTemplate().query(sql, new RowMapper<CheckCrossVersionsResult>() {
            @Override
            public CheckCrossVersionsResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                CheckCrossVersionsResult result = new CheckCrossVersionsResult();
                result.setNum(rs.getInt("NUM"));
                result.setRecordId(rs.getLong("ID"));
                result.setVersion(rs.getDate("VERSION"));
                result.setStatus(VersionedObjectStatus.getStatusById(rs.getInt("STATUS")));
                result.setNextVersion(rs.getDate("NEXTVERSION"));
                result.setNextStatus(VersionedObjectStatus.getStatusById(rs.getInt("NEXTSTATUS")));
                result.setResult(CrossResult.getResultById(rs.getInt("RESULT")));
                return result;
            }
        }, refBookId, recordId, excludedRecordId, excludedRecordId);
    }

    private final static String CHECK_UNIQUE_MATCHES = "select v.RECORD_ID as ID, a.NAME as NAME from REF_BOOK_VALUE v, REF_BOOK_RECORD r, REF_BOOK_ATTRIBUTE a \n" +
            "where r.ID = v.RECORD_ID and r.STATUS=0 and a.ID=v.ATTRIBUTE_ID and r.REF_BOOK_ID = ?";

    @Override
    public List<Pair<Long,String>> getMatchedRecordsByUniqueAttributes(Long refBookId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        boolean hasUniqueAttributes = false;
        List<RefBookValue> attributeValues = new ArrayList<RefBookValue>();
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(CHECK_UNIQUE_MATCHES);
        ps.addParam(refBookId);
        for (RefBookAttribute attribute : attributes) {
            if (attribute.isUnique()) {
                hasUniqueAttributes = true;
                ps.appendQuery(" and ");
                if (records.size() > 1) {
                    ps.appendQuery(" (");
                }
                for (int i=0; i < records.size(); i++) {
                    Map<String, RefBookValue> values = records.get(i).getValues();
                    ps.appendQuery("(v.ATTRIBUTE_ID = ?");
                    ps.addParam(attribute.getId());

                    ps.appendQuery(" and v.");
                    ps.appendQuery(attribute.getAttributeType() + "_VALUE");
                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        ps.appendQuery(" = '%s')");
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                        ps.appendQuery(" = %s)");
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                        ps.appendQuery(" = to_date('%s', 'DD.MM.YYYY'))");
                    }
                    attributeValues.add(values.get(attribute.getAlias()));

                    if (i < records.size() - 1) {
                        ps.appendQuery(" or ");
                    }
                }
                if (records.size() > 1) {
                    ps.appendQuery(")");
                }
            }
        }

        if (hasUniqueAttributes) {
            String sql = String.format(ps.getQuery().toString(), attributeValues.toArray());

            return getJdbcTemplate().query(sql, ps.getParams().toArray(), new RowMapper<Pair<Long, String>>() {
                @Override
                public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Pair<Long, String>(rs.getLong("ID"), rs.getString("NAME"));
                }
            });
        } else {
            return null;
        }
    }

    private final static String CHECK_REFERENCE_VERSIONS = "select count(*) from ref_book_record where VERSION < to_date('%s', 'DD.MM.YYYY') and ID in (%s)";

    @Override
    public boolean isReferenceValuesCorrect(Date versionFrom, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
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
                String sql = String.format(CHECK_REFERENCE_VERSIONS, sdf.format(versionFrom), in);
                return getJdbcTemplate().queryForInt(sql) == 0;
            }
        }
        return true;
    }

    private final static String CHECK_CONFLICT_VALUES_VERSIONS = "with conflictRecord as (select * from REF_BOOK_RECORD where ID in %s),\n" +
            "allRecordsInConflictGroup as (select r.* from REF_BOOK_RECORD r where exists (select 1 from conflictRecord cr where r.REF_BOOK_ID=cr.REF_BOOK_ID and r.RECORD_ID=cr.RECORD_ID)),\n" +
            "recordsByVersion as (select ar.*, row_number() over(partition by ar.RECORD_ID order by ar.version) rn from allRecordsInConflictGroup ar),\n" +
            "versionInfo as (select rv.ID, rv.VERSION versionFrom, rv2.version versionTo from conflictRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.ID=cr.ID)" +
            "select ID from versionInfo where (\n" +
            "\tversionTo IS NOT NULL and (versionFrom <= to_date('%s', 'DD.MM.YYYY') and versionTo >= to_date('%s', 'DD.MM.YYYY'))\n" +
            ") or (\n" +
            "\t\t(versionFrom >= to_date('%s', 'DD.MM.YYYY') and (%s IS NULL or versionFrom <= to_date('%s', 'DD.MM.YYYY')))\n" +
            ")";

    @Override
    public void checkConflictValuesVersions(List<Pair<Long,String>> recordPairs, Date versionFrom, Date versionTo) {
        List<Long> recordIds = new ArrayList<Long>();
        for (Pair<Long,String> pair : recordPairs) {
            recordIds.add(pair.getFirst());
        }

        String sVersionFrom = sdf.format(versionFrom);
        String sVersionTo = versionTo != null ? "'"+sdf.format(versionTo)+"'" : null;

        String sql = String.format(CHECK_CONFLICT_VALUES_VERSIONS,
                SqlUtils.transformToSqlInStatement(recordIds),
                sVersionFrom, sVersionFrom, sVersionFrom, sVersionTo, sVersionTo);
        List<Long> conflictedIds = getJdbcTemplate().queryForList(sql, Long.class);
        if (conflictedIds.size() > 0) {
            StringBuilder attrNames = new StringBuilder();
            for (Long id : conflictedIds) {
                for (Pair<Long,String> pair : recordPairs) {
                    if (pair.getFirst().equals(id)) {
                        attrNames.append("'").append(pair.getSecond()).append("',");
                    }
                }
            }
            attrNames.deleteCharAt(attrNames.length()-1);
            throw new DaoException("Нарушено требование к уникальности, уже существует элемент с такими значениями атрибута "+attrNames+" в указанном периоде!");
        }
    }

    @Override
    public void updateVersionRelevancePeriod(Long uniqueRecordId, Date version){
        String sql = String.format("update ref_book_record set version=to_date('%s', 'DD.MM.YYYY') where id=?", sdf.format(version));
        getJdbcTemplate().update(sql, uniqueRecordId);
    }

    @Override
    public void deleteVersion(Long uniqueRecordId) {
        getJdbcTemplate().update("delete from ref_book_record where id=?", uniqueRecordId);
    }

    @Override
    public boolean isVersionUsed(Long uniqueRecordId, Date versionFrom) {
        //TODO добавить проверки по другим точкам запросов
        //Проверка использования в справочниках и настройках подразделений
        String sql = String.format("select count(r.id) from ref_book_record r, ref_book_value v where r.id=v.record_id and r.version >= to_date('%s', 'DD.MM.YYYY') and v.REFERENCE_VALUE=?",
                sdf.format(versionFrom));
        return getJdbcTemplate().queryForInt(sql, uniqueRecordId) != 0;
    }

    private static final String CHECK_USAGES_IN_REFBOOK = "with checkRecords as (select * from ref_book_record where id in %s)\n" +
            "select count(r.id) from ref_book_record r, ref_book_value v, checkRecords cr where r.id=v.record_id and r.version >= cr.version and v.REFERENCE_VALUE=cr.id";

    private static final String CHECK_USAGES_IN_FORMS = "select count(*) from numeric_value where column_id in (select id from form_column where attribute_id in (select attribute_id from ref_book_value where record_id in %s)) and value in %s";

    @Override
    public boolean isVersionUsed(List<Long> uniqueRecordIds) {
        //Проверка использования в справочниках и настройках подразделений
        String in = SqlUtils.transformToSqlInStatement(uniqueRecordIds);
        String sql = String.format(CHECK_USAGES_IN_REFBOOK, in);
        boolean hasReferences = getJdbcTemplate().queryForInt(sql) != 0;
        if (!hasReferences) {
            sql = String.format(CHECK_USAGES_IN_FORMS, in, in);
            return getJdbcTemplate().queryForInt(sql) != 0;
        } else return true;
    }

    private static final String GET_NEXT_RECORD_VERSION = "with nextVersion as (select r.* from ref_book_record r where r.ref_book_id=? and r.record_id=? and r.status=0 and r.version > to_date('%s', 'DD.MM.YYYY')),\n" +
            "nextVersionEnd as (select min(r.version) as versionEnd from ref_book_record r, nextVersion nv where r.version > nv.version and r.record_id=nv.record_id and r.ref_book_id=nv.ref_book_id)\n" +
            "select nv.id as %s, nv.version as versionStart, nve.versionEnd from nextVersion nv, nextVersionEnd nve";

    @Override
    public RefBookRecordVersion getNextVersion(Long refBookId, Long recordId, Date versionFrom) {
        String sql = String.format(GET_NEXT_RECORD_VERSION,
                sdf.format(versionFrom), RefBook.RECORD_ID_ALIAS);
        try {
            return getJdbcTemplate().queryForObject(sql,
                    new Object[] {
                            refBookId, recordId
                    },
                    new RefBookUtils.RecordVersionMapper());
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

    private static final String DELETE_ALL_VERSIONS = "delete from ref_book_record where ref_book_id=? and record_id in (select record_id from ref_book_record where id in %s)";

    @Override
    public void deleteAllRecordVersions(Long refBookId, List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_ALL_VERSIONS, SqlUtils.transformToSqlInStatement(uniqueRecordIds));
        getJdbcTemplate().update(sql, refBookId);
    }

    private static final String DELETE_VERSION = "delete from ref_book_record where id in %s";

    @Override
    public void deleteRecordVersions(List<Long> uniqueRecordIds) {
        String sql = String.format(DELETE_VERSION, SqlUtils.transformToSqlInStatement(uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

    private static final String GET_RELATED_VERSIONS = "with currentRecord as (select id, record_id, ref_book_id from REF_BOOK_RECORD where id in %s),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, STATUS, VERSION, row_number() over(partition by r.RECORD_ID order by r.version) rn from REF_BOOK_RECORD r, currentRecord cr where r.ref_book_id=cr.ref_book_id and r.record_id=cr.record_id) \n" +
            "select rv2.ID from currentRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where cr.id=rv.id and rv2.status=%d";

    @Override
    public List<Long> getRelatedVersions(List<Long> uniqueRecordIds) {
        try {
            String sql = String.format(GET_RELATED_VERSIONS,
                    SqlUtils.transformToSqlInStatement(uniqueRecordIds), VersionedObjectStatus.FAKE.getId());
            return getJdbcTemplate().queryForList(sql, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    /**
     *
     * dloshkarev: Секция со старыми методами, оставленными для совместимости
     *
     * */

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
        final List<Long> refBookRecordIds  = dbUtils.getNextRefBookRecordIds(Long.valueOf(records.size()));

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
	 * @param refBookId код справочника, чьи данные необходимо записать
	 * @param records данные элементов справочника для преобразования
	 * @param refBookRecordIds идентификаторы новых строк
	 * @return данные в формате batchUpdate
	 */
	private List<Object[]> getListValuesForBatch(Long refBookId, List<Map<String, RefBookValue>> records, final List<Long> refBookRecordIds) {
		RefBook refBook = get(refBookId);
		List<Object[]> listValues = new ArrayList<Object[]>();
		for (int i = 0; i < records.size(); i++) {
			// создаем строки справочника

			// записываем значения ячеек
			Map<String, RefBookValue> record = records.get(i);
			List<RefBookAttribute> attributes = refBook.getAttributes();

			// проверка обязательности заполнения записей справочника
			List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(attributes, record);
			if (errors.size() > 0){
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


    private Long getRowId(Long recordId) {
        return getJdbcTemplate().queryForLong("select record_id from ref_book_record where id = ?", new Object[]{recordId});
    }

    @Override
    public void updateRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records) {
        if (refBookId == null || version == null || records == null) {
            throw new IllegalArgumentException("refBookId: " + refBookId + "; version: " + version + "; records: " + records);
        }
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
            List<Long> refBookRecordIds  = dbUtils.getNextRefBookRecordIds(Long.valueOf(needIdsCnt));
            Iterator<Long> idsIterator = refBookRecordIds.iterator();
            for (int i=0; i < recordAddIds.size(); i++){
                if (recordAddIds.get(i)[0] == null){
                    recordAddIds.set(i, new Object[]{idsIterator.next(), recordAddIds.get(i)[1]});
                }
            }

            Iterator<Long> iterator = refBookRecordIds.iterator();
            for (int i=0; i < recordsId.size(); i++){
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
        }
        catch (DaoException ex) {
            throw ex;
        }
        catch (Exception ex) {
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
                insertValues.add(new Object[] {rowId});
            } else {
                deleteValues.add(new Object[] {id});
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
                new Object[] {version, refBookId, version, refBookId},
                new int[] { Types.TIMESTAMP, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC });
    }
}