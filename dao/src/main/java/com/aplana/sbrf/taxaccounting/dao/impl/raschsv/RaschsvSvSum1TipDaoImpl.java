package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvSum1Tip;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;

@Repository
@Transactional
public class RaschsvSvSum1TipDaoImpl extends AbstractDao implements RaschsvSvSum1TipDao {

    // Перечень столбцов таблицы СвСум1Тип
    public static final String SUM_COLS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, null);
    public static final String SUM_FIELDS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, ":");

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvSvSum1Tip.TABLE_NAME +
            " (" + SUM_COLS + ") VALUES (" + SUM_FIELDS + ")";

    public Long insertRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        raschsvSvSum1Tip.setId(generateId(RaschsvSvSum1Tip.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvSum1Tip.COL_ID, raschsvSvSum1Tip.getId())
                .addValue(RaschsvSvSum1Tip.COL_SUM_VSEGO_PER, raschsvSvSum1Tip.getSumVsegoPer())
                .addValue(RaschsvSvSum1Tip.COL_SUM_VSEGO_POSL_3M, raschsvSvSum1Tip.getSumVsegoPosl3m())
                .addValue(RaschsvSvSum1Tip.COL_SUM_1M_POSL_3M, raschsvSvSum1Tip.getSum1mPosl3m())
                .addValue(RaschsvSvSum1Tip.COL_SUM_2M_POSL_3M, raschsvSvSum1Tip.getSum2mPosl3m())
                .addValue(RaschsvSvSum1Tip.COL_SUM_3M_POSL_3M, raschsvSvSum1Tip.getSum3mPosl3m());
        getNamedParameterJdbcTemplate().update(SQL_INSERT.toString(), params);

        return raschsvSvSum1Tip.getId();
    }

    @Override
    public void deleteRaschsvSvSum1TipByDeclarationDataId(Long declarationDataId) {
        try {
            getJdbcTemplate().update(
                    "DELETE FROM raschsv_sv_sum_1tip WHERE id in ( " +
                        "select " +
                            "sst.id " +
                        "from " +
                            "raschsv_obyaz_plat_sv ops " +
                            "inner join raschsv_oss_vnm ov on (ov.raschsv_obyaz_plat_sv_id = ops.id) " +
                            "inner join raschsv_oss_vnm_sum ovs on (ovs.raschsv_oss_vnm_id = ov.id) " +
                            "inner join raschsv_sv_sum_1tip sst on (sst.id = ovs.raschsv_sv_sum1_tip_id) " +
                        "where " +
                        "   ops.declaration_data_id = ? " +
                    ") ",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER}
            );

            getJdbcTemplate().update(
                    "DELETE FROM raschsv_sv_sum_1tip WHERE id in ( " +
                        "select " +
                            "rsst.id " +
                        "from " +
                            "raschsv_obyaz_plat_sv ops " +
                            "inner join raschsv_sv_ops_oms rsvoo on (ops.id = rsvoo.raschsv_obyaz_plat_sv_id) " +
                            "inner join raschsv_sv_ops_oms_rasch rsoor on (rsvoo.id = rsoor.raschsv_sv_ops_oms_id) " +
                            "inner join raschsv_ops_oms_rasch_sum roors on (roors.raschsv_ops_oms_rasch_sum_id = rsoor.id) " +
                            "inner join raschsv_sv_sum_1tip rsst on (rsst.id = roors.raschsv_sv_sum1_tip_id) " +
                        "where  " +
                            "ops.declaration_data_id = ? " +
                    ") ",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER}
            );

            getJdbcTemplate().update(
                    "DELETE FROM raschsv_sv_sum_1tip WHERE id in ( " +
                        "select  " +
                            "sst.id " +
                        "from  " +
                            "raschsv_obyaz_plat_sv ops " +
                            "inner join raschsv_sv_prim_tarif9_1_427 spt on (spt.raschsv_obyaz_plat_sv_id = ops.id) " +
                            "inner join raschsv_vyplat_it_427 vi on (vi.raschsv_sv_prim_tarif9_427_id = spt.id) " +
                            "inner join raschsv_sv_sum_1tip sst on (sst.id = vi.raschsv_sv_sum1_tip_id) " +
                        "where " +
                            "ops.declaration_data_id = ? " +
                    ") ",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER}
            );

            getJdbcTemplate().update(
                    "DELETE FROM raschsv_sv_sum_1tip WHERE id in (" +
                        "select " +
                            "sst.id " +
                        "from " +
                            "raschsv_obyaz_plat_sv ops " +
                            "inner join raschsv_sv_prim_tarif9_1_427 spt on (spt.raschsv_obyaz_plat_sv_id = ops.id) " +
                            "inner join raschsv_sved_patent sp on (sp.raschsv_sv_prim_tarif9_427_id = spt.id) " +
                            "inner join raschsv_sv_sum_1tip sst on (sst.id = sp.raschsv_sv_sum1_tip_id) " +
                        "where " +
                            "ops.declaration_data_id = ? " +
                    ") ",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER}
            );
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи с raschsv_sv_sum_1tip = %d", declarationDataId), e);
        }
    }
}
