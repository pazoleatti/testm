package com.aplana.sbrf.taxaccounting.dao.script.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.refbook.RefBookOkatoDao;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Date;

/**
 * Справочник "ОКАТО"
 *
 * @author Dmitriy Levykin
 */
@Repository("refBookOkatoDao")
public class RefBookOkatoDaoImpl extends AbstractDao implements RefBookOkatoDao {

    private static final String CLEAR_PARENT_QUERY = "delete from ref_book_value where attribute_id = 6 " +
            "and record_id in (select id from ref_book_record where version = trunc(?, 'DD') and ref_book_id = 3 " +
            "and status <> -1)";

    @Override
    public void clearParentId(Date version) {
        if (version == null) {
            return;
        }
        getJdbcTemplate().update(CLEAR_PARENT_QUERY, new Object[]{ version},
                new int[]{Types.TIMESTAMP});
    }

    private static String UPDATE_PARENT_QUERY = "insert into ref_book_value (record_id, attribute_id, reference_value) " +
            "select t1.id, 6, t2.id " +
            "from " +
            "( " +
            "select r.id, r.version, v.STRING_VALUE, " +
            "case when SUBSTR(v.STRING_VALUE, 6) = '000000' then SUBSTR(v.STRING_VALUE, 1, 2) || '000000000' " +
            "when SUBSTR(v.STRING_VALUE, 9, 3) = '000' then SUBSTR(v.STRING_VALUE, 1, 5) || '000000' " +
            "else SUBSTR(v.STRING_VALUE, 1, 8) || '000' end as parent_code " +
            "from ref_book_record r, ref_book_value v " +
            "where r.ref_book_id = 3 " +
            "and r.version = trunc(?, 'DD') " +
            "and r.id = v.record_id and v.attribute_id = 7 " +
            "and length(v.string_value) = 11 " +
            "and r.status <> -1 " +
            "and substr(v.string_value, 3) <> '000000000' " +
            "order by v.STRING_VALUE " +
            ") t1, " +
            "( " +
            "with t as (select " +
            "max(version) version, record_id " +
            "from " +
            "ref_book_record " +
            "where " +
            "ref_book_id = 3 and status = 0 and version <= trunc(?, 'DD') " +
            "group by " +
            "record_id) " +
            "select " +
            "r.id, " +
            "val.string_value as okato " +
            "from " +
            "ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id) " +
            "left join ref_book_value val on val.record_id = r.id and val.attribute_id = 7 " +
            "where " +
            "r.ref_book_id = 3 and " +
            "status <> -1 " +
            ") t2 " +
            "where t1.parent_code = t2.okato";

    @Override
    public int updateParentId(Date version) {
        if (version == null) {
            return 0;
        }
        return getJdbcTemplate().update(UPDATE_PARENT_QUERY, new Object[]{version, version},
                new int[]{Types.TIMESTAMP, Types.TIMESTAMP});
    }
}
