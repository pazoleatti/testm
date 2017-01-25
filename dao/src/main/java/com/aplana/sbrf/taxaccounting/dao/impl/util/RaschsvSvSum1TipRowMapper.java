package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvSum1Tip;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Маппинг для СвСум1Тип
 */
public class RaschsvSvSum1TipRowMapper implements RowMapper<RaschsvSvSum1Tip> {

    @Override
    public RaschsvSvSum1Tip mapRow(ResultSet rs, int index) throws SQLException {
        RaschsvSvSum1Tip raschsvSvSum1Tip = new RaschsvSvSum1Tip();
        raschsvSvSum1Tip.setId(SqlUtils.getLong(rs, RaschsvSvSum1Tip.COL_ID));
        raschsvSvSum1Tip.setSumVsegoPer(rs.getBigDecimal(RaschsvSvSum1Tip.COL_SUM_VSEGO_PER));
        raschsvSvSum1Tip.setSumVsegoPosl3m(rs.getBigDecimal(RaschsvSvSum1Tip.COL_SUM_VSEGO_POSL_3M));
        raschsvSvSum1Tip.setSum1mPosl3m(rs.getBigDecimal(RaschsvSvSum1Tip.COL_SUM_1M_POSL_3M));
        raschsvSvSum1Tip.setSum2mPosl3m(rs.getBigDecimal(RaschsvSvSum1Tip.COL_SUM_2M_POSL_3M));
        raschsvSvSum1Tip.setSum3mPosl3m(rs.getBigDecimal(RaschsvSvSum1Tip.COL_SUM_3M_POSL_3M));

        return raschsvSvSum1Tip;
    }
}
