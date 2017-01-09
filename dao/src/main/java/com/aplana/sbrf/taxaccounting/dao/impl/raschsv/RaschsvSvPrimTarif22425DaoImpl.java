package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif22425Dao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvInoGrazd;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif22425;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplatIt425;
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
public class RaschsvSvPrimTarif22425DaoImpl extends AbstractDao implements RaschsvSvPrimTarif22425Dao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    // Перечень столбцов таблицы "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426) Налогового кодекса Российской Федерации"
    private static final StringBuilder SV_PRIM_TARIF2_425_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif22425.COLUMNS, null));
    private static final StringBuilder SV_PRIM_TARIF2_425_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif22425.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения об иностранных гражданах, лицах без гражданства"
    private static final StringBuilder SVED_INO_GRAZD_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvInoGrazd.COLUMNS, null));
    private static final StringBuilder SVED_INO_GRAZD_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvInoGrazd.COLUMNS, ":"));

    // Перечень столбцов таблицы "Итого выплат"
    private static final StringBuilder VYPLAT_IT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt425.COLUMNS, null));
    private static final StringBuilder VYPLAT_IT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt425.COLUMNS, ":"));

    public Long insertRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425) {
        String sql = "INSERT INTO " + RaschsvSvPrimTarif22425.TABLE_NAME +
                " (" + SV_PRIM_TARIF2_425_COLS + ") VALUES (" + SV_PRIM_TARIF2_425_FIELDS + ")";

        raschsvSvPrimTarif22425.setId(generateId(RaschsvSvPrimTarif22425.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvPrimTarif22425.COL_ID, raschsvSvPrimTarif22425.getId())
                .addValue(RaschsvSvPrimTarif22425.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvPrimTarif22425.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        // Сохранение "Сведения о патенте"
        List<RaschsvSvInoGrazd> raschsvSvedPatentList = new ArrayList<RaschsvSvInoGrazd>();
        for (RaschsvSvInoGrazd raschsvSvedPatent : raschsvSvPrimTarif22425.getRaschsvSvInoGrazdList()) {
            // Установка внешнего ключа
            raschsvSvedPatent.setRaschsvSvPrimTarif2425Id(raschsvSvPrimTarif22425.getId());

            // Сохранение "Сведения по суммам (тип 1)"
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvedPatent.getRaschsvSvSum1Tip());
            raschsvSvedPatent.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvSvedPatentList.add(raschsvSvedPatent);
        }
        insertRaschsvSvInoGrazd(raschsvSvedPatentList);

        // Установка внешнего ключа
        RaschsvVyplatIt425 raschsvVyplatIt425 = raschsvSvPrimTarif22425.getRaschsvVyplatIt425();
        raschsvVyplatIt425.setRaschsvSvPrimTarif22425Id(raschsvSvPrimTarif22425.getId());

        // Сохранение "Сведения по суммам (тип 1)"
        Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvVyplatIt425.getRaschsvSvSum1Tip());
        raschsvVyplatIt425.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

        // Сохранение "Итого выплат"
        insertRaschsvVyplatIt425(raschsvVyplatIt425);

        return raschsvSvPrimTarif22425.getId();
    }

    /**
     * Сохранение "Сведения об иностранных гражданах, лицах без гражданства"
     * @param raschsvSvInoGrazdList
     * @return
     */
    private Integer insertRaschsvSvInoGrazd(List<RaschsvSvInoGrazd> raschsvSvInoGrazdList) {
        String sql = "INSERT INTO " + RaschsvSvInoGrazd.TABLE_NAME +
                " (" + SVED_INO_GRAZD_COLS + ") VALUES (" + SVED_INO_GRAZD_FIELDS + ")";

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
                            .addValue(RaschsvSvInoGrazd.COL_MIDDLE_NAME, raschsvSvInoGrazd.getMiddleName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvInoGrazdList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Итого выплат"
     * @param raschsvVyplatIt425
     * @return
     */
    private Long insertRaschsvVyplatIt425(RaschsvVyplatIt425 raschsvVyplatIt425) {
        String sql = "INSERT INTO " + RaschsvVyplatIt425.TABLE_NAME +
                " (" + VYPLAT_IT_COLS + ") VALUES (" + VYPLAT_IT_FIELDS + ")";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt425.COL_RASCHSV_SV_PRIM_TARIF2_425_ID, raschsvVyplatIt425.getRaschsvSvPrimTarif22425Id())
                .addValue(RaschsvVyplatIt425.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvVyplatIt425.getRaschsvSvSum1Tip().getId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvVyplatIt425.getRaschsvSvSum1Tip().getId();
    }
}
