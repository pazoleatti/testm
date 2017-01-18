package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvKolLicTip;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Маппинг для КолЛицТип
 */
public class RaschsvKolLicTipRowMapper implements RowMapper<RaschsvKolLicTip> {
    @Override
    public RaschsvKolLicTip mapRow(ResultSet rs, int index) throws SQLException {
        RaschsvKolLicTip raschsvKolLicTip = new RaschsvKolLicTip();
        raschsvKolLicTip.setId(SqlUtils.getLong(rs, RaschsvKolLicTip.COL_ID));
        raschsvKolLicTip.setKolVsegoPer(rs.getInt(RaschsvKolLicTip.COL_KOL_VSEGO_PER));
        raschsvKolLicTip.setKolVsegoPosl3m(rs.getInt(RaschsvKolLicTip.COL_KOL_VSEGO_POSL_3M));
        raschsvKolLicTip.setKol3mPosl3m(rs.getInt(RaschsvKolLicTip.COL_KOL_1M_POSL_3M));
        raschsvKolLicTip.setKol2mPosl3m(rs.getInt(RaschsvKolLicTip.COL_KOL_2M_POSL_3M));
        raschsvKolLicTip.setKol3mPosl3m(rs.getInt(RaschsvKolLicTip.COL_KOL_3M_POSL_3M));

        return raschsvKolLicTip;
    }
}
