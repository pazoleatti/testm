package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvOssVnmDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnmKol;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnmSum;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplSvPrev;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvOssVnmDaoImpl extends AbstractDao implements RaschsvOssVnmDao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    @Autowired
    private RaschsvKolLicTipDao raschsvKolLicTipDao;

    // Перечень столбцов таблицы "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private static final StringBuilder OSS_VNM_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvOssVnm.COLUMNS, null));
    private static final StringBuilder OSS_VNM_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvOssVnm.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сумма страховых взносов, подлежащая к уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)"
    private static final StringBuilder UPL_SV_PREV_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplSvPrev.COLUMNS, null));
    private static final StringBuilder UPL_SV_PREV_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvUplSvPrev.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сумма для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private static final StringBuilder OSS_VNM_SUM_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvOssVnmSum.COLUMNS, null));
    private static final StringBuilder OSS_VNM_SUM_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvOssVnmSum.COLUMNS, ":"));

    // Перечень столбцов таблицы "Количество для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private static final StringBuilder OSS_VNM_KOL_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvOssVnmKol.COLUMNS, null));
    private static final StringBuilder OSS_VNM_KOL_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvOssVnmKol.COLUMNS, ":"));

    public Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm) {

        String sql = "INSERT INTO " + RaschsvOssVnm.TABLE_NAME +
                " (" + OSS_VNM_COLS + ") VALUES (" + OSS_VNM_FIELDS + ")";

        raschsvOssVnm.setId(generateId(RaschsvOssVnm.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvOssVnm.COL_ID, raschsvOssVnm.getId())
                .addValue(RaschsvOssVnm.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvOssVnm.getRaschsvObyazPlatSvId())
                .addValue(RaschsvOssVnm.COL_PRIZ_VYPL, raschsvOssVnm.getPrizVypl());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        List<RaschsvUplSvPrev> raschsvUplSvPrevList = new ArrayList<RaschsvUplSvPrev>();
        List<RaschsvOssVnmKol> raschsvOssVnmKolList = new ArrayList<RaschsvOssVnmKol>();
        List<RaschsvOssVnmSum> raschsvOssVnmSumList = new ArrayList<RaschsvOssVnmSum>();

        // Установка внешнего ключа для "Сумма страховых взносов, подлежащая к уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)"
        for (RaschsvUplSvPrev raschsvUplSvPrev : raschsvOssVnm.getRaschsvUplSvPrevList()) {
            raschsvUplSvPrev.setRaschsvOssVnmId(raschsvOssVnm.getId());
            raschsvUplSvPrevList.add(raschsvUplSvPrev);
        }
        insertRaschsvUplSvPrev(raschsvUplSvPrevList);

        // Установка внешнего ключа для "Количество для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
        for (RaschsvOssVnmKol raschsvOssVnmKol : raschsvOssVnm.getRaschsvOssVnmKolList()) {
            raschsvOssVnmKol.setRaschsvOssVnmId(raschsvOssVnm.getId());

            // Сохранение "Сведения по количеству физических лиц"
            Long raschsvKolLicTipId = raschsvKolLicTipDao.insertRaschsvKolLicTip(raschsvOssVnmKol.getRaschsvKolLicTip());
            raschsvOssVnmKol.getRaschsvKolLicTip().setId(raschsvKolLicTipId);

            raschsvOssVnmKolList.add(raschsvOssVnmKol);
        }
        // Сохранение "Количество для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
        insertRaschsvOssVnmKol(raschsvOssVnmKolList);

        // Установка внешнего ключа для "Сумма для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
        for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnm.getRaschsvOssVnmSumList()) {
            raschsvOssVnmSum.setRaschsvOssVnmId(raschsvOssVnm.getId());

            // Сохранение "Сведения по суммам (тип 1)"
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvOssVnmSum.getRaschsvSvSum1Tip());
            raschsvOssVnmSum.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvOssVnmSumList.add(raschsvOssVnmSum);
        }
        // Сохранение "Сумма для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
        insertRaschsvOssVnmSum(raschsvOssVnmSumList);

        return raschsvOssVnm.getId();
    }

    /**
     * Сохранение "Сумма страховых взносов, подлежащая к уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)"
     * @param raschsvUplSvPrevList
     * @return
     */
    private Integer insertRaschsvUplSvPrev(List<RaschsvUplSvPrev> raschsvUplSvPrevList) {
        String sql = "INSERT INTO " + RaschsvUplSvPrev.TABLE_NAME +
                " (" + UPL_SV_PREV_COLS + ") VALUES (" + UPL_SV_PREV_FIELDS + ")";

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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvUplSvPrevList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Количество для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvOssVnmKolList
     * @return
     */
    private Integer insertRaschsvOssVnmKol(List<RaschsvOssVnmKol> raschsvOssVnmKolList) {
        String sql = "INSERT INTO " + RaschsvOssVnmKol.TABLE_NAME +
                " (" + OSS_VNM_KOL_COLS + ") VALUES (" + OSS_VNM_KOL_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvOssVnmKolList.size());
        for (RaschsvOssVnmKol raschsvOssVnmKol : raschsvOssVnmKolList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvOssVnmKol.COL_RASCHSV_OSS_VNM_ID, raschsvOssVnmKol.getRaschsvOssVnmId())
                            .addValue(RaschsvOssVnmKol.COL_RASCHSV_KOL_LIC_TIP_ID, raschsvOssVnmKol.getRaschsvKolLicTip().getId())
                            .addValue(RaschsvOssVnmKol.COL_NODE_NAME, raschsvOssVnmKol.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvOssVnmKolList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Сумма для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvOssVnmSumList
     * @return
     */
    private Integer insertRaschsvOssVnmSum(List<RaschsvOssVnmSum> raschsvOssVnmSumList) {
        String sql = "INSERT INTO " + RaschsvOssVnmSum.TABLE_NAME +
                " (" + OSS_VNM_SUM_COLS + ") VALUES (" + OSS_VNM_SUM_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvOssVnmSumList.size());
        for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnmSumList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvOssVnmSum.COL_RASCHSV_OSS_VNM_ID, raschsvOssVnmSum.getRaschsvOssVnmId())
                            .addValue(RaschsvOssVnmSum.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvOssVnmSum.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvOssVnmSum.COL_NODE_NAME, raschsvOssVnmSum.getNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvOssVnmSumList.size()]));

        return res.length;
    }
}
