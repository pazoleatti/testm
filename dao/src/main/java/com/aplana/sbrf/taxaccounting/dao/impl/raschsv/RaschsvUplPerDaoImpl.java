package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvUplPerDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvUplPerDaoImpl extends AbstractDao implements RaschsvUplPerDao {

    // Перечень столбцов таблицы "Персонифицированные сведения о застрахованных лицах"
    private static final StringBuilder UPL_PER_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplPer.COLUMNS, null));
    private static final StringBuilder UPL_PER_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplPer.COLUMNS, ":"));

    public Integer insertUplPer(List<RaschsvUplPer> raschsvUplPerList) {
        String sql = "INSERT INTO " + RaschsvUplPer.TABLE_NAME +
                " (" + UPL_PER_COLS + ") VALUES (" + UPL_PER_FIELDS + ")";

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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvUplPerList.size()]));

        return res.length;
    }
}
