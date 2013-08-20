package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.UniversalFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class RefBookDaoImpl extends AbstractDao implements RefBookDao {

    @Override
    public RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, script_id from ref_book where id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    @Override
    public List<RefBook> getAll() {
        return getJdbcTemplate().query(
                "select id, name, script_id from ref_book order by name",
                new RefBookRowMapper());
    }

    @Override
    public RefBook getByAttribute(long attributeId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select r.id, r.name, r.script_id from ref_book r join ref_book_attribute a on a.ref_book_id = r.id where a.id = ?",
                    new Object[]{attributeId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
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
            result.setAttributes(getAttributes(result.getId()));
            return result;
        }
    }

    /**
     * По коду справочника возвращает набор его атрибутов
     *
     * @param refBookId код справочника
     * @return набор атрибутов
     */
    private List<RefBookAttribute> getAttributes(Long refBookId) {
        try {
            return getJdbcTemplate().query(
                    "select id, name, alias, type, reference_id, attribute_id, visible, precision, width " +
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
            return result;
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute) {
        String sql = getRefBookSql(refBookId, version, sortAttribute, filter, pagingParams);
        RefBook refBook = get(refBookId);
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(sql, new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
        result.setRecords(records);
        sql = "SELECT count(*) FROM (" + getRefBookSql(refBookId, version, sortAttribute, filter, null) + ")";
        result.setTotalRecordCount(getJdbcTemplate().queryForInt(sql));
        return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        String sql = getRefBookRecordSql(refBookId, recordId);
        RefBook refBook = get(refBookId);
        return getJdbcTemplate().queryForObject(sql, new RefBookValueMapper(refBook));
    }

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private static final String WITH_STATEMENT =
            "with t as (select\n" +
                    "  max(version) version, record_id\n" +
                    "from\n" +
                    "  ref_book_record\n" +
                    "where\n" +
                    "  ref_book_id = %d and version <= to_date('%s', 'DD.MM.YYYY')\n" +
                    "group by\n" +
                    "  record_id)\n";

    /**
     * Динамически формирует запрос для справочника
     *
     * @param refBookId     код справочника
     * @param version       дата актуальности данных справочника
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param pagingParams
     * @return
     */
    private String getRefBookSql(Long refBookId, Date version, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams) {

        RefBook refBook = get(refBookId);

        /**
         * создаем StringBuffer для передачи в FilterTreeListener, псле обхода дерева
         * stringBuffer будет содержать строку с xml
         */
        StringBuffer stringBuffer = new StringBuffer();
        Filter.getFilterQuery(filter, new UniversalFilterTreeListener(refBook, stringBuffer));

        List<RefBookAttribute> attributes = refBook.getAttributes();

        if (sortAttribute != null && !attributes.contains(sortAttribute)) {
            throw new IllegalArgumentException(String.format("Reference book (id=%d) doesn't contains attribute \"%s\"",
                    refBookId, sortAttribute.getAlias()));
        }

        StringBuilder fromSql = new StringBuilder("\nfrom\n");
        fromSql.append("  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)\n");

        StringBuilder sql = new StringBuilder();
        sql.append(String.format(WITH_STATEMENT, refBookId, sdf.format(version)));
        sql.append("SELECT * FROM ");
        sql.append("(select\n");
        sql.append("  r.id as \"");
        sql.append(RefBook.RECORD_ID_ALIAS);
        sql.append("\",\n");
        if (isSupportOver() && sortAttribute != null) {
            // эту часть кода нельзя покрыть юнит тестами с использованием hsql потому что она не поддерживает row_number()
            sql.append("row_number()");
            // Надо делать сортировку
            sql.append(" over (order by \'");
            sql.append("a");
            sql.append(sortAttribute.getAlias());
            sql.append(".");
            sql.append(sortAttribute.getAttributeType().toString());
            sql.append("_value");
            sql.append("\')");
            sql.append(" as row_number_over,\n");
        } else {
            // База тестовая и не поддерживает row_number() значит сортировка работать не будет
            sql.append("rownum row_number_over,\n");
        }
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
        sql.append("where\n  r.ref_book_id = ");
        sql.append(refBookId);
        sql.append(" and\n  status <> -1\n");

        if (stringBuffer.length() > 0) {
            sql.append(" and\n ");
            sql.append(stringBuffer.toString());
            sql.append("\n");
        }
        sql.append(")");

        if (pagingParams != null) {
            sql.append(" where row_number_over between " + pagingParams.getStartIndex() + " and " + String.valueOf(pagingParams.getStartIndex() + pagingParams.getCount()));
        }

        return sql.toString();
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
                    "  version <= to_date('%s', 'DD.MM.YYYY')\n" +
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

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return null; //TODO: не реализовано (Marat Fayzullin 2013-07-10)
    }

    private static final String INSERT_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, ref_book_id, version," +
            "status, record_id) values (?, %d, to_date('%s', 'DD.MM.YYYY'), 0, seq_ref_book_record_row_id.nextval)";
    private static final String INSERT_REF_BOOK_VALUE = "insert into ref_book_value (record_id, attribute_id," +
            "string_value, number_value, date_value, reference_value) values (?, ?, ?, ?, ?, ?)";

    @Override
    public void createRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records) {
        // нет данных - нет работы
        if (records.size() == 0) {
            return;
        }

        RefBook refBook = get(refBookId);
        List<Object[]> recordIds = new ArrayList<Object[]>();
        List<Object[]> listValues = new ArrayList<Object[]>();

        int counter = 0;
        // Шаг последовательности = 100
        Long seq = generateId("seq_ref_book_record", Long.class);

        for (int i = 0; i < records.size(); i++) {
            // создаем строки справочника
            Long recordId = seq + counter++;

            if (counter >= 100) {
                counter = 0;
                seq = generateId("seq_ref_book_record", Long.class);
            }

            recordIds.add(new Object[]{recordId});
            // записываем значения ячеек
            Map<String, RefBookValue> record = records.get(i);
            for (Map.Entry<String, RefBookValue> entry : record.entrySet()) {
                String attributeAlias = entry.getKey();
                if (RefBook.RECORD_ID_ALIAS.equals(attributeAlias) ||
                        RefBook.RECORD_PARENT_ID_ALIAS.equals(attributeAlias)) {
                    continue;
                }
                RefBookAttribute attribute = refBook.getAttribute(attributeAlias);
                Object[] values = new Object[6];
                values[0] = recordId;
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
                        values[3] = entry.getValue().getNumberValue();
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
        jt.batchUpdate(String.format(INSERT_REF_BOOK_RECORD_SQL, refBookId, sdf.format(version)), recordIds);
        jt.batchUpdate(INSERT_REF_BOOK_VALUE, listValues);
    }

    private Long getRowId(Long recordId) {
        return getJdbcTemplate().queryForLong("select record_id from ref_book_record where id = ?", new Object[]{recordId});
    }

    private static final String UPDATE_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, ref_book_id, version," +
            "status, record_id) values (?, %d, to_date('%s', 'DD.MM.YYYY'), 0, ?)";

    private static final String DELETE_REF_BOOK_VALUE_SQL = "delete from ref_book_value where record_id in " +
            "(select id from ref_book_record where ref_book_id = ? and version = trunc(?, 'DD') and record_id = ?)";

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
            for (int i = 0; i < records.size(); i++) {
                Map<String, RefBookValue> record = records.get(i);
                // создаем строки справочника
                Long rowId = getRowId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());

                Long recordId = checkRecordUnique(refBookId, version, rowId);

                if (recordId == null) {
                    recordId = generateId("seq_ref_book_record", Long.class);
                    recordAddIds.add(new Object[]{recordId, rowId});
                } else {
                    delValues.add(new Object[]{refBookId, version, rowId});
                }

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
                            values[3] = entry.getValue().getNumberValue();
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
                jt.batchUpdate(DELETE_REF_BOOK_VALUE_SQL, delValues);
            }
            // + REF_BOOK_RECORD
            if (!recordAddIds.isEmpty()) {
                jt.batchUpdate(String.format(UPDATE_REF_BOOK_RECORD_SQL, refBookId, sdf.format(version)), recordAddIds);
            }
            // + REF_BOOK_VALUE
            jt.batchUpdate(INSERT_REF_BOOK_VALUE, listValues);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final String CHECK_REF_BOOK_RECORD_UNIQUE_SQL = "select id from ref_book_record " +
            "where ref_book_id = ? and version = trunc(?, 'DD') and record_id = ?";

    @Override
    public Long checkRecordUnique(Long refBookId, Date version, Long rowId) {
        try {
            return getJdbcTemplate().queryForLong(CHECK_REF_BOOK_RECORD_UNIQUE_SQL,
                    new Object[]{refBookId, version, rowId});
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private static final String DELETE_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, ref_book_id, version," +
            "status, record_id) values (seq_ref_book_record.nextval, %d, to_date('%s', 'DD.MM.YYYY'), -1, ?)";

    @Override
    public void deleteRecords(Long refBookId, Date version, List<Long> recordIds) {
        //TODO: возможно стоит добавить проверку, что запись еще не удалена (Marat Fayzullin 2013-07-26)
        // нет данных - нет работы
        if (recordIds.size() == 0) {
            return;
        }
        List<Object[]> values = new ArrayList<Object[]>();
        for (int i = 0; i < recordIds.size(); i++) {
            // создаем строки справочника
            Long rowId = getRowId(recordIds.get(i));
            values.add(new Object[]{rowId});
        }
        JdbcTemplate jt = getJdbcTemplate();
        jt.batchUpdate(String.format(DELETE_REF_BOOK_RECORD_SQL, refBookId, sdf.format(version)), values);
    }

    private static final String DELETE_MARK_ALL_REF_BOOK_RECORD_SQL = "insert into ref_book_record (id, ref_book_id, " +
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
        // Отметка записей ближайшей меньшей версии как удаленных
        getJdbcTemplate().update(DELETE_MARK_ALL_REF_BOOK_RECORD_SQL,
                new Object[] {version, refBookId, version, refBookId},
                new int[] { Types.TIMESTAMP, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC });
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
}