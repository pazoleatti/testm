package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.*;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RaschsvObyazPlatSvDaoImpl extends AbstractDao implements RaschsvObyazPlatSvDao {

    @Autowired
    private RaschsvUplPerDao raschsvUplPerDao;

    @Autowired
    private RaschsvUplPrevOssDao raschsvUplPrevOssDao;

    @Autowired
    private RaschsvSvOpsOmsDao raschsvSvOpsOmsDao;

    @Autowired
    private RaschsvOssVnmDao raschsvOssVnmDao;

    @Autowired
    private RaschsvRashOssZakDao raschsvRashOssZakDao;

    @Autowired
    private RaschsvVyplFinFbDao raschsvVyplFinFbDao;

    // Перечень столбцов таблицы ОбязПлатСВ
    private static final String OBYAZ_PLAT_SV_COLS = SqlUtils.getColumnsToString(RaschsvObyazPlatSv.COLUMNS, null);
    private static final String OBYAZ_PLAT_SV_FIELDS = SqlUtils.getColumnsToString(RaschsvObyazPlatSv.COLUMNS, ":");

    private static final String SQL_INSERT = "INSERT INTO " + RaschsvObyazPlatSv.TABLE_NAME +
            " (" + OBYAZ_PLAT_SV_COLS + ") VALUES (" + OBYAZ_PLAT_SV_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + OBYAZ_PLAT_SV_COLS + " FROM " + RaschsvObyazPlatSv.TABLE_NAME +
            " WHERE " + RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID + " = :" + RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID;

    public Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv) {
        raschsvObyazPlatSv.setId(generateId(RaschsvObyazPlatSv.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvObyazPlatSv.COL_ID, raschsvObyazPlatSv.getId())
                .addValue(RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID, raschsvObyazPlatSv.getDeclarationDataId())
                .addValue(RaschsvObyazPlatSv.COL_OKTMO, raschsvObyazPlatSv.getOktmo());
        getNamedParameterJdbcTemplate().update(SQL_INSERT.toString(), params);

        return raschsvObyazPlatSv.getId();
    }

    public RaschsvObyazPlatSv findObyazPlatSv(Long declarationDataId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID, declarationDataId);
            RaschsvObyazPlatSv raschsvObyazPlatSv =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvObyazPlatSvRowMapper());

            raschsvObyazPlatSv.setRaschsvUplPerList(raschsvUplPerDao.findUplPer(raschsvObyazPlatSv.getId()));
            raschsvObyazPlatSv.setRaschsvUplPrevOss(raschsvUplPrevOssDao.findUplPrevOss(raschsvObyazPlatSv.getId()));
            raschsvObyazPlatSv.setRaschsvSvOpsOmsList(raschsvSvOpsOmsDao.findSvOpsOms(raschsvObyazPlatSv.getId()));
            raschsvObyazPlatSv.setRaschsvOssVnm(raschsvOssVnmDao.findOssVnm(raschsvObyazPlatSv.getId()));
            raschsvObyazPlatSv.setRaschsvRashOssZak(raschsvRashOssZakDao.findRaschsvRashOssZak(raschsvObyazPlatSv.getId()));
            raschsvObyazPlatSv.setRaschsvVyplFinFb(raschsvVyplFinFbDao.findRaschsvVyplFinFb(raschsvObyazPlatSv.getId()));

            return raschsvObyazPlatSv;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Маппинг для ОбязПлатСВ
     */
    private static final class RaschsvObyazPlatSvRowMapper implements RowMapper<RaschsvObyazPlatSv> {
        @Override
        public RaschsvObyazPlatSv mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvObyazPlatSv raschsvObyazPlatSv = new RaschsvObyazPlatSv();
            raschsvObyazPlatSv.setId(SqlUtils.getLong(rs, RaschsvObyazPlatSv.COL_ID));
            raschsvObyazPlatSv.setDeclarationDataId(SqlUtils.getLong(rs, RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID));
            raschsvObyazPlatSv.setOktmo(rs.getString(RaschsvObyazPlatSv.COL_OKTMO));

            return raschsvObyazPlatSv;
        }
    }
}
