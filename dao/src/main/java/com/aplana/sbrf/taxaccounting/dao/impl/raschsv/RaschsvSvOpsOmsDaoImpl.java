package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvKolLicTipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvSvSum1TipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvOpsOmsDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RaschsvSvOpsOmsDaoImpl extends AbstractDao implements RaschsvSvOpsOmsDao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    @Autowired
    private RaschsvKolLicTipDao raschsvKolLicTipDao;

    private static final String OPS_OMS_ALIAS = "os";
    private static final String RASCH_ALIAS = "r";
    private static final String RASCH_SUM_ALIAS = "rs";
    private static final String RASCH_KOL_ALIAS = "rk";
    private static final String SUM_ALIAS = "s";
    private static final String KOL_ALIAS = "k";

    // Перечень столбцов таблицы РасчСВ_ОПС_ОМС
    private static final String OPS_OMS_COLS = SqlUtils.getColumnsToString(RaschsvSvOpsOms.COLUMNS, null);
    private static final String OPS_OMS_FIELDS = SqlUtils.getColumnsToString(RaschsvSvOpsOms.COLUMNS, ":");

    // Перечень столбцов таблицы "Вид расчета"
    private static final String RASCH_COLS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRasch.COLUMNS, null);
    private static final String RASCH_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRasch.COLUMNS, RASCH_ALIAS + ".");
    private static final String RASCH_FIELDS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRasch.COLUMNS, ":");

    // Перечень столбцов таблицы "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип"
    private static final String RASCH_SUM_COLS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschSum.COLUMNS, null);
    private static final String RASCH_SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschSum.COLUMNS, RASCH_SUM_ALIAS + ".");
    private static final String RASCH_SUM_FIELDS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschSum.COLUMNS, ":");

    // Перечень столбцов таблицы "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип"
    private static final String RASCH_KOL_COLS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschKol.COLUMNS, null);
    private static final String RASCH_KOL_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschKol.COLUMNS, RASCH_KOL_ALIAS + ".");
    private static final String RASCH_KOL_FIELDS = SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschKol.COLUMNS, ":");

    // Перечень столбцов таблицы "СвСум1Тип"
    private static final String SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, SUM_ALIAS + ".");

    // Перечень столбцов таблицы "КолЛицТип"
    private static final String KOL_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvKolLicTip.COLUMNS, KOL_ALIAS + ".");

    private static final String SQL_INSERT_OPS_OMS = "INSERT INTO " + RaschsvSvOpsOms.TABLE_NAME +
            " (" + OPS_OMS_COLS + ") VALUES (" + OPS_OMS_FIELDS + ")";

    private static final String SQL_INSERT_RASCH = "INSERT INTO " + RaschsvSvOpsOmsRasch.TABLE_NAME +
            " (" + RASCH_COLS + ") VALUES (" + RASCH_FIELDS + ")";

    private static final String SQL_INSERT_RASCH_SUM = "INSERT INTO " + RaschsvSvOpsOmsRaschSum.TABLE_NAME +
            " (" + RASCH_SUM_COLS + ") VALUES (" + RASCH_SUM_FIELDS + ")";

    private static final String SQL_INSERT_RASCH_KOL = "INSERT INTO " + RaschsvSvOpsOmsRaschKol.TABLE_NAME +
            " (" + RASCH_KOL_COLS + ") VALUES (" + RASCH_KOL_FIELDS + ")";

    private static final String SQL_SELECT_OPS_OMS = "SELECT " + OPS_OMS_COLS + " FROM " + RaschsvSvOpsOms.TABLE_NAME +
            " WHERE " + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    private static final StringBuilder SQL_SELECT_RASCH = new StringBuilder()
            .append("SELECT " + RASCH_COLS_WITH_ALIAS + " FROM " + RaschsvSvOpsOmsRasch.TABLE_NAME + " " + RASCH_ALIAS)
            .append(" INNER JOIN " + RaschsvSvOpsOms.TABLE_NAME + " " + OPS_OMS_ALIAS +
                    " ON " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID + " = " + OPS_OMS_ALIAS + "." + RaschsvSvOpsOms.COL_ID)
            .append(" WHERE " + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_RASCH_SUM = new StringBuilder()
            .append("SELECT " + RASCH_SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvOpsOmsRaschSum.TABLE_NAME + " " + RASCH_SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvSvOpsOmsRasch.TABLE_NAME + " " + RASCH_ALIAS +
                    " ON " + RASCH_SUM_ALIAS + "." + RaschsvSvOpsOmsRaschSum.COL_RASCHSV_OPS_OMS_RASCH_SUM_ID + " = " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_ID)
            .append(" INNER JOIN " + RaschsvSvOpsOms.TABLE_NAME + " " + OPS_OMS_ALIAS +
                    " ON " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID + " = " + OPS_OMS_ALIAS + "." + RaschsvSvOpsOms.COL_ID)
            .append(" WHERE " + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_SUM = new StringBuilder()
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvSvOpsOmsRaschSum.TABLE_NAME + " " + RASCH_SUM_ALIAS +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + RASCH_SUM_ALIAS + "." + RaschsvSvOpsOmsRaschSum.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append(" INNER JOIN " + RaschsvSvOpsOmsRasch.TABLE_NAME + " " + RASCH_ALIAS +
                    " ON " + RASCH_SUM_ALIAS + "." + RaschsvSvOpsOmsRaschSum.COL_RASCHSV_OPS_OMS_RASCH_SUM_ID + " = " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_ID)
            .append(" INNER JOIN " + RaschsvSvOpsOms.TABLE_NAME + " " + OPS_OMS_ALIAS +
                    " ON " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID + " = " + OPS_OMS_ALIAS + "." + RaschsvSvOpsOms.COL_ID)
            .append(" WHERE " + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_RASCH_KOL = new StringBuilder()
            .append("SELECT " + RASCH_KOL_COLS_WITH_ALIAS + " FROM " + RaschsvSvOpsOmsRaschKol.TABLE_NAME + " " + RASCH_KOL_ALIAS)
            .append(" INNER JOIN " + RaschsvSvOpsOmsRasch.TABLE_NAME + " " + RASCH_ALIAS +
                    " ON " + RASCH_KOL_ALIAS + "." + RaschsvSvOpsOmsRaschKol.COL_RASCHSV_OPS_OMS_RASCH_KOL_ID + " = " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_ID)
            .append(" INNER JOIN " + RaschsvSvOpsOms.TABLE_NAME + " " + OPS_OMS_ALIAS +
                    " ON " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID + " = " + OPS_OMS_ALIAS + "." + RaschsvSvOpsOms.COL_ID)
            .append(" WHERE " + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    private static final StringBuilder SQL_SELECT_KOL = new StringBuilder()
            .append("SELECT " + KOL_COLS_WITH_ALIAS + " FROM " + RaschsvKolLicTip.TABLE_NAME + " " + KOL_ALIAS)
            .append(" INNER JOIN " + RaschsvSvOpsOmsRaschKol.TABLE_NAME + " " + RASCH_KOL_ALIAS +
                    " ON " + KOL_ALIAS + "." + RaschsvKolLicTip.COL_ID + " = " + RASCH_KOL_ALIAS + "." + RaschsvSvOpsOmsRaschKol.COL_RASCHSV_KOL_LIC_TIP_ID)
            .append(" INNER JOIN " + RaschsvSvOpsOmsRasch.TABLE_NAME + " " + RASCH_ALIAS +
                    " ON " + RASCH_KOL_ALIAS + "." + RaschsvSvOpsOmsRaschKol.COL_RASCHSV_OPS_OMS_RASCH_KOL_ID + " = " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_ID)
            .append(" INNER JOIN " + RaschsvSvOpsOms.TABLE_NAME + " " + OPS_OMS_ALIAS +
                    " ON " + RASCH_ALIAS + "." + RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID + " = " + OPS_OMS_ALIAS + "." + RaschsvSvOpsOms.COL_ID)
            .append(" WHERE " + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID);

    public Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList) {
        // Генерация идентификаторов
        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            raschsvSvOpsOms.setId(generateId(RaschsvSvOpsOms.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsList.size());
        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOms.COL_ID, raschsvSvOpsOms.getId())
                            .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvOpsOms.getRaschsvObyazPlatSvId())
                            .addValue(RaschsvSvOpsOms.COL_TARIF_PLAT, raschsvSvOpsOms.getTarifPlat())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_OPS_OMS,
                batchValues.toArray(new Map[raschsvSvOpsOmsList.size()]));

        List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList = new ArrayList<RaschsvSvOpsOmsRasch>();
        List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList = new ArrayList<RaschsvSvOpsOmsRaschSum>();
        List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList = new ArrayList<RaschsvSvOpsOmsRaschKol>();

        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            // Генерация идентификаторов и Установка внешнего ключа для "Вид расчета"
            for (RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch : raschsvSvOpsOms.getRaschsvSvOpsOmsRaschList()) {
                raschsvSvOpsOmsRasch.setId(generateId(RaschsvSvOpsOmsRasch.SEQ, Long.class));
                raschsvSvOpsOmsRasch.setRaschsvSvOpsOmsId(raschsvSvOpsOms.getId());
                raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch);

                // Установка внешнего ключа для "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип"
                for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsRaschSumList()) {
                    raschsvSvOpsOmsRaschSum.setRaschsvOpsOmsRaschSumId(raschsvSvOpsOmsRasch.getId());

                    // Сохранение "СвСум1Тип"
                    Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip());
                    raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

                    raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum);
                }

                // Установка внешнего ключа для "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип"
                for (RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol : raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsRaschKolList()) {
                    raschsvSvOpsOmsRaschKol.setRaschsvOpsOmsRaschKolId(raschsvSvOpsOmsRasch.getId());

                    // Сохранение "КолЛицТип"
                    Long raschsvKolLicTipId = raschsvKolLicTipDao.insertRaschsvKolLicTip(raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip());
                    raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip().setId(raschsvKolLicTipId);

                    raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol);
                }
            }
        }

        // Сохранение "Вид расчета"
        if (!raschsvSvOpsOmsRaschList.isEmpty()) {
            insertRaschsvSvOpsOmsRasch(raschsvSvOpsOmsRaschList);
        }

        // Сохранение "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип"
        if (!raschsvSvOpsOmsRaschSumList.isEmpty()) {
            insertRaschsvSvOpsOmsRaschSum(raschsvSvOpsOmsRaschSumList);
        }

        // Сохранение "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип"
        if (!raschsvSvOpsOmsRaschKolList.isEmpty()) {
            insertRaschsvSvOpsOmsRaschKol(raschsvSvOpsOmsRaschKolList);
        }

        return res.length;
    }

    /**
     * Сохранение "Вид расчета"
     * @param raschsvSvOpsOmsRaschList
     * @return
     */
    private Integer insertRaschsvSvOpsOmsRasch(List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsRaschList.size());
        for (RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch : raschsvSvOpsOmsRaschList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOmsRasch.COL_ID, raschsvSvOpsOmsRasch.getId())
                            .addValue(RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID, raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsId())
                            .addValue(RaschsvSvOpsOmsRasch.COL_NODE_NAME, raschsvSvOpsOmsRasch.getNodeName())
                            .addValue(RaschsvSvOpsOmsRasch.COL_PR_OSN_SV_DOP, raschsvSvOpsOmsRasch.getPrOsnSvDop())
                            .addValue(RaschsvSvOpsOmsRasch.COL_KOD_OSNOV, raschsvSvOpsOmsRasch.getKodOsnov())
                            .addValue(RaschsvSvOpsOmsRasch.COL_OSNOV_ZAP, raschsvSvOpsOmsRasch.getOsnovZap())
                            .addValue(RaschsvSvOpsOmsRasch.COL_KLAS_USL_TRUD, raschsvSvOpsOmsRasch.getKlasUslTrud())
                            .addValue(RaschsvSvOpsOmsRasch.COL_PR_RASCH_SUM, raschsvSvOpsOmsRasch.getPrRaschSum())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_RASCH,
                batchValues.toArray(new Map[raschsvSvOpsOmsRaschList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип"
     * @param raschsvSvOpsOmsRaschSumList
     * @return
     */
    private Integer insertRaschsvSvOpsOmsRaschSum(List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsRaschSumList.size());
        for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRaschSumList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOmsRaschSum.COL_RASCHSV_OPS_OMS_RASCH_SUM_ID, raschsvSvOpsOmsRaschSum.getRaschsvOpsOmsRaschSumId())
                            .addValue(RaschsvSvOpsOmsRaschSum.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvOpsOmsRaschSum.COL_NODE_NAME, raschsvSvOpsOmsRaschSum.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_RASCH_SUM,
                batchValues.toArray(new Map[raschsvSvOpsOmsRaschSumList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип"
     * @param raschsvSvOpsOmsRaschKolList
     * @return
     */
    private Integer insertRaschsvSvOpsOmsRaschKol(List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsRaschKolList.size());
        for (RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol : raschsvSvOpsOmsRaschKolList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOmsRaschKol.COL_RASCHSV_OPS_OMS_RASCH_KOL_ID, raschsvSvOpsOmsRaschKol.getRaschsvOpsOmsRaschKolId())
                            .addValue(RaschsvSvOpsOmsRaschKol.COL_RASCHSV_KOL_LIC_TIP_ID, raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip().getId())
                            .addValue(RaschsvSvOpsOmsRaschKol.COL_NODE_NAME, raschsvSvOpsOmsRaschKol.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_RASCH_KOL,
                batchValues.toArray(new Map[raschsvSvOpsOmsRaschKolList.size()]));

        return res.length;
    }

    public List<RaschsvSvOpsOms> findSvOpsOms(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        // Выборка из РасчСВ_ОПС_ОМС
        List<RaschsvSvOpsOms> raschsvSvOpsOmsList =
                getNamedParameterJdbcTemplate().query(SQL_SELECT_OPS_OMS, params, new RaschsvSvOpsOmsRowMapper());

        if (!raschsvSvOpsOmsList.isEmpty()) {
            Map<Long, RaschsvSvOpsOms> mapSvOpsOms = new HashMap<Long, RaschsvSvOpsOms>();
            List<Long> raschsvSvOpsOmsIds = new ArrayList<Long>(raschsvSvOpsOmsList.size());
            for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
                mapSvOpsOms.put(raschsvSvOpsOms.getId(), raschsvSvOpsOms);
                raschsvSvOpsOmsIds.add(raschsvSvOpsOms.getId());
            }

            // Выборка из таблицы Вида расчета
            List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList = findSvOpsOmsRasch(obyazPlatSvId);
            Map<Long, RaschsvSvOpsOmsRasch> mapSvOpsOmsRasch = new HashMap<Long, RaschsvSvOpsOmsRasch>();
            for (RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch : raschsvSvOpsOmsRaschList) {
                RaschsvSvOpsOms raschsvSvOpsOms = mapSvOpsOms.get(raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsId());
                mapSvOpsOmsRasch.put(raschsvSvOpsOmsRasch.getId(), raschsvSvOpsOmsRasch);
                raschsvSvOpsOms.addRaschsvSvOpsOmsRasch(raschsvSvOpsOmsRasch);
            }

            // Выборка из СвСум1Тип
            List<RaschsvSvSum1Tip> raschsvSvSum1TipList = findSvSum1Tip(obyazPlatSvId);
            Map<Long, RaschsvSvSum1Tip> mapSvSum1Tip = new HashMap<Long, RaschsvSvSum1Tip>();
            for (RaschsvSvSum1Tip raschsvSvSum1Tip : raschsvSvSum1TipList) {
                mapSvSum1Tip.put(raschsvSvSum1Tip.getId(), raschsvSvSum1Tip);
            }

            // Выборка из таблицы-связки Вида расчета с СвСум1Тип
            List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList = findSvOpsOmsRaschSum(obyazPlatSvId);
            for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRaschSumList) {
                RaschsvSvSum1Tip raschsvSvSum1Tip = mapSvSum1Tip.get(raschsvSvOpsOmsRaschSum.getRaschsvSvSum1TipId());
                raschsvSvOpsOmsRaschSum.setRaschsvSvSum1Tip(raschsvSvSum1Tip);

                RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = mapSvOpsOmsRasch.get(raschsvSvOpsOmsRaschSum.getRaschsvOpsOmsRaschSumId());
                raschsvSvOpsOmsRasch.addRaschsvSvOpsOmsRaschSum(raschsvSvOpsOmsRaschSum);
            }

            // Выборка из КолЛицТип
            List<RaschsvKolLicTip> raschsvKolLicTipList = findKolLicTip(obyazPlatSvId);
            Map<Long, RaschsvKolLicTip> mapKolLicTip = new HashMap<Long, RaschsvKolLicTip>();
            for (RaschsvKolLicTip raschsvKolLicTip : raschsvKolLicTipList) {
                mapKolLicTip.put(raschsvKolLicTip.getId(), raschsvKolLicTip);
            }

            // Выборка из таблицы-связки Вида расчета с КолЛицТип
            List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList = findSvOpsOmsRaschKol(obyazPlatSvId);
            for (RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol : raschsvSvOpsOmsRaschKolList) {
                RaschsvKolLicTip raschsvKolLicTip = mapKolLicTip.get(raschsvSvOpsOmsRaschKol.getRaschsvKolLicTipId());
                raschsvSvOpsOmsRaschKol.setRaschsvKolLicTip(raschsvKolLicTip);

                RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = mapSvOpsOmsRasch.get(raschsvSvOpsOmsRaschKol.getRaschsvOpsOmsRaschKolId());
                raschsvSvOpsOmsRasch.addRaschsvSvOpsOmsRaschKol(raschsvSvOpsOmsRaschKol);
            }
        }
        return raschsvSvOpsOmsList;
    }

    /**
     * Выборка из таблицы Вида расчета
     * @param obyazPlatSvId
     * @return
     */
    private List<RaschsvSvOpsOmsRasch> findSvOpsOmsRasch(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_RASCH.toString(), params, new RaschsvSvOpsOmsRaschRowMapper());
    }

    /**
     * Выборка из таблицы-связки Вида расчета с СвСум1Тип
     * @param obyazPlatSvId
     * @return
     */
    private List<RaschsvSvOpsOmsRaschSum> findSvOpsOmsRaschSum(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_RASCH_SUM.toString(), params, new RaschsvSvOpsOmsRaschSumRowMapper());
    }

    /**
     * Выборка из СвСум1Тип
     * @param obyazPlatSvId
     * @return
     */
    private List<RaschsvSvSum1Tip> findSvSum1Tip(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SUM.toString(), params, new RaschsvSvSum1TipRowMapper());
    }

    /**
     * Выборка из таблицы-связки Вида расчета с КолЛицТип
     * @param obyazPlatSvId
     * @return
     */
    private List<RaschsvSvOpsOmsRaschKol> findSvOpsOmsRaschKol(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_RASCH_KOL.toString(), params, new RaschsvSvOpsOmsRaschKolRowMapper());
    }

    /**
     * Выборка из КолЛицТип
     * @param obyazPlatSvId
     * @return
     */
    private List<RaschsvKolLicTip> findKolLicTip(Long obyazPlatSvId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_KOL.toString(), params, new RaschsvKolLicTipRowMapper());
    }

    /**
     * Маппинг для РасчСВ_ОПС_ОМС
     */
    private static final class RaschsvSvOpsOmsRowMapper implements RowMapper<RaschsvSvOpsOms> {
        @Override
        public RaschsvSvOpsOms mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvOpsOms raschsvSvOpsOms = new RaschsvSvOpsOms();
            raschsvSvOpsOms.setId(SqlUtils.getLong(rs, RaschsvSvOpsOms.COL_ID));
            raschsvSvOpsOms.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvSvOpsOms.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            raschsvSvOpsOms.setTarifPlat(rs.getString(RaschsvSvOpsOms.COL_TARIF_PLAT));

            return raschsvSvOpsOms;
        }
    }

    /**
     * Маппинг для РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО
     */
    private static final class RaschsvSvOpsOmsRaschRowMapper implements RowMapper<RaschsvSvOpsOmsRasch> {
        @Override
        public RaschsvSvOpsOmsRasch mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = new RaschsvSvOpsOmsRasch();
            raschsvSvOpsOmsRasch.setId(SqlUtils.getLong(rs, RaschsvSvOpsOmsRasch.COL_ID));
            raschsvSvOpsOmsRasch.setRaschsvSvOpsOmsId(SqlUtils.getLong(rs, RaschsvSvOpsOmsRasch.COL_RASCHSV_SV_OPS_OMS_ID));
            raschsvSvOpsOmsRasch.setNodeName(rs.getString(RaschsvSvOpsOmsRasch.COL_NODE_NAME));
            raschsvSvOpsOmsRasch.setPrOsnSvDop(rs.getString(RaschsvSvOpsOmsRasch.COL_PR_OSN_SV_DOP));
            raschsvSvOpsOmsRasch.setKodOsnov(rs.getString(RaschsvSvOpsOmsRasch.COL_KOD_OSNOV));
            raschsvSvOpsOmsRasch.setOsnovZap(rs.getString(RaschsvSvOpsOmsRasch.COL_OSNOV_ZAP));
            raschsvSvOpsOmsRasch.setKlasUslTrud(rs.getString(RaschsvSvOpsOmsRasch.COL_KLAS_USL_TRUD));
            raschsvSvOpsOmsRasch.setPrRaschSum(rs.getString(RaschsvSvOpsOmsRasch.COL_PR_RASCH_SUM));

            return raschsvSvOpsOmsRasch;
        }
    }

    /**
     * Маппинг для Связи РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип
     */
    private static final class RaschsvSvOpsOmsRaschSumRowMapper implements RowMapper<RaschsvSvOpsOmsRaschSum> {
        @Override
        public RaschsvSvOpsOmsRaschSum mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum = new RaschsvSvOpsOmsRaschSum();
            raschsvSvOpsOmsRaschSum.setRaschsvOpsOmsRaschSumId(SqlUtils.getLong(rs, RaschsvSvOpsOmsRaschSum.COL_RASCHSV_OPS_OMS_RASCH_SUM_ID));
            raschsvSvOpsOmsRaschSum.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvSvOpsOmsRaschSum.COL_RASCHSV_SV_SUM1_TIP_ID));
            raschsvSvOpsOmsRaschSum.setNodeName(rs.getString(RaschsvSvOpsOmsRaschSum.COL_NODE_NAME));

            return raschsvSvOpsOmsRaschSum;
        }
    }

    /**
     * Маппинг для Связи РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип
     */
    private static final class RaschsvSvOpsOmsRaschKolRowMapper implements RowMapper<RaschsvSvOpsOmsRaschKol> {
        @Override
        public RaschsvSvOpsOmsRaschKol mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol = new RaschsvSvOpsOmsRaschKol();
            raschsvSvOpsOmsRaschKol.setRaschsvOpsOmsRaschKolId(SqlUtils.getLong(rs, RaschsvSvOpsOmsRaschKol.COL_RASCHSV_OPS_OMS_RASCH_KOL_ID));
            raschsvSvOpsOmsRaschKol.setRaschsvKolLicTipId(SqlUtils.getLong(rs, RaschsvSvOpsOmsRaschKol.COL_RASCHSV_KOL_LIC_TIP_ID));
            raschsvSvOpsOmsRaschKol.setNodeName(rs.getString(RaschsvSvOpsOmsRaschKol.COL_NODE_NAME));

            return raschsvSvOpsOmsRaschKol;
        }
    }
}
