package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBookerStatementPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RefBookBookerStatementPeriodDaoImpl extends AbstractDao implements RefBookBookerStatementPeriodDao {

	private static final Log LOG = LogFactory.getLog(RefBookBookerStatementPeriodDaoImpl.class);

    private static final Long REF_BOOK_ID_106 = 106L;
    private static final Long REF_BOOK_ID_107 = 107L;

    @Autowired
    RefBookDao refBookDao;

    /** Для получения данных из справочника. */
    private static final String SQL_REF_BOOK =
            "select \n" +
            "    r.id as record_id \n" +
            "    %s \n" +
            "from \n" +
            "    ref_book_record r \n" +
            "    %s \n" +
            "where \n" +
            "    r.ref_book_id = %d and status = 0";

    /** Для получения списка периода БО года и названия периода (без учета подразделения). */
    private static final String SQL =
            " --данные из 106 справочнка \n" +
            " with \n" +
            " t106 as ( \n" +
            "     %s \n" +
            " ), \n" +
            " \n" +
            " --данные из 107 справочника \n" +
            " t107 as ( \n" +
            "     %s \n" +
            " ) \n" +
            " \n" +
            " --данные для справочника 108 \n" +
            " select \n" +
            "     --id на запись справочника 107 \n" +
            "     periods.record_id, \n" +
            "     YEAR, \n" +
            "     ACCOUNT_PERIOD_ID, \n" +
            "     --год + название периода БО \n" +
            "     periods.YEAR || ': ' || t106.NAME as PERIOD_NAME \n" +
            " from ( \n" +
            "     --данные из 107 справочник сгруппированные по году и периоду без подразделения \n" +
            "     select \n" +
            "         max(t107.record_id) as record_id, \n" +
            "         t107.YEAR, \n" +
            "         t107.ACCOUNT_PERIOD_ID \n" +
            "     from t107 \n" +
            "     group by t107.YEAR, t107.ACCOUNT_PERIOD_ID \n" +
            " ) periods \n" +
            "     --для получения названия периода БО из справочник 106 \n" +
            "     left join t106 on t106.record_id = periods.ACCOUNT_PERIOD_ID \n" +
            " order by YEAR desc, ACCOUNT_PERIOD_ID \n";

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords() {
        Long refBookId = RefBookBookerStatementPeriodDao.REF_BOOK_ID;
        RefBook refBook = get(refBookId);

        PreparedStatementData ps = getRefBookSql();
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);

        // Получение количества данных в справкочнике
        ps.setQuery(new StringBuilder("SELECT count(*) FROM (" + ps.getQuery().toString() + ")"));
        result.setTotalCount(getJdbcTemplate().queryForInt(ps.getQuery().toString(), ps.getParams().toArray()));

        return result;
    }

    private static final String SQL_FOR_RECORD =
            " --данные из 106 справочнка \n" +
                    " with \n" +
                    " t106 as ( \n" +
                    "     %s \n" +
                    " ), \n" +
                    " \n" +
                    " --данные из 107 справочника \n" +
                    " t107 as ( \n" +
                    "     %s \n" +
                    " ) \n" +
                    " \n" +
                    " --данные для справочника 108 \n" +
                    " select \n" +
                    "     --id на запись справочника 107 \n" +
                    "     periods.record_id, \n" +
                    "     YEAR, \n" +
                    "     ACCOUNT_PERIOD_ID, \n" +
                    "     --год + название периода БО \n" +
                    "     periods.YEAR || ': ' || t106.NAME as PERIOD_NAME \n" +
                    " from ( \n" +
                    "     --данные из 107 справочник сгруппированные по году и периоду без подразделения \n" +
                    "     select \n" +
                    "         max(t107.record_id) as record_id, \n" +
                    "         t107.YEAR, \n" +
                    "         t107.ACCOUNT_PERIOD_ID \n" +
                    "     from t107 \n" +
                    "     group by t107.YEAR, t107.ACCOUNT_PERIOD_ID \n" +
                    " ) periods \n" +
                    "     --для получения названия периода БО из справочник 106 \n" +
                    "     left join t106 on t106.record_id = periods.ACCOUNT_PERIOD_ID \n" +
                    "       where periods.record_id = ?\n";

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        Long refBookId = RefBookBookerStatementPeriodDao.REF_BOOK_ID;
        RefBook refBook = get(refBookId);
        PreparedStatementData ps = new PreparedStatementData();
        String sqlRefBook106 = getRefBookSelect(REF_BOOK_ID_106);
        String sqlRefBook107 = getRefBookSelect(REF_BOOK_ID_107);

        String sql = String.format(SQL_FOR_RECORD, sqlRefBook106, sqlRefBook107);
        ps.appendQuery(sql);
        ps.addParam(recordId);

        try {
            List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
            return records.isEmpty()? new HashMap<String, RefBookValue>(0) : records.get(0);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    /** Динамически формирует запрос для справочника. */
    private PreparedStatementData getRefBookSql() {
        // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        String sqlRefBook106 = getRefBookSelect(REF_BOOK_ID_106);
        String sqlRefBook107 = getRefBookSelect(REF_BOOK_ID_107);

        String sql = String.format(SQL, sqlRefBook106, sqlRefBook107);
        ps.appendQuery(sql);

        return ps;
    }

    private RefBook get(Long refBookId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, script_id, visible, type, read_only, region_attribute_id, table_name from ref_book where id = ?",
                    new Object[]{refBookId}, new int[]{Types.NUMERIC},
                    new RefBookRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
        }
    }

    /**Настройка маппинга для справочника. */
    private class RefBookRowMapper implements RowMapper<RefBook> {
        @Override
        public RefBook mapRow(ResultSet rs, int index) throws SQLException {
            RefBook result = new RefBook();
            result.setId(SqlUtils.getLong(rs, "id"));
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

    private RefBookAttribute getAttribute(Long attributeId) {
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

    private List<RefBookAttribute> getAttributes(Long refBookId) {
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
        @Override
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

    /**
     * Получить запрос, возвращающий данные справочника.
     *
     * @param refBookId идентификатор справочника
     */
    private String getRefBookSelect(Long refBookId) {
        RefBook refBook = get(refBookId);
        List<RefBookAttribute> attributes = refBook.getAttributes();

        StringBuilder params = new StringBuilder();
        StringBuilder froms = new StringBuilder();

        for (RefBookAttribute attribute : attributes) {
            String alias = attribute.getAlias();
            params.append(", \n a");
            params.append(alias);
            params.append(".");
            params.append(attribute.getAttributeType().toString());
            params.append("_value as \"");
            params.append(alias);
            params.append("\"");

            froms.append("\n left join ref_book_value a");
            froms.append(alias);
            froms.append(" on a");
            froms.append(alias);
            froms.append(".record_id = r.id and a");
            froms.append(alias);
            froms.append(".attribute_id = ");
            froms.append(attribute.getId());
        }
        return String.format(SQL_REF_BOOK, params, froms, refBookId);
    }
}
