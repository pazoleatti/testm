package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvOpsOmsDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvSvOpsOmsDaoImpl extends AbstractDao implements RaschsvSvOpsOmsDao {

    // Перечень столбцов таблицы "Сводные данные об обязательствах плательщика страховых взносов"
    private static final StringBuilder SV_OPS_OMS_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOms.COLUMNS, null));
    private static final StringBuilder SV_OPS_OMS_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOms.COLUMNS, ":"));

    public Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList) {
        String sql = "INSERT INTO " + RaschsvSvOpsOms.TABLE_NAME +
                " (" + SV_OPS_OMS_COLS + ") VALUES (" + SV_OPS_OMS_FIELDS + ")";

        // Генерация идентификаторов
        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            raschsvSvOpsOms.setId(generateId(RaschsvSvOpsOms.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsList.size());
        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOms.COL_ID, raschsvSvOpsOms.getId())
                            .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOms.getRaschsvObyazPlatSvId())
                            .addValue(RaschsvSvOpsOms.COL_TARIF_PLAT, raschsvSvOpsOms.getTarifPlat())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvOpsOmsList.size()]));

        return res.length;
    }
}
