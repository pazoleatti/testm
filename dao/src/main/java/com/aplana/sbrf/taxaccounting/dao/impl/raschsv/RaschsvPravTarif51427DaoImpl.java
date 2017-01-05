package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif51427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvPravTarif51427DaoImpl extends AbstractDao implements RaschsvPravTarif51427Dao {

    // Перечень столбцов таблицы "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427"
    private static final StringBuilder PRAV_TARIF_51427_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif51427.COLUMNS, null));
    private static final StringBuilder PRAV_TARIF_51427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif51427.COLUMNS, ":"));

    public Long insertRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427) {
        String sql = "INSERT INTO " + RaschsvPravTarif51427.TABLE_NAME +
                " (" + PRAV_TARIF_51427_COLS + ") VALUES (" + PRAV_TARIF_51427_FIELDS + ")";

        raschsvPravTarif51427.setId(generateId(RaschsvPravTarif51427.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvPravTarif51427.COL_ID, raschsvPravTarif51427.getId())
                .addValue(RaschsvPravTarif51427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvPravTarif51427.getRaschsvObyazPlatSvId())
                .addValue(RaschsvPravTarif51427.COL_DOH346_15VS, raschsvPravTarif51427.getDoh346_15vs())
                .addValue(RaschsvPravTarif51427.COL_DOH6_427, raschsvPravTarif51427.getDoh6_427())
                .addValue(RaschsvPravTarif51427.COL_DOL_DOH6_427, raschsvPravTarif51427.getDolDoh6_427());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvPravTarif51427.getId();
    }
}
