package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvUplPrevOssDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;
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
public class RaschsvUplPrevOssDaoImpl extends AbstractDao implements RaschsvUplPrevOssDao {

    // Перечень столбцов таблицы "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private static final StringBuilder UPL_PREV_OSS_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplPrevOss.COLUMNS, null));
    private static final StringBuilder UPL_PREV_OSS_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplPrevOss.COLUMNS, ":"));

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvUplPrevOss.TABLE_NAME +
            " (" + UPL_PREV_OSS_COLS + ") VALUES (" + UPL_PREV_OSS_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + UPL_PREV_OSS_COLS + " FROM " + RaschsvUplPrevOss.TABLE_NAME +
            " WHERE " + RaschsvUplPrevOss.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvUplPrevOss.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    public Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss) {
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
        getNamedParameterJdbcTemplate().update(SQL_INSERT.toString(), params);

        return raschsvUplPrevOss.getId();
    }

    public RaschsvUplPrevOss findUplPrevOss(Long obyazPlatSvId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvUplPrevOss.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
            return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvUplPrevOssRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Маппинг для "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     */
    private static final class RaschsvUplPrevOssRowMapper implements RowMapper<RaschsvUplPrevOss> {
        @Override
        public RaschsvUplPrevOss mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvUplPrevOss raschsvUplPrevOss = new RaschsvUplPrevOss();
            raschsvUplPrevOss.setId(SqlUtils.getLong(rs, RaschsvUplPrevOss.COL_ID));
            raschsvUplPrevOss.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvUplPrevOss.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            raschsvUplPrevOss.setSumSbUplPer(rs.getDouble(RaschsvUplPrevOss.COL_SUM_SB_UPL_PER));
            raschsvUplPrevOss.setKbk(rs.getString(RaschsvUplPrevOss.COL_KBK));
            raschsvUplPrevOss.setSumSbUpl1m(rs.getDouble(RaschsvUplPrevOss.COL_SUM_SB_UPL_1M));
            raschsvUplPrevOss.setSumSbUpl2m(rs.getDouble(RaschsvUplPrevOss.COL_SUM_SB_UPL_2M));
            raschsvUplPrevOss.setSumSbUpl3m(rs.getDouble(RaschsvUplPrevOss.COL_SUM_SB_UPL_3M));
            raschsvUplPrevOss.setPrevRashSvPer(rs.getDouble(RaschsvUplPrevOss.COL_PREV_RASH_SV_PER));
            raschsvUplPrevOss.setPrevRashSv1m(rs.getDouble(RaschsvUplPrevOss.COL_PREV_RASH_SV_1M));
            raschsvUplPrevOss.setPrevRashSv2m(rs.getDouble(RaschsvUplPrevOss.COL_PREV_RASH_SV_2M));
            raschsvUplPrevOss.setPrevRashSv3m(rs.getDouble(RaschsvUplPrevOss.COL_PREV_RASH_SV_3M));

            return raschsvUplPrevOss;
        }
    }
}
