package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvnpPodpisantDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class RaschsvSvnpPodpisantDaoImpl extends AbstractDao implements RaschsvSvnpPodpisantDao {

    // Перечень столбцов таблицы "СвНП и Подписант"
    private static final StringBuilder SVNP_PODPISANT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvnpPodpisant.COLUMNS, null));
    private static final StringBuilder SVNP_PODPISANT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvnpPodpisant.COLUMNS, ":"));

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvSvnpPodpisant.TABLE_NAME +
            " (" + SVNP_PODPISANT_COLS + ") VALUES (" + SVNP_PODPISANT_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + SVNP_PODPISANT_COLS + " FROM " + RaschsvSvnpPodpisant.TABLE_NAME +
            " WHERE " + RaschsvSvnpPodpisant.COL_DECLARATION_DATA_ID + " = :" + RaschsvSvnpPodpisant.COL_DECLARATION_DATA_ID;

    public Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant) {
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
                .addValue(RaschsvSvnpPodpisant.COL_OTCHESTVO, raschsvSvnpPodpisant.getOtchestvo())
                .addValue(RaschsvSvnpPodpisant.COL_PODPISANT_PR_PODP, raschsvSvnpPodpisant.getPodpisantPrPodp())
                .addValue(RaschsvSvnpPodpisant.COL_PODPISANT_NAIM_DOC, raschsvSvnpPodpisant.getPodpisantNaimDoc())
                .addValue(RaschsvSvnpPodpisant.COL_PODPISANT_NAIM_ORG, raschsvSvnpPodpisant.getPodpisantNaimOrg());
        getNamedParameterJdbcTemplate().update(SQL_INSERT.toString(), params);

        return raschsvSvnpPodpisant.getId();
    }

    @Override
    public RaschsvSvnpPodpisant findRaschsvSvnpPodpisant(Long declarationDataId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvSvnpPodpisant.COL_DECLARATION_DATA_ID, declarationDataId);
            return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvSvnpPodpisantRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Маппинг для "СвНП и Подписант"
     */
    private static final class RaschsvSvnpPodpisantRowMapper implements RowMapper<RaschsvSvnpPodpisant> {
        @Override
        public RaschsvSvnpPodpisant mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvnpPodpisant raschsvSvnpPodpisant = new RaschsvSvnpPodpisant();
            raschsvSvnpPodpisant.setId(SqlUtils.getLong(rs, RaschsvSvnpPodpisant.COL_ID));
            raschsvSvnpPodpisant.setDeclarationDataId(SqlUtils.getLong(rs, RaschsvSvnpPodpisant.COL_DECLARATION_DATA_ID));
            raschsvSvnpPodpisant.setSvnpOkved(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_OKVED));
            raschsvSvnpPodpisant.setSvnpTlph(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_TLPH));
            raschsvSvnpPodpisant.setSvnpNaimOrg(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_NAIM_ORG));
            raschsvSvnpPodpisant.setSvnpInnyl(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_INNYL));
            raschsvSvnpPodpisant.setSvnpKpp(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_KPP));
            raschsvSvnpPodpisant.setSvnpSvReorgForm(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_SV_REORG_FORM));
            raschsvSvnpPodpisant.setSvnpSvReorgInnyl(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_SV_REORG_INNYL));
            raschsvSvnpPodpisant.setSvnpSvReorgKpp(rs.getNString(RaschsvSvnpPodpisant.COL_SVNP_SV_REORG_KPP));
            raschsvSvnpPodpisant.setFamilia(rs.getNString(RaschsvSvnpPodpisant.COL_FAMILIA));
            raschsvSvnpPodpisant.setImya(rs.getNString(RaschsvSvnpPodpisant.COL_IMYA));
            raschsvSvnpPodpisant.setOtchestvo(rs.getNString(RaschsvSvnpPodpisant.COL_OTCHESTVO));
            raschsvSvnpPodpisant.setPodpisantPrPodp(rs.getNString(RaschsvSvnpPodpisant.COL_PODPISANT_PR_PODP));
            raschsvSvnpPodpisant.setPodpisantNaimDoc(rs.getNString(RaschsvSvnpPodpisant.COL_PODPISANT_NAIM_DOC));
            raschsvSvnpPodpisant.setPodpisantNaimOrg(rs.getNString(RaschsvSvnpPodpisant.COL_PODPISANT_NAIM_ORG));
            return raschsvSvnpPodpisant;
        }
    }
}
