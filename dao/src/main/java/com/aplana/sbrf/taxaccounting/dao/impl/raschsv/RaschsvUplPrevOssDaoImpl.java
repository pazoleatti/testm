package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvUplPrevOssDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvUplPrevOssDaoImpl extends AbstractDao implements RaschsvUplPrevOssDao {

    // Перечень столбцов таблицы "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private static final StringBuilder UPL_PREV_OSS_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplPrevOss.COLUMNS, null));
    private static final StringBuilder UPL_PREV_OSS_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplPrevOss.COLUMNS, ":"));

    public Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss) {
        String sql = "INSERT INTO " + RaschsvUplPrevOss.TABLE_NAME +
                " (" + UPL_PREV_OSS_COLS + ") VALUES (" + UPL_PREV_OSS_FIELDS + ")";
        raschsvUplPrevOss.setId(generateId(RaschsvUplPrevOss.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvUplPrevOss.COL_ID, raschsvUplPrevOss.getId())
                .addValue(RaschsvUplPrevOss.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvUplPrevOss.getRaschsvObyazPlatSvId())
                .addValue(RaschsvUplPrevOss.COL_KBK, raschsvUplPrevOss.getKbk())
                .addValue(RaschsvUplPrevOss.COL_SUM_SB_UPL_PER, raschsvUplPrevOss.getSumSbUplPer())
                .addValue(RaschsvUplPrevOss.COL_SUM_SB_UPL_1M, raschsvUplPrevOss.getSumSbUpl1m())
                .addValue(RaschsvUplPrevOss.COL_SUM_SB_UPL_2M, raschsvUplPrevOss.getSumSbUpl2m())
                .addValue(RaschsvUplPrevOss.COL_SUM_SB_UPL_3M, raschsvUplPrevOss.getSumSbUpl3m())
                .addValue(RaschsvUplPrevOss.COL_PREV_RASH_SV_PER, raschsvUplPrevOss.getPrevRashSvPer())
                .addValue(RaschsvUplPrevOss.COL_PREV_RASH_SV_1M, raschsvUplPrevOss.getPrevRashSv1m())
                .addValue(RaschsvUplPrevOss.COL_PREV_RASH_SV_2M, raschsvUplPrevOss.getPrevRashSv2m())
                .addValue(RaschsvUplPrevOss.COL_PREV_RASH_SV_3M, raschsvUplPrevOss.getPrevRashSv3m());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvUplPrevOss.getId();
    }
}
