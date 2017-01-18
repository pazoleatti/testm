package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif51427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class RaschsvPravTarif51427DaoImpl extends AbstractDao implements RaschsvPravTarif51427Dao {

    // Перечень столбцов таблицы ПравТариф5.1.427
    private static final StringBuilder PRAV_TARIF_51427_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif51427.COLUMNS, null));
    private static final StringBuilder PRAV_TARIF_51427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif51427.COLUMNS, ":"));

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvPravTarif51427.TABLE_NAME +
            " (" + PRAV_TARIF_51427_COLS + ") VALUES (" + PRAV_TARIF_51427_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + PRAV_TARIF_51427_COLS + " FROM " + RaschsvPravTarif51427.TABLE_NAME +
            " WHERE " + RaschsvPravTarif51427.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvPravTarif51427.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    public Long insertRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427) {
        raschsvPravTarif51427.setId(generateId(RaschsvPravTarif51427.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvPravTarif51427.COL_ID, raschsvPravTarif51427.getId())
                .addValue(RaschsvPravTarif51427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvPravTarif51427.getRaschsvObyazPlatSvId())
                .addValue(RaschsvPravTarif51427.COL_DOH346_15VS, raschsvPravTarif51427.getDoh346_15vs())
                .addValue(RaschsvPravTarif51427.COL_DOH6_427, raschsvPravTarif51427.getDoh6_427())
                .addValue(RaschsvPravTarif51427.COL_DOL_DOH6_427, raschsvPravTarif51427.getDolDoh6_427());
        getNamedParameterJdbcTemplate().update(SQL_INSERT.toString(), params);
        return raschsvPravTarif51427.getId();
    }

    public RaschsvPravTarif51427 findRaschsvPravTarif51427(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvPravTarif51427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvPravTarif51427RowMapper());
    }

    /**
     * Маппинг для ПравТариф5.1.427
     */
    private static final class RaschsvPravTarif51427RowMapper implements RowMapper<RaschsvPravTarif51427> {
        @Override
        public RaschsvPravTarif51427 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvPravTarif51427 raschsvPravTarif51427 = new RaschsvPravTarif51427();
            raschsvPravTarif51427.setId(SqlUtils.getLong(rs, RaschsvPravTarif51427.COL_ID));
            raschsvPravTarif51427.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvPravTarif51427.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            raschsvPravTarif51427.setDoh346_15vs(rs.getLong(RaschsvPravTarif51427.COL_DOH346_15VS));
            raschsvPravTarif51427.setDoh6_427(rs.getLong(RaschsvPravTarif51427.COL_DOH6_427));
            raschsvPravTarif51427.setDolDoh6_427(rs.getDouble(RaschsvPravTarif51427.COL_DOL_DOH6_427));

            return raschsvPravTarif51427;
        }
    }
}
