package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvRashOssZakDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZakRash;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvRashOssZakDaoImpl extends AbstractDao implements RaschsvRashOssZakDao {

    // Перечень столбцов таблицы "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
    private static final StringBuilder RASH_OSS_ZAK_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashOssZak.COLUMNS, null));
    private static final StringBuilder RASH_OSS_ZAK_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashOssZak.COLUMNS, ":"));

    // Перечень столбцов таблицы "Данные по расходам по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
    private static final StringBuilder RASH_OSS_ZAK_RASH_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashOssZakRash.COLUMNS, null));
    private static final StringBuilder RASH_OSS_ZAK_RASH_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashOssZakRash.COLUMNS, ":"));

    public Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak) {
        String sql = "INSERT INTO " + RaschsvRashOssZak.TABLE_NAME +
                " (" + RASH_OSS_ZAK_COLS + ") VALUES (" + RASH_OSS_ZAK_FIELDS + ")";

        raschsvRashOssZak.setId(generateId(RaschsvRashOssZak.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvRashOssZak.COL_ID, raschsvRashOssZak.getId())
                .addValue(RaschsvRashOssZak.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvRashOssZak.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        List<RaschsvRashOssZakRash> raschsvRashOssZakRashList = new ArrayList<RaschsvRashOssZakRash>();

        // Установка внешнего ключа для "Данные по расходам по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
        for (RaschsvRashOssZakRash raschsvRashOssZakRash : raschsvRashOssZak.getRaschsvRashOssZakRashList()) {
            raschsvRashOssZakRash.setRaschsvRashOssZakId(raschsvRashOssZak.getId());
            raschsvRashOssZakRashList.add(raschsvRashOssZakRash);
        }

        // Сохранение "Данные по расходам по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
        insertRaschsvRashOssZakRash(raschsvRashOssZakRashList);

        return raschsvRashOssZak.getId();
    }

    private Integer insertRaschsvRashOssZakRash(List<RaschsvRashOssZakRash> raschsvRashOssZakRashList) {
        String sql = "INSERT INTO " + RaschsvRashOssZakRash.TABLE_NAME +
                " (" + RASH_OSS_ZAK_RASH_COLS + ") VALUES (" + RASH_OSS_ZAK_RASH_FIELDS + ")";

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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvRashOssZakRashList.size()]));

        return res.length;
    }
}
