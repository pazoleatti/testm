package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvUplPerDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvUplPerDaoImpl extends AbstractDao implements RaschsvUplPerDao {

    // Перечень столбцов таблицы "Персонифицированные сведения о застрахованных лицах"
    private static final String UPL_PER_COLS = SqlUtils.getColumnsToString(RaschsvUplPer.COLUMNS, null);
    private static final String UPL_PER_FIELDS = SqlUtils.getColumnsToString(RaschsvUplPer.COLUMNS, ":");

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvUplPer.TABLE_NAME +
            " (" + UPL_PER_COLS + ") VALUES (" + UPL_PER_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + UPL_PER_COLS + " FROM " + RaschsvUplPer.TABLE_NAME +
            " WHERE " + RaschsvUplPer.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvUplPer.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    public Integer insertUplPer(List<RaschsvUplPer> raschsvUplPerList) {
        // Генерация идентификаторов
        for (RaschsvUplPer raschsvUplPer : raschsvUplPerList) {
            raschsvUplPer.setId(generateId(RaschsvUplPer.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvUplPerList.size());
        for (RaschsvUplPer raschsvUplPer : raschsvUplPerList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvUplPer.COL_ID, raschsvUplPer.getId())
                            .addValue(RaschsvUplPer.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvUplPer.getRaschsvObyazPlatSvId())
                            .addValue(RaschsvUplPer.COL_NODE_NAME, raschsvUplPer.getNodeName())
                            .addValue(RaschsvUplPer.COL_KBK, raschsvUplPer.getKbk())
                            .addValue(RaschsvUplPer.COL_SUM_SB_UPL_PER, raschsvUplPer.getSumSbUplPer())
                            .addValue(RaschsvUplPer.COL_SUM_SB_UPL_1M, raschsvUplPer.getSumSbUpl1m())
                            .addValue(RaschsvUplPer.COL_SUM_SB_UPL_2M, raschsvUplPer.getSumSbUpl2m())
                            .addValue(RaschsvUplPer.COL_SUM_SB_UPL_3M, raschsvUplPer.getSumSbUpl3m())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT,
                batchValues.toArray(new Map[raschsvUplPerList.size()]));

        return res.length;
    }

    public List<RaschsvUplPer> findUplPer(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvUplPer.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);

        return getNamedParameterJdbcTemplate().query(SQL_SELECT, params, new RaschsvUplPerRowMapper());
    }

    /**
     * Маппинг для "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
     */
    private static final class RaschsvUplPerRowMapper implements RowMapper<RaschsvUplPer> {
        @Override
        public RaschsvUplPer mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvUplPer raschsvUplPer = new RaschsvUplPer();
            raschsvUplPer.setId(SqlUtils.getLong(rs, RaschsvUplPer.COL_ID));
            raschsvUplPer.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvUplPer.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            raschsvUplPer.setNodeName(rs.getString(RaschsvUplPer.COL_NODE_NAME));
            raschsvUplPer.setKbk(rs.getString(RaschsvUplPer.COL_KBK));
            raschsvUplPer.setSumSbUplPer(rs.getDouble(RaschsvUplPer.COL_SUM_SB_UPL_PER));
            raschsvUplPer.setSumSbUpl1m(rs.getDouble(RaschsvUplPer.COL_SUM_SB_UPL_1M));
            raschsvUplPer.setSumSbUpl2m(rs.getDouble(RaschsvUplPer.COL_SUM_SB_UPL_2M));
            raschsvUplPer.setSumSbUpl3m(rs.getDouble(RaschsvUplPer.COL_SUM_SB_UPL_3M));

            return raschsvUplPer;
        }
    }
}
