package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookTaxOrganDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class RefBookTaxOrganDaoImpl extends AbstractDao implements RefBookTaxOrganDao {

    private final Long REF_BOOK_ID_200 = 200L;
    private final String CODE = "t200.TAX_ORGAN_CODE";
    private final String KPP = "t200.KPP";

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

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsCode() {
        return getRecordsCode(null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsCode(String filter) {
        PreparedStatementData ps = getRefBookSql(filter, CODE);
        return getRecords(RefBookTaxOrganDao.REF_BOOK_CODE_ID, ps);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsKpp() {
        return getRecordsKpp(null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsKpp(String filter) {
        PreparedStatementData ps = getRefBookSql(filter, KPP);
        return getRecords(RefBookTaxOrganDao.REF_BOOK_KPP_ID, ps);
    }

    private PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, PreparedStatementData ps) {
        RefBook refBook = refBookDao.get(refBookId);

        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);

        // Получение количества данных в справкочнике
        ps.setQuery(new StringBuilder("SELECT count(*) FROM (" + ps.getQuery().toString() + ")"));
        result.setTotalCount(getJdbcTemplate().queryForInt(ps.getQuery().toString(), ps.getParams().toArray()));

        return result;
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
