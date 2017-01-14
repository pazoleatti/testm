package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif13422Dao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif13422;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvReestrMdo;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvedObuch;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplatIt422;
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
public class RaschsvSvPrimTarif13422DaoImpl extends AbstractDao implements RaschsvSvPrimTarif13422Dao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    // Перечень столбцов таблицы "Сведения об обучающихся, необходимые для применения положений подпункта 1 пункта 3 статьи 422"
    private static final StringBuilder SV_PRIM_TARIF_13422_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif13422.COLUMNS, null));
    private static final StringBuilder SV_PRIM_TARIF_13422_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif13422.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения об обучающихся"
    private static final StringBuilder SVED_OBUCH_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedObuch.COLUMNS, null));
    private static final StringBuilder SVED_OBUCH_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedObuch.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой"
    private static final StringBuilder SV_REESTR_MDO_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvReestrMdo.COLUMNS, null));
    private static final StringBuilder SV_REESTR_MDO_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvReestrMdo.COLUMNS, ":"));

    // Перечень столбцов таблицы "Итого выплат"
    private static final StringBuilder VYPLAT_IT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt422.COLUMNS, null));
    private static final StringBuilder VYPLAT_IT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt422.COLUMNS, ":"));

    public Long insertRaschsvSvPrimTarif13422(RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422) {
        String sql = "INSERT INTO " + RaschsvSvPrimTarif13422.TABLE_NAME +
                " (" + SV_PRIM_TARIF_13422_COLS + ") VALUES (" + SV_PRIM_TARIF_13422_FIELDS + ")";

        // Генерация идентификатора
        raschsvSvPrimTarif13422.setId(generateId(RaschsvSvPrimTarif13422.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvPrimTarif13422.COL_ID, raschsvSvPrimTarif13422.getId())
                .addValue(RaschsvSvPrimTarif13422.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvPrimTarif13422.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        // Сохранение "Сведения об обучающихся"
        List<RaschsvSvedObuch> raschsvSvedObuchList = new ArrayList<RaschsvSvedObuch>();
        List<RaschsvSvReestrMdo> raschsvSvReestrMdoList = new ArrayList<RaschsvSvReestrMdo>();
        for (RaschsvSvedObuch raschsvSvedObuch : raschsvSvPrimTarif13422.getRaschsvSvedObuchList()) {
            // Генерация идентификатора
            raschsvSvedObuch.setId(generateId(RaschsvSvedObuch.SEQ, Long.class));

            for (RaschsvSvReestrMdo raschsvSvReestrMdo : raschsvSvedObuch.getRaschsvSvReestrMdoList()) {
                raschsvSvReestrMdo.setId(generateId(RaschsvSvReestrMdo.SEQ, Long.class));
                raschsvSvReestrMdo.setRaschsvSvedObuchId(raschsvSvedObuch.getId());
                raschsvSvReestrMdoList.add(raschsvSvReestrMdo);
            }

            // Установка внешнего ключа
            raschsvSvedObuch.setRaschsvSvPrimTarif1422Id(raschsvSvPrimTarif13422.getId());

            // Сохранение "Сведения по суммам (тип 1)"
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvedObuch.getRaschsvSvSum1Tip());
            raschsvSvedObuch.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvSvedObuchList.add(raschsvSvedObuch);
        }
        // Сохранение "Сведения об обучающихся"
        insertRaschsvSvedObuch(raschsvSvedObuchList);

        // Сохранение "Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой"
        insertRaschsvSvReestrMdo(raschsvSvReestrMdoList);

        // Установка внешнего ключа
        RaschsvVyplatIt422 raschsvVyplatIt422 = raschsvSvPrimTarif13422.getRaschsvVyplatIt422();
        raschsvVyplatIt422.setRaschsvSvPrimTarif1422Id(raschsvSvPrimTarif13422.getId());

        // Сохранение "Сведения по суммам (тип 1)"
        Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvVyplatIt422.getRaschsvSvSum1Tip());
        raschsvVyplatIt422.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

        // Сохранение "Итого выплат"
        insertRaschsvVyplatIt422(raschsvVyplatIt422);

        return raschsvSvPrimTarif13422.getId();
    }

    /**
     * Сохранение "Сведения об обучающихся"
     * @param raschsvSvedObuchList
     * @return
     */
    private Integer insertRaschsvSvedObuch(List<RaschsvSvedObuch> raschsvSvedObuchList) {
        String sql = "INSERT INTO " + RaschsvSvedObuch.TABLE_NAME +
                " (" + SVED_OBUCH_COLS + ") VALUES (" + SVED_OBUCH_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvedObuchList.size());
        for (RaschsvSvedObuch raschsvSvedObuch : raschsvSvedObuchList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvSvedObuch.getRaschsvSvPrimTarif1422Id())
                            .addValue(RaschsvSvedObuch.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvedObuch.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvedObuch.COL_ID, raschsvSvedObuch.getId())
                            .addValue(RaschsvSvedObuch.COL_UNIK_NOMER, raschsvSvedObuch.getUnikNomer())
                            .addValue(RaschsvSvedObuch.COL_FAMILIA, raschsvSvedObuch.getFamilia())
                            .addValue(RaschsvSvedObuch.COL_IMYA, raschsvSvedObuch.getImya())
                            .addValue(RaschsvSvedObuch.COL_MIDDLE_NAME, raschsvSvedObuch.getMiddleName())
                            .addValue(RaschsvSvedObuch.COL_SPRAV_NOMER, raschsvSvedObuch.getSpravNomer())
                            .addValue(RaschsvSvedObuch.COL_SPRAV_DATA, raschsvSvedObuch.getSpravData())
                            .addValue(RaschsvSvedObuch.COL_SPRAV_NODE_NAME, raschsvSvedObuch.getSpravNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvedObuchList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой"
     * @param raschsvSvReestrMdoList
     * @return
     */
    private Integer insertRaschsvSvReestrMdo(List<RaschsvSvReestrMdo> raschsvSvReestrMdoList) {
        String sql = "INSERT INTO " + RaschsvSvReestrMdo.TABLE_NAME +
                " (" + SV_REESTR_MDO_COLS + ") VALUES (" + SV_REESTR_MDO_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvReestrMdoList.size());
        for (RaschsvSvReestrMdo raschsvSvReestrMdo : raschsvSvReestrMdoList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvReestrMdo.COL_RASCHSV_SVED_OBUCH_ID, raschsvSvReestrMdo.getRaschsvSvedObuchId())
                            .addValue(RaschsvSvReestrMdo.COL_ID, raschsvSvReestrMdo.getId())
                            .addValue(RaschsvSvReestrMdo.COL_NAIM_MDO, raschsvSvReestrMdo.getNaimMdo())
                            .addValue(RaschsvSvReestrMdo.COL_DATA_ZAPIS, raschsvSvReestrMdo.getDataZapis())
                            .addValue(RaschsvSvReestrMdo.COL_NOMER_ZAPIS, raschsvSvReestrMdo.getNomerZapis())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvReestrMdoList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Итого выплат"
     * @param raschsvVyplatIt422
     * @return
     */
    private Long insertRaschsvVyplatIt422(RaschsvVyplatIt422 raschsvVyplatIt422) {
        String sql = "INSERT INTO " + RaschsvVyplatIt422.TABLE_NAME +
                " (" + VYPLAT_IT_COLS + ") VALUES (" + VYPLAT_IT_FIELDS + ")";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvVyplatIt422.getRaschsvSvPrimTarif1422Id())
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvVyplatIt422.getRaschsvSvSum1Tip().getId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvVyplatIt422.getRaschsvSvSum1Tip().getId();
    }
}
