package com.aplana.sbrf.taxaccounting.dao.script.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.dictionary.RefBookOkatoDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.Date;

/**
 * Справочник "ОКАТО"
 *
 * @author Dmitriy Levykin
 */
@Repository("refBookOkatoDao")
@Transactional
public class RefBookOkatoDaoImpl extends AbstractDao implements RefBookOkatoDao {

    private static String UPDATE_QUERY = "insert into ref_book_value (record_id, attribute_id, reference_value) " +
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
        return getJdbcTemplate().update(UPDATE_QUERY, new Object[]{version, version}, new int[]{Types.TIMESTAMP, Types.TIMESTAMP});
    }
}
