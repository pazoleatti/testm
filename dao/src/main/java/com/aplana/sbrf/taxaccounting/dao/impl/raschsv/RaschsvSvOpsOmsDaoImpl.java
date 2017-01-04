package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvOpsOmsDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOmsRasch;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOmsRaschKol;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOmsRaschSum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvSvOpsOmsDaoImpl extends AbstractDao implements RaschsvSvOpsOmsDao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    @Autowired
    private RaschsvKolLicTipDao raschsvKolLicTipDao;

    // Перечень столбцов таблицы "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
    private static final StringBuilder SV_OPS_OMS_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOms.COLUMNS, null));
    private static final StringBuilder SV_OPS_OMS_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOms.COLUMNS, ":"));

    // Перечень столбцов таблицы "Вид расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
    private static final StringBuilder SV_OPS_OMS_RASCH_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOmsRasch.COLUMNS, null));
    private static final StringBuilder SV_OPS_OMS_RASCH_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOmsRasch.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сумма для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
    private static final StringBuilder SV_OPS_OMS_RASCH_SUM_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschSum.COLUMNS, null));
    private static final StringBuilder SV_OPS_OMS_RASCH_SUM_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschSum.COLUMNS, ":"));

    // Перечень столбцов таблицы "Количество для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
    private static final StringBuilder SV_OPS_OMS_RASCH_KOL_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschKol.COLUMNS, null));
    private static final StringBuilder SV_OPS_OMS_RASCH_KOL_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvOpsOmsRaschKol.COLUMNS, ":"));

    public Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList) {
        String sql = "INSERT INTO " + RaschsvSvOpsOms.TABLE_NAME +
                " (" + SV_OPS_OMS_COLS + ") VALUES (" + SV_OPS_OMS_FIELDS + ")";

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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvOpsOmsList.size()]));

        List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList = new ArrayList<RaschsvSvOpsOmsRasch>();
        List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList = new ArrayList<RaschsvSvOpsOmsRaschSum>();
        List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList = new ArrayList<RaschsvSvOpsOmsRaschKol>();

        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            // Генерация идентификаторов и Установка внешнего ключа для
            // "Вид расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
            for (RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch : raschsvSvOpsOms.getRaschsvSvOpsOmsRaschList()) {
                raschsvSvOpsOmsRasch.setId(generateId(RaschsvSvOpsOmsRasch.SEQ, Long.class));
                raschsvSvOpsOmsRasch.setRaschsvSvOpsOmsId(raschsvSvOpsOms.getId());
                raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch);

                // Установка внешнего ключа для "Сумма для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
                for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsRaschSumList()) {
                    raschsvSvOpsOmsRaschSum.setRaschsvOpsOmsRaschSumId(raschsvSvOpsOmsRasch.getId());

                    // Сохранение "Сведения по суммам (тип 1)"
                    Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip());
                    raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

                    raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum);
                }

                // Установка внешнего ключа для "Количество для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
                for (RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol : raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsRaschKolList()) {
                    raschsvSvOpsOmsRaschKol.setRaschsvOpsOmsRaschKolId(raschsvSvOpsOmsRasch.getId());

                    // Сохранение "Сведения по количеству физических лиц"
                    Long raschsvKolLicTipId = raschsvKolLicTipDao.insertRaschsvKolLicTip(raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip());
                    raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip().setId(raschsvKolLicTipId);

                    raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol);
                }
            }
        }

        // Сохранение "Вид расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
        if (!raschsvSvOpsOmsRaschList.isEmpty()) {
            insertRaschsvSvOpsOmsRasch(raschsvSvOpsOmsRaschList);
        }

        // Сохранение "Сумма для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
        if (!raschsvSvOpsOmsRaschSumList.isEmpty()) {
            insertRaschsvSvOpsOmsRaschSum(raschsvSvOpsOmsRaschSumList);
        }

        // Сохранение "Количество для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
        if (!raschsvSvOpsOmsRaschKolList.isEmpty()) {
            insertRaschsvSvOpsOmsRaschKol(raschsvSvOpsOmsRaschKolList);
        }

        return res.length;
    }

    /**
     * Сохранение "Вид расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     * @param raschsvSvOpsOmsRaschList
     * @return
     */
    private Integer insertRaschsvSvOpsOmsRasch(List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList) {
        String sql = "INSERT INTO " + RaschsvSvOpsOmsRasch.TABLE_NAME +
                " (" + SV_OPS_OMS_RASCH_COLS + ") VALUES (" + SV_OPS_OMS_RASCH_FIELDS + ")";

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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvOpsOmsRaschList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Сумма для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     * @param raschsvSvOpsOmsRaschSumList
     * @return
     */
    private Integer insertRaschsvSvOpsOmsRaschSum(List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList) {
        String sql = "INSERT INTO " + RaschsvSvOpsOmsRaschSum.TABLE_NAME +
                " (" + SV_OPS_OMS_RASCH_SUM_COLS + ") VALUES (" + SV_OPS_OMS_RASCH_SUM_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsRaschSumList.size());
        for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRaschSumList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOmsRaschSum.COL_RASCHSV_OPS_OMS_RASCH_SUM_ID, raschsvSvOpsOmsRaschSum.getRaschsvOpsOmsRaschSumId())
                            .addValue(RaschsvSvOpsOmsRaschSum.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvOpsOmsRaschSum.COL_NODE_NAME, raschsvSvOpsOmsRaschSum.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvOpsOmsRaschSumList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Количество для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     * @param raschsvSvOpsOmsRaschKolList
     * @return
     */
    private Integer insertRaschsvSvOpsOmsRaschKol(List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList) {
        String sql = "INSERT INTO " + RaschsvSvOpsOmsRaschKol.TABLE_NAME +
                " (" + SV_OPS_OMS_RASCH_KOL_COLS + ") VALUES (" + SV_OPS_OMS_RASCH_KOL_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvOpsOmsRaschKolList.size());
        for (RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol : raschsvSvOpsOmsRaschKolList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvOpsOmsRaschKol.COL_RASCHSV_OPS_OMS_RASCH_KOL_ID, raschsvSvOpsOmsRaschKol.getRaschsvOpsOmsRaschKolId())
                            .addValue(RaschsvSvOpsOmsRaschKol.COL_RASCHSV_KOL_LIC_TIP_ID, raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip().getId())
                            .addValue(RaschsvSvOpsOmsRaschKol.COL_NODE_NAME, raschsvSvOpsOmsRaschKol.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvOpsOmsRaschKolList.size()]));

        return res.length;
    }
}
