package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.springframework.stereotype.Repository;

import javax.sound.midi.Sequence;
import java.util.List;

/**
 * Утилитный класс
 * @author auldanov
 */
@Repository
class BDUtilsImpl extends AbstractDao implements BDUtils {
    @Override
    public List<Long> getNextDataRowIds(Long count) {
        return getNextIds(Sequence.DATA_ROW, count);
    }

    @Override
    public List<Long> getNextRefBookRecordIds(Long count) {
        return getNextIds(Sequence.REF_BOOK_RECORD, count);
    }

    @Override
    public List<Long> getNextIds(Sequence sequence, Long count) {
        return getJdbcTemplate().queryForList("select "+sequence.getName()+".nextval from dual connect by level<= ?", new Object[]{count}, java.lang.Long.class);
    }
}
