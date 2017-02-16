package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvKolLicTip;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;

@Repository
@Transactional
public class RaschsvKolLicTipDaoImpl extends AbstractDao implements RaschsvKolLicTipDao {

    // Перечень столбцов таблицы КолЛицТип
    private static final StringBuilder KOL_LIC_TIP_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvKolLicTip.COLUMNS, null));
    private static final StringBuilder KOL_LIC_TIP_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvKolLicTip.COLUMNS, ":"));

    public Long insertRaschsvKolLicTip(RaschsvKolLicTip raschsvKolLicTip) {
        String sql = "INSERT INTO " + RaschsvKolLicTip.TABLE_NAME +
                " (" + KOL_LIC_TIP_COLS + ") VALUES (" + KOL_LIC_TIP_FIELDS + ")";
        raschsvKolLicTip.setId(generateId(RaschsvKolLicTip.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvKolLicTip.COL_ID, raschsvKolLicTip.getId())
                .addValue(RaschsvKolLicTip.COL_KOL_VSEGO_PER, raschsvKolLicTip.getKolVsegoPer())
                .addValue(RaschsvKolLicTip.COL_KOL_VSEGO_POSL_3M, raschsvKolLicTip.getKolVsegoPosl3m())
                .addValue(RaschsvKolLicTip.COL_KOL_1M_POSL_3M, raschsvKolLicTip.getKol1mPosl3m())
                .addValue(RaschsvKolLicTip.COL_KOL_2M_POSL_3M, raschsvKolLicTip.getKol2mPosl3m())
                .addValue(RaschsvKolLicTip.COL_KOL_3M_POSL_3M, raschsvKolLicTip.getKol3mPosl3m());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvKolLicTip.getId();
    }

    @Override
    public void deleteRaschsvKolLicTipByDeclarationDataId(Long declarationDataId) {
        try {
            getJdbcTemplate().update(
                    "DELETE FROM raschsv_kol_lic_tip WHERE id in ( " +
                        "select " +
                            "klt.id " +
                        "from " +
                            "raschsv_obyaz_plat_sv ops " +
                            "inner join raschsv_oss_vnm ov on (ops.id = ov.raschsv_obyaz_plat_sv_id) " +
                            "inner join raschsv_oss_vnm_kol ovk on (ov.id = ovk.raschsv_oss_vnm_id) " +
                            "inner join raschsv_kol_lic_tip klt on (ovk.raschsv_kol_lic_tip_id = klt.id) " +
                        "where " +
                            "ops.declaration_data_id = ? " +
                    ") ",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER}
            );

            getJdbcTemplate().update(
                    "DELETE FROM raschsv_kol_lic_tip WHERE id in (" +
                        "select " +
                            "rklt.id " +
                        "from " +
                            "raschsv_obyaz_plat_sv ops " +
                            "inner join raschsv_sv_ops_oms rsvoo on (ops.id = rsvoo.raschsv_obyaz_plat_sv_id) " +
                            "inner join raschsv_sv_ops_oms_rasch rsoor on (rsvoo.id = rsoor.raschsv_sv_ops_oms_id) " +
                            "inner join raschsv_ops_oms_rasch_kol roork on (roork.raschsv_ops_oms_rasch_kol_id = rsoor.id) " +
                            "inner join raschsv_kol_lic_tip rklt on (rklt.id = roork.raschsv_kol_lic_tip_id) " +
                        "where "+
                            "ops.declaration_data_id = ? " +
                    ") ",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER}
            );
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи с raschsv_kol_lic_tip = %d", declarationDataId), e);
        }
    }
}
