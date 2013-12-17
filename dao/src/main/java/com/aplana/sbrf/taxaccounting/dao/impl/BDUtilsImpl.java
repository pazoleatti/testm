package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BDUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Утилитный класс
 * @author auldanov
 */
@Repository
@Transactional(readOnly = true)
class BDUtilsImpl extends AbstractDao implements BDUtils {
    @Override
    public List<Long> getNextIds(Long count) {
        return getJdbcTemplate().queryForList("select seq_data_row.nextval from dual connect by level<= ?", new Object[]{count}, java.lang.Long.class);
    }
}
