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


    public static final Long OPERSTAT_ID = 1010L;
    public static final Long SOCRBASE_ID = 1020L;
    public static final Long ADDR_OBJECT_ID = 1030L;
    public static final Long HOUSE_ID = 1040L;
    public static final Long HOUSEINT_ID = 1050L;
    public static final Long ROOM_ID = 1060L;


    public static final String OPERSTAT_TABLE_NAME = "fias_operstat";
    public static final String SOCRBASE_TABLE_NAME = "fias_socrbase";
    public static final String ADDR_OBJECT_TABLE_NAME = "fias_addrobj";
    public static final String HOUSE_TABLE_NAME = "fias_house";
    public static final String HOUSEINT_TABLE_NAME = "fias_houseint";
    public static final String ROOM_TABLE_NAME = "fias_room";

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

    public void clearAll() {
        getJdbcTemplate().update("TRUNCATE TABLE fias_room");
        getJdbcTemplate().update("TRUNCATE TABLE fias_houseint");
        getJdbcTemplate().update("TRUNCATE TABLE fias_house");
        getJdbcTemplate().update("TRUNCATE TABLE fias_addrobj");
        getJdbcTemplate().update("TRUNCATE TABLE fias_socrbase");
        getJdbcTemplate().update("TRUNCATE TABLE fias_operstat");
    }

}
