package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvKolLicTipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvSvSum1TipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvOssVnmDao;
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
public class RaschsvOssVnmDaoImpl extends AbstractDao implements RaschsvOssVnmDao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    @Autowired
    private RaschsvKolLicTipDao raschsvKolLicTipDao;

    private static final String OSS_VNM_ALIAS = "v";
    private static final String UPL_SV_PREV_ALIAS = "p";
    private static final String OSS_VNM_SUM_ALIAS = "vs";
    private static final String OSS_VNM_KOL_ALIAS = "vk";
    private static final String SUM_ALIAS = "s";
    private static final String KOL_ALIAS = "k";

    // Перечень столбцов таблицы РасчСВ_ОСС.ВНМ
    private static final String OSS_VNM_COLS = SqlUtils.getColumnsToString(RaschsvOssVnm.COLUMNS, null);
    private static final String OSS_VNM_FIELDS = SqlUtils.getColumnsToString(RaschsvOssVnm.COLUMNS, ":");

    // Перечень столбцов таблицы УплСВПрев
    private static final String UPL_SV_PREV_COLS = SqlUtils.getColumnsToString(RaschsvUplSvPrev.COLUMNS, null);
    private static final String UPL_SV_PREV_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvUplSvPrev.COLUMNS, UPL_SV_PREV_ALIAS + ".");
    private static final String UPL_SV_PREV_FIELDS = SqlUtils.getColumnsToString(RaschsvUplSvPrev.COLUMNS, ":");

    // Перечень столбцов таблицы "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
    private static final String OSS_VNM_SUM_COLS = SqlUtils.getColumnsToString(RaschsvOssVnmSum.COLUMNS, null);
    private static final String OSS_VNM_SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvOssVnmSum.COLUMNS, OSS_VNM_SUM_ALIAS + ".");
    private static final String OSS_VNM_SUM_FIELDS = SqlUtils.getColumnsToString(RaschsvOssVnmSum.COLUMNS, ":");

    // Перечень столбцов таблицы "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
    private static final String OSS_VNM_KOL_COLS = SqlUtils.getColumnsToString(RaschsvOssVnmKol.COLUMNS, null);
    private static final String OSS_VNM_KOL_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvOssVnmKol.COLUMNS, OSS_VNM_KOL_ALIAS + ".");
    private static final String OSS_VNM_KOL_FIELDS = SqlUtils.getColumnsToString(RaschsvOssVnmKol.COLUMNS, ":");

    // Перечень столбцов таблицы "СвСум1Тип"
    private static final String SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, SUM_ALIAS + ".");

    // Перечень столбцов таблицы "КолЛицТип"
    private static final String KOL_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvKolLicTip.COLUMNS, KOL_ALIAS + ".");

    private static final String SQL_INSERT_OSS_VNM = "INSERT INTO " + RaschsvOssVnm.TABLE_NAME +
            " (" + OSS_VNM_COLS + ") VALUES (" + OSS_VNM_FIELDS + ")";

    private static final String SQL_INSERT_UPL_SV_PREV = "INSERT INTO " + RaschsvUplSvPrev.TABLE_NAME +
            " (" + UPL_SV_PREV_COLS + ") VALUES (" + UPL_SV_PREV_FIELDS + ")";

    private static final String SQL_INSERT_OSS_VNM_KOL = "INSERT INTO " + RaschsvOssVnmKol.TABLE_NAME +
            " (" + OSS_VNM_KOL_COLS + ") VALUES (" + OSS_VNM_KOL_FIELDS + ")";

    private static final String SQL_INSERT_OSS_VNM_SUM = "INSERT INTO " + RaschsvOssVnmSum.TABLE_NAME +
            " (" + OSS_VNM_SUM_COLS + ") VALUES (" + OSS_VNM_SUM_FIELDS + ")";

    private static final String SQL_SELECT_OSS_VNM = "SELECT " + OSS_VNM_COLS + " FROM " + RaschsvOssVnm.TABLE_NAME +
            " WHERE " + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    private static final StringBuilder SQL_SELECT_UPL_SV_PREV = new StringBuilder()
            .append("SELECT " + UPL_SV_PREV_COLS_WITH_ALIAS + " FROM " + RaschsvUplSvPrev.TABLE_NAME + " " + UPL_SV_PREV_ALIAS)
            .append(" INNER JOIN " + RaschsvOssVnm.TABLE_NAME + " " + OSS_VNM_ALIAS +
                    " ON " + UPL_SV_PREV_ALIAS + "." + RaschsvUplSvPrev.COL_RASCHSV_OSS_VNM_ID + " = " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_ID)
            .append(" WHERE " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_SUM = new StringBuilder()
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvOssVnmSum.TABLE_NAME + " " + OSS_VNM_SUM_ALIAS +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + OSS_VNM_SUM_ALIAS + "." + RaschsvOssVnmSum.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append(" INNER JOIN " + RaschsvOssVnm.TABLE_NAME + " " + OSS_VNM_ALIAS +
                    " ON " + OSS_VNM_SUM_ALIAS + "." + RaschsvOssVnmSum.COL_RASCHSV_OSS_VNM_ID + " = " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_ID)
            .append(" WHERE " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_OSS_VNM_SUM = new StringBuilder()
            .append("SELECT " + OSS_VNM_SUM_COLS_WITH_ALIAS + " FROM " + RaschsvOssVnmSum.TABLE_NAME + " " + OSS_VNM_SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvOssVnm.TABLE_NAME + " " + OSS_VNM_ALIAS +
                    " ON " + OSS_VNM_SUM_ALIAS + "." + RaschsvOssVnmSum.COL_RASCHSV_OSS_VNM_ID + " = " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_ID)
            .append(" WHERE " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_KOL = new StringBuilder()
            .append("SELECT " + KOL_COLS_WITH_ALIAS + " FROM " + RaschsvKolLicTip.TABLE_NAME + " " + KOL_ALIAS)
            .append(" INNER JOIN " + RaschsvOssVnmKol.TABLE_NAME + " " + OSS_VNM_KOL_ALIAS +
                    " ON " + KOL_ALIAS + "." + RaschsvKolLicTip.COL_ID + " = " + OSS_VNM_KOL_ALIAS + "." + RaschsvOssVnmKol.COL_RASCHSV_KOL_LIC_TIP_ID)
            .append(" INNER JOIN " + RaschsvOssVnm.TABLE_NAME + " " + OSS_VNM_ALIAS +
                    " ON " + OSS_VNM_KOL_ALIAS + "." + RaschsvOssVnmKol.COL_RASCHSV_OSS_VNM_ID + " = " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_ID)
            .append(" WHERE " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_OSS_VNM_KOL = new StringBuilder()
            .append("SELECT " + OSS_VNM_KOL_COLS_WITH_ALIAS + " FROM " + RaschsvOssVnmKol.TABLE_NAME + " " + OSS_VNM_KOL_ALIAS)
            .append(" INNER JOIN " + RaschsvOssVnm.TABLE_NAME + " " + OSS_VNM_ALIAS +
                    " ON " + OSS_VNM_KOL_ALIAS + "." + RaschsvOssVnmKol.COL_RASCHSV_OSS_VNM_ID + " = " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_ID)
            .append(" WHERE " + OSS_VNM_ALIAS + "." + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    public Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm) {
        raschsvOssVnm.setId(generateId(RaschsvOssVnm.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_ID, raschsvOssVnm.getId())
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvOssVnm.getRaschsvObyazPlatSvId())
                .addValue(RaschsvOssVnm.COL_PRIZ_VYPL, raschsvOssVnm.getPrizVypl());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_OSS_VNM.toString(), params);

        List<RaschsvUplSvPrev> raschsvUplSvPrevList = new ArrayList<RaschsvUplSvPrev>();
        List<RaschsvOssVnmKol> raschsvOssVnmKolList = new ArrayList<RaschsvOssVnmKol>();
        List<RaschsvOssVnmSum> raschsvOssVnmSumList = new ArrayList<RaschsvOssVnmSum>();

        // Установка внешнего ключа для УплСВПрев
        for (RaschsvUplSvPrev raschsvUplSvPrev : raschsvOssVnm.getRaschsvUplSvPrevList()) {
            raschsvUplSvPrev.setRaschsvOssVnmId(raschsvOssVnm.getId());
            raschsvUplSvPrevList.add(raschsvUplSvPrev);
        }
        insertRaschsvUplSvPrev(raschsvUplSvPrevList);

        // Установка внешнего ключа для "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
        for (RaschsvOssVnmKol raschsvOssVnmKol : raschsvOssVnm.getRaschsvOssVnmKolList()) {
            raschsvOssVnmKol.setRaschsvOssVnmId(raschsvOssVnm.getId());

            // Сохранение КолЛицТип
            Long raschsvKolLicTipId = raschsvKolLicTipDao.insertRaschsvKolLicTip(raschsvOssVnmKol.getRaschsvKolLicTip());
            raschsvOssVnmKol.getRaschsvKolLicTip().setId(raschsvKolLicTipId);

            raschsvOssVnmKolList.add(raschsvOssVnmKol);
        }
        // Сохранение "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
        insertRaschsvOssVnmKol(raschsvOssVnmKolList);

        // Установка внешнего ключа для "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
        for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnm.getRaschsvOssVnmSumList()) {
            raschsvOssVnmSum.setRaschsvOssVnmId(raschsvOssVnm.getId());

            // Сохранение СвСум1Тип
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvOssVnmSum.getRaschsvSvSum1Tip());
            raschsvOssVnmSum.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvOssVnmSumList.add(raschsvOssVnmSum);
        }
        // Сохранение "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
        insertRaschsvOssVnmSum(raschsvOssVnmSumList);

        return raschsvOssVnm.getId();
    }

    /**
     * Сохранение УплСВПрев
     * @param raschsvUplSvPrevList
     * @return
     */
    private Integer insertRaschsvUplSvPrev(List<RaschsvUplSvPrev> raschsvUplSvPrevList) {
        // Генерация идентификаторов
        for (RaschsvUplSvPrev raschsvUplSvPrev : raschsvUplSvPrevList) {
            raschsvUplSvPrev.setId(generateId(RaschsvUplSvPrev.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvUplSvPrevList.size());
        for (RaschsvUplSvPrev raschsvUplSvPrev : raschsvUplSvPrevList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvUplSvPrev.COL_ID, raschsvUplSvPrev.getId())
                            .addValue(RaschsvUplSvPrev.COL_RASCHSV_OSS_VNM_ID, raschsvUplSvPrev.getRaschsvOssVnmId())
                            .addValue(RaschsvUplSvPrev.COL_PRIZNAK, raschsvUplSvPrev.getPriznak())
                            .addValue(RaschsvUplSvPrev.COL_SV_SUM, raschsvUplSvPrev.getSvSum())
                            .addValue(RaschsvUplSvPrev.COL_NODE_NAME, raschsvUplSvPrev.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_UPL_SV_PREV,
                batchValues.toArray(new Map[raschsvUplSvPrevList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
     * @param raschsvOssVnmKolList
     * @return
     */
    private Integer insertRaschsvOssVnmKol(List<RaschsvOssVnmKol> raschsvOssVnmKolList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvOssVnmKolList.size());
        for (RaschsvOssVnmKol raschsvOssVnmKol : raschsvOssVnmKolList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvOssVnmKol.COL_RASCHSV_OSS_VNM_ID, raschsvOssVnmKol.getRaschsvOssVnmId())
                            .addValue(RaschsvOssVnmKol.COL_RASCHSV_KOL_LIC_TIP_ID, raschsvOssVnmKol.getRaschsvKolLicTip().getId())
                            .addValue(RaschsvOssVnmKol.COL_NODE_NAME, raschsvOssVnmKol.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_OSS_VNM_KOL,
                batchValues.toArray(new Map[raschsvOssVnmKolList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
     * @param raschsvOssVnmSumList
     * @return
     */
    private Integer insertRaschsvOssVnmSum(List<RaschsvOssVnmSum> raschsvOssVnmSumList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvOssVnmSumList.size());
        for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnmSumList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvOssVnmSum.COL_RASCHSV_OSS_VNM_ID, raschsvOssVnmSum.getRaschsvOssVnmId())
                            .addValue(RaschsvOssVnmSum.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvOssVnmSum.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvOssVnmSum.COL_NODE_NAME, raschsvOssVnmSum.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_OSS_VNM_SUM,
                batchValues.toArray(new Map[raschsvOssVnmSumList.size()]));

        return res.length;
    }

    public RaschsvOssVnm findOssVnm(Long obyazPlatSvId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
            // Выборка из РасчСВ_ОСС.ВНМ
            RaschsvOssVnm raschsvOssVnm =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_OSS_VNM, params, new RaschsvOssVnmRowMapper());

            // Выборка из УплСВПрев
            raschsvOssVnm.setRaschsvUplSvPrevList(findRaschsvUplSvPrev(raschsvOssVnm.getId()));

            // Выборка из СвСум1Тип
            List<RaschsvSvSum1Tip> raschsvSvSum1TipList = findRaschsvSvSum1Tip(raschsvOssVnm.getId());
            Map<Long, RaschsvSvSum1Tip> mapRaschsvSvSum1Tip = new HashMap<Long, RaschsvSvSum1Tip>();
            for (RaschsvSvSum1Tip raschsvSvSum1Tip : raschsvSvSum1TipList) {
                mapRaschsvSvSum1Tip.put(raschsvSvSum1Tip.getId(), raschsvSvSum1Tip);
            }

            // Выборка "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
            List<RaschsvOssVnmSum> raschsvOssVnmSumList = findRaschsvOssVnmSum(raschsvOssVnm.getId());
            for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnmSumList) {
                RaschsvSvSum1Tip raschsvSvSum1Tip = mapRaschsvSvSum1Tip.get(raschsvOssVnmSum.getRaschsvSvSum1TipId());
                raschsvOssVnmSum.setRaschsvSvSum1Tip(raschsvSvSum1Tip);
                raschsvOssVnm.addRaschsvOssVnmSum(raschsvOssVnmSum);
            }

            // Выборка из КолЛицТип
            List<RaschsvKolLicTip> raschsvKolLicTipList = findRaschsvKolLicTip(raschsvOssVnm.getId());
            Map<Long, RaschsvKolLicTip> mapRaschsvKolLicTip = new HashMap<Long, RaschsvKolLicTip>();
            for (RaschsvKolLicTip raschsvKolLicTip : raschsvKolLicTipList) {
                mapRaschsvKolLicTip.put(raschsvKolLicTip.getId(), raschsvKolLicTip);
            }

            // Выборка "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
            List<RaschsvOssVnmKol> raschsvOssVnmKolList = findRaschsvOssVnmKol(raschsvOssVnm.getId());
            for (RaschsvOssVnmKol raschsvOssVnmKol : raschsvOssVnmKolList) {
                RaschsvKolLicTip raschsvKolLicTip = mapRaschsvKolLicTip.get(raschsvOssVnmKol.getRaschsvKolLicTipId());
                raschsvOssVnmKol.setRaschsvKolLicTip(raschsvKolLicTip);
                raschsvOssVnm.addRaschsvOssVnmKol(raschsvOssVnmKol);
            }

            return raschsvOssVnm;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из УплСВПрев
     * @param raschsvSvOpsOmsId
     * @return
     */
    private List<RaschsvUplSvPrev> findRaschsvUplSvPrev(Long raschsvSvOpsOmsId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOmsId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_UPL_SV_PREV.toString(), params, new RaschsvUplSvPrevRowMapper());
    }

    /**
     * Выборка из СвСум1Тип
     * @param raschsvSvOpsOmsId
     * @return
     */
    private List<RaschsvSvSum1Tip> findRaschsvSvSum1Tip(Long raschsvSvOpsOmsId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOmsId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SUM.toString(), params, new RaschsvSvSum1TipRowMapper());
    }

    /**
     * Выборка "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
     * @param raschsvSvOpsOmsId
     * @return
     */
    private List<RaschsvOssVnmSum> findRaschsvOssVnmSum(Long raschsvSvOpsOmsId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOmsId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_OSS_VNM_SUM.toString(), params, new RaschsvOssVnmSumRowMapper());
    }

    /**
     * Выборка из КолЛицТип
     * @param raschsvSvOpsOmsId
     * @return
     */
    private List<RaschsvKolLicTip> findRaschsvKolLicTip(Long raschsvSvOpsOmsId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOmsId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_KOL.toString(), params, new RaschsvKolLicTipRowMapper());
    }

    /**
     * Выборка "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
     * @param raschsvSvOpsOmsId
     * @return
     */
    private List<RaschsvOssVnmKol> findRaschsvOssVnmKol(Long raschsvSvOpsOmsId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOmsId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_OSS_VNM_KOL.toString(), params, new RaschsvOssVnmKolRowMapper());
    }

    /**
     * Маппинг для РасчСВ_ОСС.ВНМ
     */
    private static final class RaschsvOssVnmRowMapper implements RowMapper<RaschsvOssVnm> {
        @Override
        public RaschsvOssVnm mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvOssVnm raschsvOssVnm = new RaschsvOssVnm();
            raschsvOssVnm.setId(SqlUtils.getLong(rs, RaschsvOssVnm.COL_ID));
            raschsvOssVnm.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            raschsvOssVnm.setPrizVypl(rs.getString(RaschsvOssVnm.COL_PRIZ_VYPL));

            return raschsvOssVnm;
        }
    }

    /**
     * Маппинг для "Связь РасчСВ_ОСС.ВНМ и СвСум1Тип"
     */
    private static final class RaschsvOssVnmSumRowMapper implements RowMapper<RaschsvOssVnmSum> {
        @Override
        public RaschsvOssVnmSum mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvOssVnmSum raschsvOssVnmSum = new RaschsvOssVnmSum();
            raschsvOssVnmSum.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvOssVnmSum.COL_RASCHSV_SV_SUM1_TIP_ID));
            raschsvOssVnmSum.setNodeName(rs.getString(RaschsvOssVnmSum.COL_NODE_NAME));

            return raschsvOssVnmSum;
        }
    }

    /**
     * Маппинг для "Связь РасчСВ_ОСС.ВНМ и КолЛицТип"
     */
    private static final class RaschsvOssVnmKolRowMapper implements RowMapper<RaschsvOssVnmKol> {
        @Override
        public RaschsvOssVnmKol mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvOssVnmKol raschsvOssVnmKol = new RaschsvOssVnmKol();
            raschsvOssVnmKol.setRaschsvKolLicTipId(SqlUtils.getLong(rs, RaschsvOssVnmKol.COL_RASCHSV_KOL_LIC_TIP_ID));
            raschsvOssVnmKol.setNodeName(rs.getString(RaschsvOssVnmKol.COL_NODE_NAME));

            return raschsvOssVnmKol;
        }
    }

    /**
     * Маппинг для "УплСВПрев"
     */
    private static final class RaschsvUplSvPrevRowMapper implements RowMapper<RaschsvUplSvPrev> {
        @Override
        public RaschsvUplSvPrev mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvUplSvPrev raschsvUplSvPrev = new RaschsvUplSvPrev();
            raschsvUplSvPrev.setId(SqlUtils.getLong(rs, RaschsvUplSvPrev.COL_ID));
            raschsvUplSvPrev.setRaschsvOssVnmId(SqlUtils.getLong(rs, RaschsvUplSvPrev.COL_RASCHSV_OSS_VNM_ID));
            raschsvUplSvPrev.setNodeName(rs.getString(RaschsvUplSvPrev.COL_NODE_NAME));
            raschsvUplSvPrev.setPriznak(rs.getString(RaschsvUplSvPrev.COL_PRIZNAK));
            raschsvUplSvPrev.setSvSum(rs.getDouble(RaschsvUplSvPrev.COL_SV_SUM));

            return raschsvUplSvPrev;
        }
    }
}
