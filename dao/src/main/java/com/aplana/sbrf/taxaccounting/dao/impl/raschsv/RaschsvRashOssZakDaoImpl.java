package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvRashOssZakDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZakRash;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvRashOssZakDaoImpl extends AbstractDao implements RaschsvRashOssZakDao {

    // Перечень столбцов РасхОССЗак
    private static final String OSS_ZAK_COLS = SqlUtils.getColumnsToString(RaschsvRashOssZak.COLUMNS, null);
    private static final String OSS_ZAK_FIELDS = SqlUtils.getColumnsToString(RaschsvRashOssZak.COLUMNS, ":");

    // Перечень столбцов "Расходы РасхОССЗак"
    private static final String OSS_ZAK_RASH_COLS = SqlUtils.getColumnsToString(RaschsvRashOssZakRash.COLUMNS, null);
    private static final String OSS_ZAK_RASH_FIELDS = SqlUtils.getColumnsToString(RaschsvRashOssZakRash.COLUMNS, ":");

    private static final String SQL_INSERT_OSS_ZAK = "INSERT INTO " + RaschsvRashOssZak.TABLE_NAME +
            " (" + OSS_ZAK_COLS + ") VALUES (" + OSS_ZAK_FIELDS + ")";

    private static final String SQL_INSERT_OSS_ZAK_RASH = "INSERT INTO " + RaschsvRashOssZakRash.TABLE_NAME +
            " (" + OSS_ZAK_RASH_COLS + ") VALUES (" + OSS_ZAK_RASH_FIELDS + ")";

    private static final String SQL_SELECT_OSS_ZAK = "SELECT " + OSS_ZAK_COLS + " FROM " + RaschsvRashOssZak.TABLE_NAME +
            " WHERE " + RaschsvRashOssZak.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvRashOssZak.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    private static final String SQL_SELECT_OSS_ZAK_RASH = "SELECT " + OSS_ZAK_RASH_COLS + " FROM " + RaschsvRashOssZakRash.TABLE_NAME +
            " WHERE " + RaschsvRashOssZakRash.COL_RASCHSV_RASH_OSS_ZAK_ID + " = :" + RaschsvRashOssZakRash.COL_RASCHSV_RASH_OSS_ZAK_ID;

    public Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak) {
        raschsvRashOssZak.setId(generateId(RaschsvRashOssZak.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvRashOssZak.COL_ID, raschsvRashOssZak.getId())
                .addValue(RaschsvRashOssZak.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvRashOssZak.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_OSS_ZAK.toString(), params);

        List<RaschsvRashOssZakRash> raschsvRashOssZakRashList = new ArrayList<RaschsvRashOssZakRash>();
        // Установка внешнего ключа для "Расходы РасхОССЗак"
        for (RaschsvRashOssZakRash raschsvRashOssZakRash : raschsvRashOssZak.getRaschsvRashOssZakRashList()) {
            raschsvRashOssZakRash.setRaschsvRashOssZakId(raschsvRashOssZak.getId());
            raschsvRashOssZakRashList.add(raschsvRashOssZakRash);
        }
        // Сохранение "Расходы РасхОССЗак"
        insertRaschsvRashOssZakRash(raschsvRashOssZakRashList);

        return raschsvRashOssZak.getId();
    }

    /**
     * Сохранение "Расходы РасхОССЗак"
     * @param raschsvRashOssZakRashList
     * @return
     */
    private Integer insertRaschsvRashOssZakRash(List<RaschsvRashOssZakRash> raschsvRashOssZakRashList) {
        // Генерация идентификаторов
        for (RaschsvRashOssZakRash raschsvRashOssZakRash : raschsvRashOssZakRashList) {
            raschsvRashOssZakRash.setId(generateId(RaschsvRashOssZakRash.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvRashOssZakRashList.size());
        for (RaschsvRashOssZakRash raschsvRashOssZakRash : raschsvRashOssZakRashList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvRashOssZakRash.COL_ID, raschsvRashOssZakRash.getId())
                            .addValue(RaschsvRashOssZakRash.COL_RASCHSV_RASH_OSS_ZAK_ID, raschsvRashOssZakRash.getRaschsvRashOssZakId())
                            .addValue(RaschsvRashOssZakRash.COL_NODE_NAME, raschsvRashOssZakRash.getNodeName())
                            .addValue(RaschsvRashOssZakRash.COL_CHISL_SLUCH, raschsvRashOssZakRash.getChislSluch())
                            .addValue(RaschsvRashOssZakRash.COL_KOL_VYPL, raschsvRashOssZakRash.getKolVypl())
                            .addValue(RaschsvRashOssZakRash.COL_PASH_VSEGO, raschsvRashOssZakRash.getPashVsego())
                            .addValue(RaschsvRashOssZakRash.COL_RASH_FIN_FB, raschsvRashOssZakRash.getRashFinFb())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_OSS_ZAK_RASH,
                batchValues.toArray(new Map[raschsvRashOssZakRashList.size()]));
        return res.length;
    }

    public RaschsvRashOssZak findRaschsvRashOssZak(Long obyazPlatSvId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvRashOssZak.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);

            // Выборка из РасхОССЗак
            RaschsvRashOssZak raschsvRashOssZak =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_OSS_ZAK, params, new RaschsvRashOssZakRowMapper());

            raschsvRashOssZak.setRaschsvRashOssZakRashList(findRaschsvRashOssZakRash(raschsvRashOssZak.getId()));

            return raschsvRashOssZak;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из "Расходы РасхОССЗак"
     * @param raschsvRashOssZakId
     * @return
     */
    private List<RaschsvRashOssZakRash> findRaschsvRashOssZakRash(Long raschsvRashOssZakId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvRashOssZakRash.COL_RASCHSV_RASH_OSS_ZAK_ID, raschsvRashOssZakId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_OSS_ZAK_RASH.toString(), params, new RaschsvRashOssZakRashRowMapper());
    }

    /**
     * Маппинг для РасхОССЗак
     */
    private static final class RaschsvRashOssZakRowMapper implements RowMapper<RaschsvRashOssZak> {
        @Override
        public RaschsvRashOssZak mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvRashOssZak raschsvRashOssZak = new RaschsvRashOssZak();
            raschsvRashOssZak.setId(SqlUtils.getLong(rs, RaschsvRashOssZak.COL_ID));
            raschsvRashOssZak.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvRashOssZak.COL_RASCHSV_OBYAZ_PLAT_SV_ID));

            return raschsvRashOssZak;
        }
    }

    /**
     * Маппинг для Расходы РасхОССЗак
     */
    private static final class RaschsvRashOssZakRashRowMapper implements RowMapper<RaschsvRashOssZakRash> {
        @Override
        public RaschsvRashOssZakRash mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvRashOssZakRash raschsvRashOssZakRash = new RaschsvRashOssZakRash();
            raschsvRashOssZakRash.setId(SqlUtils.getLong(rs, RaschsvRashOssZakRash.COL_ID));
            raschsvRashOssZakRash.setRaschsvRashOssZakId(SqlUtils.getLong(rs, RaschsvRashOssZakRash.COL_RASCHSV_RASH_OSS_ZAK_ID));
            raschsvRashOssZakRash.setNodeName(rs.getString(RaschsvRashOssZakRash.COL_NODE_NAME));
            raschsvRashOssZakRash.setChislSluch(rs.getInt(RaschsvRashOssZakRash.COL_CHISL_SLUCH));
            raschsvRashOssZakRash.setKolVypl(rs.getInt(RaschsvRashOssZakRash.COL_KOL_VYPL));
            raschsvRashOssZakRash.setPashVsego(rs.getDouble(RaschsvRashOssZakRash.COL_PASH_VSEGO));
            raschsvRashOssZakRash.setRashFinFb(rs.getDouble(RaschsvRashOssZakRash.COL_RASH_FIN_FB));

            return raschsvRashOssZakRash;
        }
    }
}
