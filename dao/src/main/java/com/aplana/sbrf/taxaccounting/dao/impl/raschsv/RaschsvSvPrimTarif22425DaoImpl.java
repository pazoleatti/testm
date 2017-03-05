package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvSvSum1TipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif22425Dao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvSvPrimTarif22425DaoImpl extends AbstractDao implements RaschsvSvPrimTarif22425Dao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    private static final String SVED_INO_GRAZD_PATENT = "g";
    private static final String SUM_ALIAS = "s";
    private static final String IT_ALIAS = "i";

    // Перечень столбцов таблицы СвПримТариф2.2.425
    private static final StringBuilder TARIF_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif22425.COLUMNS, null));
    private static final StringBuilder TARIF_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif22425.COLUMNS, ":"));

    // Перечень столбцов таблицы СвИноГражд
    private static final StringBuilder SVED_INO_GRAZD_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvInoGrazd.COLUMNS, null));
    private static final StringBuilder SVED_INO_GRAZD_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvInoGrazd.COLUMNS, ":"));

    // Перечень столбцов таблицы ВыплатИт
    private static final StringBuilder VYPLAT_IT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt425.COLUMNS, null));
    private static final StringBuilder VYPLAT_IT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt425.COLUMNS, ":"));

    // Перечень столбцов таблицы "СвСум1Тип"
    private static final String SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, SUM_ALIAS + ".");

    private static final String SQL_INSERT_TARIF = "INSERT INTO " + RaschsvSvPrimTarif22425.TABLE_NAME +
            " (" + TARIF_COLS + ") VALUES (" + TARIF_FIELDS + ")";

    private static final String SQL_INSERT_IT = "INSERT INTO " + RaschsvVyplatIt425.TABLE_NAME +
            " (" + VYPLAT_IT_COLS + ") VALUES (" + VYPLAT_IT_FIELDS + ")";

    private static final String SQL_INSERT_SV_INO_GRAZD = "INSERT INTO " + RaschsvSvInoGrazd.TABLE_NAME +
            " (" + SVED_INO_GRAZD_COLS + ") VALUES (" + SVED_INO_GRAZD_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + SqlUtils.getColumnsToString(RaschsvSvPrimTarif22425.COLUMNS, "pt.") +
            " FROM raschsv_sv_prim_tarif2_2_425 pt " +
            " INNER JOIN raschsv_obyaz_plat_sv ob ON pt.raschsv_obyaz_plat_sv_id = ob.id " +
            " WHERE ob.declaration_data_id = :declaration_data_id";

    private static final String SQL_SELECT_IT = "SELECT " + VYPLAT_IT_COLS + " FROM " + RaschsvVyplatIt425.TABLE_NAME +
            " WHERE " + RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID + " = :" + RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID;

    private static final String SQL_SELECT_SVED_INO_GRAZD = "SELECT " + SVED_INO_GRAZD_COLS + " FROM " + RaschsvSvInoGrazd.TABLE_NAME +
            " WHERE " + RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID + " = :" + RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID;

    private static final StringBuilder SQL_SELECT_SUM = new StringBuilder()
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvVyplatIt425.TABLE_NAME + " " + IT_ALIAS +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + IT_ALIAS + "." + RaschsvVyplatIt425.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append( " WHERE " + IT_ALIAS + "." + RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID + " = :" + RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID)
            .append(" UNION ALL ")
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvSvInoGrazd.TABLE_NAME + " " + SVED_INO_GRAZD_PATENT +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + SVED_INO_GRAZD_PATENT + "." + RaschsvSvInoGrazd.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append(" WHERE " + SVED_INO_GRAZD_PATENT + "." + RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID + " = :" + RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID);

    public Long insertRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425) {
        raschsvSvPrimTarif22425.setId(generateId(RaschsvSvPrimTarif22425.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvPrimTarif22425.COL_ID, raschsvSvPrimTarif22425.getId())
                .addValue(RaschsvSvPrimTarif22425.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvPrimTarif22425.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_TARIF.toString(), params);

        // Сохранение СвИноГражд
        List<RaschsvSvInoGrazd> raschsvSvInoGrazdList = new ArrayList<RaschsvSvInoGrazd>();
        for (RaschsvSvInoGrazd raschsvSvInoGrazd : raschsvSvPrimTarif22425.getRaschsvSvInoGrazdList()) {
            // Установка внешнего ключа
            raschsvSvInoGrazd.setRaschsvSvPrimTarif2425Id(raschsvSvPrimTarif22425.getId());

            // Сохранение СвСум1Тип
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvInoGrazd.getRaschsvSvSum1Tip());
            raschsvSvInoGrazd.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvSvInoGrazdList.add(raschsvSvInoGrazd);
        }
        insertRaschsvSvInoGrazd(raschsvSvInoGrazdList);

        // Установка внешнего ключа
        RaschsvVyplatIt425 raschsvVyplatIt425 = raschsvSvPrimTarif22425.getRaschsvVyplatIt425();
        raschsvVyplatIt425.setRaschsvSvPrimTarif22425Id(raschsvSvPrimTarif22425.getId());

        // Сохранение СвСум1Тип
        Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvVyplatIt425.getRaschsvSvSum1Tip());
        raschsvVyplatIt425.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

        // Сохранение ВыплатИт
        insertRaschsvVyplatIt425(raschsvVyplatIt425);

        return raschsvSvPrimTarif22425.getId();
    }

    /**
     * Сохранение СвИноГражд
     * @param raschsvSvInoGrazdList
     * @return
     */
    private Integer insertRaschsvSvInoGrazd(List<RaschsvSvInoGrazd> raschsvSvInoGrazdList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvInoGrazdList.size());
        for (RaschsvSvInoGrazd raschsvSvInoGrazd : raschsvSvInoGrazdList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID, raschsvSvInoGrazd.getRaschsvSvPrimTarif2425Id())
                            .addValue(RaschsvSvInoGrazd.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvInoGrazd.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvInoGrazd.COL_INNFL, raschsvSvInoGrazd.getInnfl())
                            .addValue(RaschsvSvInoGrazd.COL_SNILS, raschsvSvInoGrazd.getSnils())
                            .addValue(RaschsvSvInoGrazd.COL_GRAZD, raschsvSvInoGrazd.getGrazd())
                            .addValue(RaschsvSvInoGrazd.COL_FAMILIA, raschsvSvInoGrazd.getFamilia())
                            .addValue(RaschsvSvInoGrazd.COL_IMYA, raschsvSvInoGrazd.getImya())
                            .addValue(RaschsvSvInoGrazd.COL_OTCHESTVO, raschsvSvInoGrazd.getOtchestvo())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SV_INO_GRAZD,
                batchValues.toArray(new Map[raschsvSvInoGrazdList.size()]));

        return res.length;
    }

    /**
     * Сохранение ВыплатИт
     * @param raschsvVyplatIt425
     * @return
     */
    private Long insertRaschsvVyplatIt425(RaschsvVyplatIt425 raschsvVyplatIt425) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID, raschsvVyplatIt425.getRaschsvSvPrimTarif22425Id())
                .addValue(RaschsvVyplatIt425.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvVyplatIt425.getRaschsvSvSum1Tip().getId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_IT.toString(), params);

        return raschsvVyplatIt425.getRaschsvSvSum1Tip().getId();
    }

    @Override
    public RaschsvSvPrimTarif22425 findRaschsvSvPrimTarif22425(Long declarationDataId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID, declarationDataId);
            RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425 =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvSvPrimTarif22425RowMapper());

            // Выборка из СвСум1Тип
            List<RaschsvSvSum1Tip> raschsvSvSum1TipList = findRaschsvSvSum1Tip(raschsvSvPrimTarif22425.getId());
            Map<Long, RaschsvSvSum1Tip> mapRaschsvSvSum1Tip = new HashMap<Long, RaschsvSvSum1Tip>();
            for (RaschsvSvSum1Tip raschsvSvSum1Tip : raschsvSvSum1TipList) {
                mapRaschsvSvSum1Tip.put(raschsvSvSum1Tip.getId(), raschsvSvSum1Tip);
            }

            // Выборка из ВыплатИт
            RaschsvVyplatIt425 raschsvVyplatIt425 = findRaschsvVyplatIt425(raschsvSvPrimTarif22425.getId());
            raschsvVyplatIt425.setRaschsvSvSum1Tip(mapRaschsvSvSum1Tip.get(raschsvVyplatIt425.getRaschsvSvSum1TipId()));
            raschsvSvPrimTarif22425.setRaschsvVyplatIt425(raschsvVyplatIt425);

            // Выборка из СвИноГражд
            List<RaschsvSvInoGrazd> raschsvSvInoGrazdList = findRaschsvSvInoGrazd(raschsvSvPrimTarif22425.getId());
            for (RaschsvSvInoGrazd raschsvSvInoGrazd : raschsvSvInoGrazdList) {
                raschsvSvInoGrazd.setRaschsvSvSum1Tip(mapRaschsvSvSum1Tip.get(raschsvSvInoGrazd.getRaschsvSvSum1TipId()));
                raschsvSvPrimTarif22425.addRaschsvSvInoGrazd(raschsvSvInoGrazd);
            }
            return raschsvSvPrimTarif22425;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из ВыплатИт
     * @param raschsvSvPrimTarif22425Id
     * @return
     */
    private RaschsvVyplatIt425 findRaschsvVyplatIt425(Long raschsvSvPrimTarif22425Id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID, raschsvSvPrimTarif22425Id);
            return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_IT, params, new RaschsvVyplatIt425RowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из СвИноГражд
     * @param raschsvSvPrimTarif22425Id
     * @return
     */
    private List<RaschsvSvInoGrazd> findRaschsvSvInoGrazd(Long raschsvSvPrimTarif22425Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID, raschsvSvPrimTarif22425Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SVED_INO_GRAZD, params, new RaschsvSvInoGrazdRowMapper());
    }

    /**
     * Выборка из СвСум1Тип
     * @param raschsvSvPrimTarif22425Id
     * @return
     */
    private List<RaschsvSvSum1Tip> findRaschsvSvSum1Tip(Long raschsvSvPrimTarif22425Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID, raschsvSvPrimTarif22425Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SUM.toString(), params, new RaschsvSvSum1TipRowMapper());
    }

    /**
     * Маппинг для СвПримТариф2.2.425
     */
    private static final class RaschsvSvPrimTarif22425RowMapper implements RowMapper<RaschsvSvPrimTarif22425> {
        @Override
        public RaschsvSvPrimTarif22425 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425 = new RaschsvSvPrimTarif22425();
            raschsvSvPrimTarif22425.setId(SqlUtils.getLong(rs, RaschsvSvPrimTarif22425.COL_ID));
            raschsvSvPrimTarif22425.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvSvPrimTarif22425.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            return raschsvSvPrimTarif22425;
        }
    }

    /**
     * Маппинг для ВыплатИт
     */
    private static final class RaschsvVyplatIt425RowMapper implements RowMapper<RaschsvVyplatIt425> {
        @Override
        public RaschsvVyplatIt425 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplatIt425 raschsvVyplatIt425 = new RaschsvVyplatIt425();
            raschsvVyplatIt425.setRaschsvSvPrimTarif22425Id(SqlUtils.getLong(rs, RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID));
            raschsvVyplatIt425.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvVyplatIt425.COL_RASCHSV_SV_SUM1_TIP_ID));
            return raschsvVyplatIt425;
        }
    }

    /**
     * Маппинг для СвИноГражд
     */
    private static final class RaschsvSvInoGrazdRowMapper implements RowMapper<RaschsvSvInoGrazd> {
        @Override
        public RaschsvSvInoGrazd mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvInoGrazd raschsvSvInoGrazd = new RaschsvSvInoGrazd();
            raschsvSvInoGrazd.setRaschsvSvPrimTarif2425Id(SqlUtils.getLong(rs, RaschsvSvInoGrazd.COL_RASCHSV_SV_PRIM_TARIF2_425_ID));
            raschsvSvInoGrazd.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvSvInoGrazd.COL_RASCHSV_SV_SUM1_TIP_ID));
            raschsvSvInoGrazd.setInnfl(rs.getString(RaschsvSvInoGrazd.COL_INNFL));
            raschsvSvInoGrazd.setSnils(rs.getString(RaschsvSvInoGrazd.COL_SNILS));
            raschsvSvInoGrazd.setGrazd(rs.getString(RaschsvSvInoGrazd.COL_GRAZD));
            raschsvSvInoGrazd.setFamilia(rs.getString(RaschsvSvInoGrazd.COL_FAMILIA));
            raschsvSvInoGrazd.setImya(rs.getString(RaschsvSvInoGrazd.COL_IMYA));
            raschsvSvInoGrazd.setOtchestvo(rs.getString(RaschsvSvInoGrazd.COL_OTCHESTVO));
            return raschsvSvInoGrazd;
        }
    }
}
