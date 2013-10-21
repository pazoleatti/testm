package com.aplana.sbrf.taxaccounting.dao.script.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.refbook.RefBookOkatoDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class RefBookOkatoDaoImpl extends AbstractDao implements RefBookOkatoDao {

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private static String CLEAR_PARENT_QUERY = "delete from ref_book_value where attribute_id = 6 " +
            "and record_id in (select id from ref_book_record where version = trunc(?, 'DD') and ref_book_id = 3)";

    @Override
    public void clearParentId(Date version) {
        if (version == null) {
            return;
        }
        getJdbcTemplate().update(CLEAR_PARENT_QUERY, new Object[]{ version},
                new int[]{Types.TIMESTAMP});
    }

    private static String UPDATE_PARENT_QUERY = "insert into ref_book_value (record_id, attribute_id, reference_value) " +
            "select t1.id, 6, v.record_id " +
            "from " +
            "(select r.id, " +
            "case " +
            "when SUBSTR(v.STRING_VALUE, 6) = '000000' then SUBSTR(v.STRING_VALUE, 1, 2) || '000000000' " +
            "when SUBSTR(v.STRING_VALUE, 9, 3) = '000' then SUBSTR(v.STRING_VALUE, 1, 5) || '000000' " +
            "else SUBSTR(v.STRING_VALUE, 1, 8) || '000' " +
            "end as parent_code " +
            "from ref_book_record r, ref_book_value v " +
            "where r.ref_book_id = 3 " +
            "and r.version = trunc(?, 'DD') " +
            "and r.id = v.record_id " +
            "and v.attribute_id = 7 " +
            "and length(v.string_value) = 11 " +
            "and substr(v.string_value, 3) <> '000000000') t1, " +
            "ref_book_value v, ref_book_record r " +
            "where v.attribute_id = 7 " +
            "and v.record_id = r.id " +
            "and r.version = trunc(?, 'DD') " +
            "and v.string_value = t1.parent_code";

    @Override
    public int updateParentId(Date version) {
        if (version == null) {
            return 0;
        }
        return getJdbcTemplate().update(UPDATE_PARENT_QUERY, new Object[]{version, version},
                new int[]{Types.TIMESTAMP, Types.TIMESTAMP});
    }

    private static String UPDATE_VALUES_QUERY = "update ref_book_value " +
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

        int[] result = getJdbcTemplate().batchUpdate(String.format(UPDATE_VALUES_QUERY, sdf.format(version)), list,
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
