package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvnpPodpisantDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaschsvSvnpPodpisantDaoImpl extends AbstractDao implements RaschsvSvnpPodpisantDao {

    // Перечень столбцов таблицы "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
    private static final StringBuilder SVNP_PODPISANT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvnpPodpisant.COLUMNS, null));
    private static final StringBuilder SVNP_PODPISANT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvnpPodpisant.COLUMNS, ":"));

    public Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant) {
        String sql = "INSERT INTO " + RaschsvSvnpPodpisant.TABLE_NAME +
                " (" + SVNP_PODPISANT_COLS + ") VALUES (" + SVNP_PODPISANT_FIELDS + ")";

        raschsvSvnpPodpisant.setId(generateId(RaschsvSvnpPodpisant.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvnpPodpisant.COL_ID, raschsvSvnpPodpisant.getId())
                .addValue(RaschsvSvnpPodpisant.COL_DECLARATION_DATA_ID, raschsvSvnpPodpisant.getDeclarationDataId())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_OKVED, raschsvSvnpPodpisant.getSvnpOkved())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_TLPH, raschsvSvnpPodpisant.getSvnpTlph())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_NAIM_ORG, raschsvSvnpPodpisant.getSvnpNaimOrg())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_INNYL, raschsvSvnpPodpisant.getSvnpInnyl())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_KPP, raschsvSvnpPodpisant.getSvnpKpp())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_SV_REORG_FORM, raschsvSvnpPodpisant.getSvnpSvReorgForm())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_SV_REORG_INNYL, raschsvSvnpPodpisant.getSvnpSvReorgInnyl())
                .addValue(RaschsvSvnpPodpisant.COL_SVNP_SV_REORG_KPP, raschsvSvnpPodpisant.getSvnpSvReorgKpp())
                .addValue(RaschsvSvnpPodpisant.COL_FAMILIA, raschsvSvnpPodpisant.getFamilia())
                .addValue(RaschsvSvnpPodpisant.COL_IMYA, raschsvSvnpPodpisant.getImya())
                .addValue(RaschsvSvnpPodpisant.COL_MIDDLE_NAME, raschsvSvnpPodpisant.getMiddleName())
                .addValue(RaschsvSvnpPodpisant.COL_PODPISANT_PR_PODP, raschsvSvnpPodpisant.getPodpisantPrPodp())
                .addValue(RaschsvSvnpPodpisant.COL_PODPISANT_NAIM_DOC, raschsvSvnpPodpisant.getPodpisantNaimDoc())
                .addValue(RaschsvSvnpPodpisant.COL_PODPISANT_NAIM_ORG, raschsvSvnpPodpisant.getPodpisantNaimOrg());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvSvnpPodpisant.getId();
    }
}
