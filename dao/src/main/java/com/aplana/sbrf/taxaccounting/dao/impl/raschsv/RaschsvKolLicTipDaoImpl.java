package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvKolLicTip;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvKolLicTipDaoImpl extends AbstractDao implements RaschsvKolLicTipDao {

    // Перечень столбцов таблицы КолЛицТип
    private static final StringBuilder KOL_LIC_TIP_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvKolLicTip.COLUMNS, null));
    private static final StringBuilder KOL_LIC_TIP_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvKolLicTip.COLUMNS, ":"));

    public Long insertRaschsvKolLicTip(RaschsvKolLicTip raschsvKolLicTip) {
        String sql = "INSERT INTO " + RaschsvKolLicTip.TABLE_NAME +
                " (" + KOL_LIC_TIP_COLS + ") VALUES (" + KOL_LIC_TIP_FIELDS + ")";
        raschsvKolLicTip.setId(generateId(RaschsvKolLicTip.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvKolLicTip.COL_ID, raschsvKolLicTip.getId())
                .addValue(RaschsvKolLicTip.COL_KOL_VSEGO_PER, raschsvKolLicTip.getKolVsegoPer())
                .addValue(RaschsvKolLicTip.COL_KOL_VSEGO_POSL_3M, raschsvKolLicTip.getKolVsegoPosl3m())
                .addValue(RaschsvKolLicTip.COL_KOL_1M_POSL_3M, raschsvKolLicTip.getKol1mPosl3m())
                .addValue(RaschsvKolLicTip.COL_KOL_2M_POSL_3M, raschsvKolLicTip.getKol2mPosl3m())
                .addValue(RaschsvKolLicTip.COL_KOL_3M_POSL_3M, raschsvKolLicTip.getKol3mPosl3m());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvKolLicTip.getId();
    }
}
