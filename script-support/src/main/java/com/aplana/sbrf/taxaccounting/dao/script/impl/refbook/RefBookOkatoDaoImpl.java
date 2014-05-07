package com.aplana.sbrf.taxaccounting.dao.script.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.refbook.RefBookOkatoDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Справочник "ОКАТО"
 *
 * @author Dmitriy Levykin
 */
@Repository("refBookOkatoDao")
public class RefBookOkatoDaoImpl extends AbstractDao implements RefBookOkatoDao {

    private static final String UPDATE_VALUES_QUERY = "update ref_book_value " +
            "set string_value = ? " +
            "where attribute_id = 8 and record_id = (select r.id " +
            "from ref_book_record r, " +
            "ref_book_value v " +
            "where v.record_id = r.id " +
            "and r.version = trunc(?, 'DD') " +
            "and r.ref_book_id = 3 " +
            "and v.attribute_id = 7 " +
            "and v.string_value = ?)";

    @Override
    public List<Map<String, RefBookValue>> updateValueNames(final Date version, final List<Map<String, RefBookValue>> recordsList) {
        BatchPreparedStatementSetter preparedStatementSetter = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, recordsList.get(i).get("NAME").getStringValue());
                ps.setDate(2, new java.sql.Date(version.getTime()));
                ps.setString(3, recordsList.get(i).get("OKATO").getStringValue());
            }

            @Override
            public int getBatchSize() {
                return recordsList.size();
            }
        };

        int[] result = getJdbcTemplate().batchUpdate(
                UPDATE_VALUES_QUERY,
                preparedStatementSetter);

        List<Map<String, RefBookValue>> retList = new LinkedList<Map<String, RefBookValue>>();
        for (int i = 0; i < result.length; i++) {
            if (result[i] == 0) {
                // Не нашлось кода
                retList.add(recordsList.get(i));
            }
        }
        return retList;
    }
}
