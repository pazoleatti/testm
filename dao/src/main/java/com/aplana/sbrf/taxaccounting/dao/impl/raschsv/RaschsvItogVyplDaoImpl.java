package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvItogVyplDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class RaschsvItogVyplDaoImpl extends AbstractDao implements RaschsvItogVyplDao {
    private static final StringBuilder KOL_LIC_ITOG_STRAH_LIC_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvItogStrahLic.COLUMNS, null));
    private static final StringBuilder KOL_LIC_ITOG_STRAH_LIC_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvItogStrahLic.COLUMNS, ":"));

    private static final StringBuilder KOL_LIC_ITOG_VYPL_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvItogVypl.COLUMNS, null));
    private static final StringBuilder KOL_LIC_ITOG_VYPL_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvItogVypl.COLUMNS, ":"));

    private static final StringBuilder KOL_LIC_ITOG_VYPL_DOP_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvItogVyplDop.COLUMNS, null));
    private static final StringBuilder KOL_LIC_ITOG_VYPL_DOP_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvItogVyplDop.COLUMNS, ":"));

    @Override
    public Long insertItogStrahLic(RaschsvItogStrahLic raschsvItogStrahLic) {
        String sql =
                "INSERT INTO " + RaschsvItogStrahLic.TABLE_NAME + " " +
                        "(" + KOL_LIC_ITOG_STRAH_LIC_COLS + ") " +
                        "VALUES(" + KOL_LIC_ITOG_STRAH_LIC_FIELDS + ")";

        raschsvItogStrahLic.setId(generateId(RaschsvItogStrahLic.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvItogStrahLic.COL_ID, raschsvItogStrahLic.getId())
                .addValue(RaschsvItogStrahLic.COL_DECLARATION_DATA_ID, raschsvItogStrahLic.getDeclarationDataId())
                .addValue(RaschsvItogStrahLic.COL_KOL_LIC, raschsvItogStrahLic.getKolLic());
        getNamedParameterJdbcTemplate().update(sql, params);

        return raschsvItogStrahLic.getId();

    }

    @Override
    public int[] insertItogVypl(Collection<RaschsvItogVypl> raschsvItogVypls) {
        String sql =
                "INSERT INTO " + RaschsvItogVypl.TABLE_NAME + " " +
                "(" + KOL_LIC_ITOG_VYPL_COLS + ") " +
                "VALUES(" + KOL_LIC_ITOG_VYPL_FIELDS + ")";

        List<Map<String, ?>> batchValues = new ArrayList<Map<String, ?>>(raschsvItogVypls.size());
        for (RaschsvItogVypl raschsvItogVypl : raschsvItogVypls) {
            raschsvItogVypl.setId(generateId(RaschsvItogVypl.SEQ, Long.class));

            batchValues.add(
                    new MapSqlParameterSource()
                            .addValue(RaschsvItogVypl.COL_ID, raschsvItogVypl.getId())
                            .addValue(RaschsvItogVypl.COL_RASCHSV_ITOG_STRAH_LIC_ID, raschsvItogVypl.getRaschsvItogStrahLicId())
                            .addValue(RaschsvItogVypl.COL_MESYAC, raschsvItogVypl.getMesyac())
                            .addValue(RaschsvItogVypl.COL_KOD_KAT_LIC, raschsvItogVypl.getKodKatLic())
                            .addValue(RaschsvItogVypl.COL_KOL_FL, raschsvItogVypl.getKolFl())
                            .addValue(RaschsvItogVypl.COL_SUM_VYPL, raschsvItogVypl.getSumVypl())
                            .addValue(RaschsvItogVypl.COL_VYPL_OPS, raschsvItogVypl.getVyplOps())
                            .addValue(RaschsvItogVypl.COL_VYPL_OPS_DOG, raschsvItogVypl.getVyplOpsDog())
                            .addValue(RaschsvItogVypl.COL_SUM_NACHISL, raschsvItogVypl.getSumNachisl())
                            .getValues()
            );
        }

        return getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[raschsvItogVypls.size()]));
    }

    @Override
    public int[] insertItogVyplDop(Collection<RaschsvItogVyplDop> raschsvItogVyplDops) {
        String sql =
                "INSERT INTO " + RaschsvItogVyplDop.TABLE_NAME + " " +
                        "(" + KOL_LIC_ITOG_VYPL_DOP_COLS + ") " +
                        "VALUES(" + KOL_LIC_ITOG_VYPL_DOP_FIELDS + ")";

        List<Map<String, ?>> batchValues = new ArrayList<Map<String, ?>>(raschsvItogVyplDops.size());
        for (RaschsvItogVyplDop raschsvItogVyplDop : raschsvItogVyplDops) {
            raschsvItogVyplDop.setId(generateId(RaschsvItogVyplDop.SEQ, Long.class));

            batchValues.add(
                    new MapSqlParameterSource()
                            .addValue(RaschsvItogVyplDop.COL_ID, raschsvItogVyplDop.getId())
                            .addValue(RaschsvItogVyplDop.COL_RASCHSV_ITOG_STRAH_LIC_ID, raschsvItogVyplDop.getRaschsvItogStrahLicId())
                            .addValue(RaschsvItogVyplDop.COL_MESYAC, raschsvItogVyplDop.getMesyac())
                            .addValue(RaschsvItogVyplDop.COL_TARIF, raschsvItogVyplDop.getTarif())
                            .addValue(RaschsvItogVyplDop.COL_KOL_FL, raschsvItogVyplDop.getKolFl())
                            .addValue(RaschsvItogVyplDop.COL_SUM_VYPL, raschsvItogVyplDop.getSumVypl())
                            .addValue(RaschsvItogVyplDop.COL_SUM_NACHISL, raschsvItogVyplDop.getSumNachisl())
                            .getValues()
            );
        }

        return getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[raschsvItogVyplDops.size()]));
    }
}
