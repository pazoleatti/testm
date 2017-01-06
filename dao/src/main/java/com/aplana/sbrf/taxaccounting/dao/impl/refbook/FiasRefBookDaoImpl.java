package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class FiasRefBookDaoImpl extends AbstractDao implements FiasRefBookDao {

    @Override
    public void insertRecordsBatch(String tableName, List<Map<String, Object>> records) {
        if (records != null && !records.isEmpty()) {
            String[] columns = records.get(0).keySet().toArray(new String[]{});
            StringBuilder sqlStatement = new StringBuilder();
            sqlStatement.append("insert into ");
            sqlStatement.append(tableName);
            sqlStatement.append("(").append(SqlUtils.getColumnsToString(columns, null)).append(")");
            sqlStatement.append(" VALUES ");
            sqlStatement.append("(").append(SqlUtils.getColumnsToString(columns, ":")).append(")");
            getNamedParameterJdbcTemplate().batchUpdate(sqlStatement.toString(), records.toArray(new Map[records.size()]));
        }
    }

}
