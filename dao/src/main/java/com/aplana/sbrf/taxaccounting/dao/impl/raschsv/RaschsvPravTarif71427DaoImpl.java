package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif71427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvPravTarif71427DaoImpl extends AbstractDao implements RaschsvPravTarif71427Dao {

    // Перечень столбцов таблицы "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
    private static final StringBuilder PRAV_TARIF_71427_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif71427.COLUMNS, null));
    private static final StringBuilder PRAV_TARIF_71427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif71427.COLUMNS, ":"));

    public Long insertRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427) {
        String sql = "INSERT INTO " + RaschsvPravTarif71427.TABLE_NAME +
                " (" + PRAV_TARIF_71427_COLS + ") VALUES (" + PRAV_TARIF_71427_FIELDS + ")";

        raschsvPravTarif71427.setId(generateId(RaschsvPravTarif71427.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvPravTarif71427.COL_ID, raschsvPravTarif71427.getId())
                .addValue(RaschsvPravTarif71427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvPravTarif71427.getRaschsvObyazPlatSvId())
                .addValue(RaschsvPravTarif71427.COL_DOH_VS_PRED, raschsvPravTarif71427.getDohVsPred())
                .addValue(RaschsvPravTarif71427.COL_DOH_VS_PER, raschsvPravTarif71427.getDohVsPer())
                .addValue(RaschsvPravTarif71427.COL_DOH_CEL_POST_PRED, raschsvPravTarif71427.getDohCelPostPred())
                .addValue(RaschsvPravTarif71427.COL_DOH_CEL_POST_PER, raschsvPravTarif71427.getDohCelPostPer())
                .addValue(RaschsvPravTarif71427.COL_DOH_GRANT_PRED, raschsvPravTarif71427.getDohGrantPred())
                .addValue(RaschsvPravTarif71427.COL_DOH_GRANT_PER, raschsvPravTarif71427.getDohGrantPer())
                .addValue(RaschsvPravTarif71427.COL_DOH_EK_DEYAT_PRED, raschsvPravTarif71427.getDohEkDeyatPred())
                .addValue(RaschsvPravTarif71427.COL_DOH_EK_DEYAT_PER, raschsvPravTarif71427.getDohEkDeyatPer())
                .addValue(RaschsvPravTarif71427.COL_DOL_DOH_PRED, raschsvPravTarif71427.getDolDohPred())
                .addValue(RaschsvPravTarif71427.COL_DOL_DOH_PER, raschsvPravTarif71427.getDolDohPer())
                ;
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvPravTarif71427.getId();
    }
}
