package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvSum1Tip;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvSvSum1TipDaoImpl extends AbstractDao implements RaschsvSvSum1TipDao {

    // Перечень столбцов таблицы "Сведения по суммам (тип 1)"
    private static final StringBuilder SV_SUM_1TIP_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, null));
    private static final StringBuilder SV_SUM_1TIP_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, ":"));

    public Long insertRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        String sql = "INSERT INTO " + RaschsvSvSum1Tip.TABLE_NAME +
                " (" + SV_SUM_1TIP_COLS + ") VALUES (" + SV_SUM_1TIP_FIELDS + ")";
        raschsvSvSum1Tip.setId(generateId(RaschsvSvSum1Tip.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvSum1Tip.COL_ID, raschsvSvSum1Tip.getId())
                .addValue(RaschsvSvSum1Tip.COL_SUM_VSEGO_PER, raschsvSvSum1Tip.getSumVsegoPer())
                .addValue(RaschsvSvSum1Tip.COL_SUM_VSEGO_POSL_3M, raschsvSvSum1Tip.getSumVsegoPosl3m())
                .addValue(RaschsvSvSum1Tip.COL_SUM_1M_POSL_3M, raschsvSvSum1Tip.getSum1mPosl3m())
                .addValue(RaschsvSvSum1Tip.COL_SUM_2M_POSL_3M, raschsvSvSum1Tip.getSum2mPosl3m())
                .addValue(RaschsvSvSum1Tip.COL_SUM_3M_POSL_3M, raschsvSvSum1Tip.getSum3mPosl3m());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvSvSum1Tip.getId();
    }
}
