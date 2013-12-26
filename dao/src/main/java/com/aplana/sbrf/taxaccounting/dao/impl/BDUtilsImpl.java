package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BDUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Утилитный класс
 * @author auldanov
 */
@Repository
class BDUtilsImpl extends AbstractDao implements BDUtils {
    @Override
    public List<Long> getNextDataRowIds(Long count) {
        return getNextIds("seq_data_row", count);
    }

    @Override
    public List<Long> getNextIds(String seqName, Long count) {
        return getJdbcTemplate().queryForList("select "+seqName+".nextval from dual connect by level<= ?", new Object[]{count}, java.lang.Long.class);
    }
}
