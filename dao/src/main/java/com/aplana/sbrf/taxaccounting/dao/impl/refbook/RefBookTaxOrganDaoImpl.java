package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookTaxOrganDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RefBookTaxOrganDaoImpl extends AbstractDao implements RefBookTaxOrganDao {

	private static final Log LOG = LogFactory.getLog(RefBookTaxOrganDaoImpl.class);

    private final static Long REF_BOOK_ID_200 = 200L;
    private final static String CODE = "t200.TAX_ORGAN_CODE";
    private final static String KPP = "t200.KPP";

    private final Map<Long, String> refBookNameMapping = new HashMap() {{
        put(REF_BOOK_CODE_ID, CODE);
        put(REF_BOOK_KPP_ID, KPP);
    }};

    @Autowired
    RefBookDao refBookDao;

    /**
     * Для получения данных из справочника.
     */
    private static final String SQL_REF_BOOK =
            "select \n" +
                    "    r.id as record_id \n" +
                    "    %s \n" +
                    "from \n" +
                    "    ref_book_record r \n" +
                    "    %s \n" +
                    "where \n" +
                    "    r.ref_book_id = %d and status = 0";

    private static final String SQL =
            " with \n" +
                    " t200 as ( \n" +
                    "     %s \n" +
                    " ) \n" +
                    " \n" +
                    "     select \n" +
                    "         max(t200.record_id) as record_id, \n" +
                    "         %s \n" +
                    "     from t200 \n" +
                    "     %s \n" +
                    "     group by %s \n";

    private static final String SQL_FOR_RECORD =
            " with \n" +
                    " t200 as ( \n" +
                    "     %s \n" +
                    " ) \n" +
                    " \n" +
                    "     select \n" +
                    "         max(t200.record_id) as record_id, \n" +
                    "         %s \n" +
                    "     from t200 \n" +
                    "     where periods.record_id = ?\n";

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId) {
        return getRecords(refBookId, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String filter) {
        PreparedStatementData ps = getRefBookSql(filter, refBookNameMapping.get(refBookId));

        RefBook refBook = refBookDao.get(refBookId);

        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);

        // Получение количества данных в справкочнике
        result.setTotalCount(getRecordsCount(refBookId, filter));

        return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID_200);
        PreparedStatementData ps = new PreparedStatementData();

        String sql = String.format(SQL_FOR_RECORD, getRefBookSelect(REF_BOOK_ID_200), refBookNameMapping.get(refBookId));
        ps.appendQuery(sql);
        ps.addParam(recordId);

        try {
            List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
            return records.isEmpty() ? new HashMap<String, RefBookValue>(0) : records.get(0);
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public int getRecordsCount(Long refBookId, String filter) {
        PreparedStatementData ps = getRefBookSql(filter, refBookNameMapping.get(refBookId));
        ps.setQuery(new StringBuilder("SELECT count(*) FROM (" + ps.getQuery().toString() + ")"));
        return getJdbcTemplate().queryForInt(ps.getQuery().toString(), ps.getParams().toArray());
    }

    /**
     * Динамически формирует запрос для справочника.
     */
    private PreparedStatementData getRefBookSql(String filter, String attribute) {
        // модель которая будет возвращаться как результат
        PreparedStatementData ps = new PreparedStatementData();

        String sqlRefBook200 = getRefBookSelect(REF_BOOK_ID_200);
        StringBuilder whereBlock = new StringBuilder("");
        if (filter != null && !filter.isEmpty()) {
            whereBlock.append("where ").append(filter);
        }
        String sql = String.format(SQL, sqlRefBook200, attribute, whereBlock, attribute);

        ps.appendQuery(sql);

        return ps;
    }

    /**
     * Получить запрос, возвращающий данные справочника.
     *
     * @param refBookId идентификатор справочника
     */
    private String getRefBookSelect(Long refBookId) {
        RefBook refBook = refBookDao.get(refBookId);
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
