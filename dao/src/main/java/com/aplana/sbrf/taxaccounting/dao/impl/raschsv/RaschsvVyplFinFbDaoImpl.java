package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvVyplFinFbDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplPrichina;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvVyplFinFbDaoImpl extends AbstractDao implements RaschsvVyplFinFbDao {

    // Перечень столбцов таблицы "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
    private static final StringBuilder VYPL_FIN_FB_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplFinFb.COLUMNS, null));
    private static final StringBuilder VYPL_FIN_FB_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplFinFb.COLUMNS, ":"));

    // Перечень столбцов таблицы "Основание выплат, произведенных за счет средств, финансируемых из федерального бюджета"
    private static final StringBuilder VYPL_PRICHINA_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplPrichina.COLUMNS, null));
    private static final StringBuilder VYPL_PRICHINA_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplPrichina.COLUMNS, ":"));

    // Перечень столбцов таблицы "Информация о выплате, произведенной за счет средств, финансируемых из Федерального бюджета"
    private static final StringBuilder RASH_VYPL_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashVypl.COLUMNS, null));
    private static final StringBuilder RASH_VYPL_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashVypl.COLUMNS, ":"));

    public Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb) {
        String sql = "INSERT INTO " + RaschsvVyplFinFb.TABLE_NAME +
                " (" + VYPL_FIN_FB_COLS + ") VALUES (" + VYPL_FIN_FB_FIELDS + ")";

        raschsvVyplFinFb.setId(generateId(RaschsvVyplFinFb.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplFinFb.COL_ID, raschsvVyplFinFb.getId())
                .addValue(RaschsvVyplFinFb.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvVyplFinFb.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        List<RaschsvVyplPrichina> raschsvVyplPrichinaList = new ArrayList<RaschsvVyplPrichina>();
        List<RaschsvRashVypl> raschsvRashVyplList = new ArrayList<RaschsvRashVypl>();

        for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplFinFb.getRaschsvVyplPrichinaList()) {
            // Установка внешнего ключа для "Основание выплат, произведенных за счет средств, финансируемых из федерального бюджета"
            raschsvVyplPrichina.setRaschsvVyplFinFbId(raschsvVyplFinFb.getId());
            raschsvVyplPrichina.setId(generateId(RaschsvVyplPrichina.SEQ, Long.class));
            raschsvVyplPrichinaList.add(raschsvVyplPrichina);

            // Установка внешнего ключа для "Информация о выплате, произведенной за счет средств, финансируемых из Федерального бюджета"
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.getRaschsvRashVyplList()) {
                raschsvRashVypl.setRaschsvVyplPrichinaId(raschsvVyplPrichina.getId());
                raschsvRashVypl.setId(generateId(RaschsvRashVypl.SEQ, Long.class));
                raschsvRashVyplList.add(raschsvRashVypl);
            }
        }

        // Сохранение "Основание выплат, произведенных за счет средств, финансируемых из федерального бюджета"
        insertRaschsvVyplPrichina(raschsvVyplPrichinaList);

        // Сохранение "Информация о выплате, произведенной за счет средств, финансируемых из Федерального бюджета"
        insertRaschsvRashVypl(raschsvRashVyplList);

        return raschsvVyplFinFb.getId();
    }

    /**
     * Сохранение "Основание выплат, произведенных за счет средств, финансируемых из федерального бюджета"
     * @param raschsvVyplPrichinaList
     * @return
     */
    private Integer insertRaschsvVyplPrichina(List<RaschsvVyplPrichina> raschsvVyplPrichinaList) {
        String sql = "INSERT INTO " + RaschsvVyplPrichina.TABLE_NAME +
                " (" + VYPL_PRICHINA_COLS + ") VALUES (" + VYPL_PRICHINA_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvVyplPrichinaList.size());
        for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplPrichinaList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvVyplPrichina.COL_ID, raschsvVyplPrichina.getId())
                            .addValue(RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID, raschsvVyplPrichina.getRaschsvVyplFinFbId())
                            .addValue(RaschsvVyplPrichina.COL_NODE_NAME, raschsvVyplPrichina.getNodeName())
                            .addValue(RaschsvVyplPrichina.COL_SV_VNF_UHOD_INV, raschsvVyplPrichina.getSvVnfUhodInv())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvVyplPrichinaList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Информация о выплате, произведенной за счет средств, финансируемых из Федерального бюджета"
     * @param raschsvRashVyplList
     * @return
     */
    private Integer insertRaschsvRashVypl(List<RaschsvRashVypl> raschsvRashVyplList) {
        String sql = "INSERT INTO " + RaschsvRashVypl.TABLE_NAME +
                " (" + RASH_VYPL_COLS + ") VALUES (" + RASH_VYPL_FIELDS + ")";

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvRashVyplList.size());
        for (RaschsvRashVypl raschsvRashVypl : raschsvRashVyplList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvRashVypl.COL_ID, raschsvRashVypl.getId())
                            .addValue(RaschsvRashVypl.COL_RASCHSV_VYPL_PRICHINA_ID, raschsvRashVypl.getRaschsvVyplPrichinaId())
                            .addValue(RaschsvRashVypl.COL_NODE_NAME, raschsvRashVypl.getNodeName())
                            .addValue(RaschsvRashVypl.COL_CHISL_POLUCH, raschsvRashVypl.getChislPoluch())
                            .addValue(RaschsvRashVypl.COL_KOL_VYPL, raschsvRashVypl.getKolVypl())
                            .addValue(RaschsvRashVypl.COL_RASHOD, raschsvRashVypl.getRashod())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvRashVyplList.size()]));

        return res.length;
    }
}
