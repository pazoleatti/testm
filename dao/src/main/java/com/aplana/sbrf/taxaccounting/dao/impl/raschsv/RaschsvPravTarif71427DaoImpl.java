package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif71427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class RaschsvPravTarif71427DaoImpl extends AbstractDao implements RaschsvPravTarif71427Dao {

    // Перечень столбцов таблицы ПравТариф7.1.427
    private static final StringBuilder PRAV_TARIF_71427_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif71427.COLUMNS, null));
    private static final StringBuilder PRAV_TARIF_71427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPravTarif71427.COLUMNS, ":"));

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvPravTarif71427.TABLE_NAME +
            " (" + PRAV_TARIF_71427_COLS + ") VALUES (" + PRAV_TARIF_71427_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + PRAV_TARIF_71427_COLS + " FROM " + RaschsvPravTarif71427.TABLE_NAME +
            " WHERE " + RaschsvPravTarif71427.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvPravTarif71427.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    public Long insertRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427) {
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
        getNamedParameterJdbcTemplate().update(SQL_INSERT.toString(), params);

        return raschsvPravTarif71427.getId();
    }

    public RaschsvPravTarif71427 findRaschsvPravTarif71427(Long obyazPlatSvId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvPravTarif71427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
            return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvPravTarif71427RowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Маппинг для ПравТариф7.1.427
     */
    private static final class RaschsvPravTarif71427RowMapper implements RowMapper<RaschsvPravTarif71427> {
        @Override
        public RaschsvPravTarif71427 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvPravTarif71427 raschsvPravTarif71427 = new RaschsvPravTarif71427();
            raschsvPravTarif71427.setId(SqlUtils.getLong(rs, RaschsvPravTarif71427.COL_ID));
            raschsvPravTarif71427.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvPravTarif71427.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            raschsvPravTarif71427.setDohVsPred(rs.getLong(RaschsvPravTarif71427.COL_DOH_VS_PRED));
            raschsvPravTarif71427.setDohVsPer(rs.getLong(RaschsvPravTarif71427.COL_DOH_VS_PER));
            raschsvPravTarif71427.setDohCelPostPred(rs.getLong(RaschsvPravTarif71427.COL_DOH_CEL_POST_PRED));
            raschsvPravTarif71427.setDohCelPostPer(rs.getLong(RaschsvPravTarif71427.COL_DOH_CEL_POST_PER));
            raschsvPravTarif71427.setDohGrantPred(rs.getLong(RaschsvPravTarif71427.COL_DOH_GRANT_PRED));
            raschsvPravTarif71427.setDohGrantPer(rs.getLong(RaschsvPravTarif71427.COL_DOH_GRANT_PER));
            raschsvPravTarif71427.setDohEkDeyatPred(rs.getLong(RaschsvPravTarif71427.COL_DOH_EK_DEYAT_PRED));
            raschsvPravTarif71427.setDohEkDeyatPer(rs.getLong(RaschsvPravTarif71427.COL_DOH_EK_DEYAT_PER));
            raschsvPravTarif71427.setDolDohPred(rs.getDouble(RaschsvPravTarif71427.COL_DOL_DOH_PRED));
            raschsvPravTarif71427.setDolDohPer(rs.getDouble(RaschsvPravTarif71427.COL_DOL_DOH_PER));

            return raschsvPravTarif71427;
        }
    }
}
