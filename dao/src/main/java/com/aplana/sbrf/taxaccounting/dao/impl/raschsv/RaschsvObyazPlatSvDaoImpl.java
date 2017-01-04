package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvObyazPlatSvDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional
public class RaschsvObyazPlatSvDaoImpl extends AbstractDao implements RaschsvObyazPlatSvDao {

    // Перечень столбцов таблицы "Сводные данные об обязательствах плательщика страховых взносов"
    private static final StringBuilder OBYAZ_PLAT_SV_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvObyazPlatSv.COLUMNS, null));
    private static final StringBuilder OBYAZ_PLAT_SV_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvObyazPlatSv.COLUMNS, ":"));

    /**
     * Сохранение "Сводные данные об обязательствах плательщика страховых взносов"
     * @return
     */
    public Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv) {
        String sql = "INSERT INTO " + RaschsvObyazPlatSv.TABLE_NAME +
                " (" + OBYAZ_PLAT_SV_COLS + ") VALUES (" + OBYAZ_PLAT_SV_FIELDS + ")";

        raschsvObyazPlatSv.setId(generateId(RaschsvObyazPlatSv.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvObyazPlatSv.COL_ID, raschsvObyazPlatSv.getId())
                .addValue(RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID, raschsvObyazPlatSv.getDeclarationDataId())
                .addValue(RaschsvObyazPlatSv.COL_OKTMO, raschsvObyazPlatSv.getOktmo());
        getNamedParameterJdbcTemplate().update(sql.toString(), params);

        return raschsvObyazPlatSv.getId();
    }

    /**
     * Маппинг для "Сводные данные об обязательствах плательщика страховых взносов"
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
