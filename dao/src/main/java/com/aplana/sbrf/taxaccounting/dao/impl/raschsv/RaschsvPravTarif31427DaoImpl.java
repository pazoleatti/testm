package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif31427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif31427;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvPravTarif31427DaoImpl extends AbstractDao implements RaschsvPravTarif31427Dao {

    // Перечень столбцов таблицы "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427"
    private static final StringBuilder PRAV_TARIF_31427_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif31427.COLUMNS, null));
    private static final StringBuilder PRAV_TARIF_31427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif31427.COLUMNS, ":"));

    public Long insertRaschsvPravTarif31427(RaschsvPravTarif31427 raschsvPravTarif31427) {
        String sql = "INSERT INTO " + RaschsvPravTarif31427.TABLE_NAME +
                " (" + PRAV_TARIF_31427_COLS + ") VALUES (" + PRAV_TARIF_31427_FIELDS + ")";

        raschsvPravTarif31427.setId(generateId(RaschsvPravTarif31427.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvPravTarif31427.COL_ID, raschsvPravTarif31427.getId())
                .addValue(RaschsvPravTarif31427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvPravTarif31427.getRaschsvObyazPlatSvId())
                .addValue(RaschsvPravTarif31427.COL_SR_CHISL_9MPR, raschsvPravTarif31427.getSrChisl9mpr())
                .addValue(RaschsvPravTarif31427.COL_SR_CHISL_PER, raschsvPravTarif31427.getSrChislPer())
                .addValue(RaschsvPravTarif31427.COL_DOH248_9MPR, raschsvPravTarif31427.getDoh2489mpr())
                .addValue(RaschsvPravTarif31427.COL_DOH248_PER, raschsvPravTarif31427.getDoh248Per())
                .addValue(RaschsvPravTarif31427.COL_DOH_KR5_427_9MPR, raschsvPravTarif31427.getDohKr54279mpr())
                .addValue(RaschsvPravTarif31427.COL_DOH_KR5_427_PER, raschsvPravTarif31427.getDohKr5427Per())
                .addValue(RaschsvPravTarif31427.COL_DOH_DOH5_427_9MPR, raschsvPravTarif31427.getDohDoh54279mpr())
                .addValue(RaschsvPravTarif31427.COL_DOH_DOH5_427_PER, raschsvPravTarif31427.getDohDoh5427per())
                .addValue(RaschsvPravTarif31427.COL_DATA_ZAP_AK_ORG, raschsvPravTarif31427.getDataZapAkOrg())
                .addValue(RaschsvPravTarif31427.COL_NOM_ZAP_AK_ORG, raschsvPravTarif31427.getNomZapAkOrg())
                ;
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvPravTarif31427.getId();
    }
}
