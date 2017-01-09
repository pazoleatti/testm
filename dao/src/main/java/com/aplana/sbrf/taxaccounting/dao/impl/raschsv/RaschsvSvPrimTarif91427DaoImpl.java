package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif91427Dao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif91427;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvedPatent;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplatIt427;
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
public class RaschsvSvPrimTarif91427DaoImpl extends AbstractDao implements RaschsvSvPrimTarif91427Dao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    // Перечень столбцов таблицы "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
    private static final StringBuilder SV_PRIM_TARIF_91427_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif91427.COLUMNS, null));
    private static final StringBuilder SV_PRIM_TARIF_91427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif91427.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения о патенте"
    private static final StringBuilder SVED_PATENT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedPatent.COLUMNS, null));
    private static final StringBuilder SVED_PATENT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedPatent.COLUMNS, ":"));

    // Перечень столбцов таблицы "Итого выплат"
    private static final StringBuilder VYPLAT_IT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt427.COLUMNS, null));
    private static final StringBuilder VYPLAT_IT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt427.COLUMNS, ":"));

    public Long insertRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427) {
        String sql = "INSERT INTO " + RaschsvSvPrimTarif91427.TABLE_NAME +
                " (" + SV_PRIM_TARIF_91427_COLS + ") VALUES (" + SV_PRIM_TARIF_91427_FIELDS + ")";

        raschsvSvPrimTarif91427.setId(generateId(RaschsvSvPrimTarif91427.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvPrimTarif91427.COL_ID, raschsvSvPrimTarif91427.getId())
                .addValue(RaschsvSvPrimTarif91427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvPrimTarif91427.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        // Сохранение "Сведения о патенте"
        List<RaschsvSvedPatent> raschsvSvedPatentList = new ArrayList<RaschsvSvedPatent>();
        for (RaschsvSvedPatent raschsvSvedPatent : raschsvSvPrimTarif91427.getRaschsvSvedPatentList()) {
            // Установка внешнего ключа
            raschsvSvedPatent.setRaschsvSvPrimTarif91427Id(raschsvSvPrimTarif91427.getId());

            // Сохранение "Сведения по суммам (тип 1)"
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvedPatent.getRaschsvSvSum1Tip());
            raschsvSvedPatent.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvSvedPatentList.add(raschsvSvedPatent);
        }
        insertRaschsvSvedPatent(raschsvSvedPatentList);

        // Установка внешнего ключа
        RaschsvVyplatIt427 raschsvVyplatIt427 = raschsvSvPrimTarif91427.getRaschsvVyplatIt427();
        raschsvVyplatIt427.setRaschsvSvPrimTarif91427Id(raschsvSvPrimTarif91427.getId());

        // Сохранение "Сведения по суммам (тип 1)"
        Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvVyplatIt427.getRaschsvSvSum1Tip());
        raschsvVyplatIt427.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

        // Сохранение "Итого выплат"
        insertRaschsvVyplatIt427(raschsvVyplatIt427);

        return raschsvSvPrimTarif91427.getId();
    }

    /**
     * Сохранение "Сведения о патенте"
     * @param raschsvSvedPatentList
     * @return
     */
    private Integer insertRaschsvSvedPatent(List<RaschsvSvedPatent> raschsvSvedPatentList) {
        String sql = "INSERT INTO " + RaschsvSvedPatent.TABLE_NAME +
                " (" + SVED_PATENT_COLS + ") VALUES (" + SVED_PATENT_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvedPatentList.size());
        for (RaschsvSvedPatent raschsvSvedPatent : raschsvSvedPatentList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvSvedPatent.getRaschsvSvPrimTarif91427Id())
                            .addValue(RaschsvSvedPatent.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvedPatent.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvedPatent.COL_NOM_PATENT, raschsvSvedPatent.getNomPatent())
                            .addValue(RaschsvSvedPatent.COL_VYD_DEYAT_PATENT, raschsvSvedPatent.getVydDeyatPatent())
                            .addValue(RaschsvSvedPatent.COL_DATA_NACH_DEYST, raschsvSvedPatent.getDataNachDeyst())
                            .addValue(RaschsvSvedPatent.COL_DATA_KON_DEYST, raschsvSvedPatent.getDataKonDeyst())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvedPatentList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Итого выплат"
     * @param raschsvVyplatIt427
     * @return
     */
    private Long insertRaschsvVyplatIt427(RaschsvVyplatIt427 raschsvVyplatIt427) {
        String sql = "INSERT INTO " + RaschsvVyplatIt427.TABLE_NAME +
                " (" + VYPLAT_IT_COLS + ") VALUES (" + VYPLAT_IT_FIELDS + ")";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvVyplatIt427.getRaschsvSvPrimTarif91427Id())
                .addValue(RaschsvVyplatIt427.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvVyplatIt427.getRaschsvSvSum1Tip().getId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvVyplatIt427.getRaschsvSvSum1Tip().getId();
    }
}
