package com.aplana.sbrf.taxaccounting.dao.script.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.refbook.RefBookOkatoDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.text.SimpleDateFormat;
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

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");

    private static final String UPDATE_VALUES_QUERY = "update ref_book_value " +
            "set string_value = ? " +
            "where attribute_id = 8 and record_id = (select r.id " +
            "from ref_book_record r, " +
            "ref_book_value v " +
            "where v.record_id = r.id " +
            "and r.version = to_date('%s', 'DD.MM.YYYY') " +
            "and r.ref_book_id = 3 " +
            "and v.attribute_id = 7 " +
            "and v.string_value = ?)";

    @Override
    public List<Map<String, RefBookValue>> updateValueNames(Date version, List<Map<String, RefBookValue>> recordsList) {
        List<Object[]> list = new LinkedList<Object[]>();
        for (Map<String, RefBookValue> rec : recordsList) {
            list.add(new Object[]{rec.get("NAME").getStringValue(), rec.get("OKATO").getStringValue()});
        }

        int[] result = getJdbcTemplate().batchUpdate(String.format(UPDATE_VALUES_QUERY, SDF.format(version)), list,
                new int[]{Types.VARCHAR, Types.VARCHAR});

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
