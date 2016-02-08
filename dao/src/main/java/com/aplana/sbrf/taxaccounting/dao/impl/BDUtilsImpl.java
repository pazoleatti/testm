package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс
 * @author auldanov
 */
@Repository
class BDUtilsImpl extends AbstractDao implements BDUtils {
    @Override
    public List<Long> getNextDataRowIds(Long count) {
        return getNextIds(Sequence.FORM_DATA_ROW, count);
    }

    @Override
    public List<Long> getNextRefBookRecordIds(Long count) {
        return getNextIds(Sequence.REF_BOOK_RECORD, count);
    }

    @Override
    public List<Long> getNextIds(Sequence sequence, Long count) {
        if (isSupportOver())
            return getJdbcTemplate().queryForList("select "+sequence.getName()+".nextval from dual connect by level<= ?", new Object[]{count}, java.lang.Long.class);
        else {
            ArrayList<Long> listIds = new ArrayList<Long>(count.intValue());
            for (Integer i = 0; i < count;i++)
                listIds.add(getJdbcTemplate().queryForLong("select "+sequence.getName()+".nextval from dual"));
            return listIds;
        }
    }
}
