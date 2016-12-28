package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvFileDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvFile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class RaschsvFileDaoImpl extends AbstractDao implements RaschsvFileDao {

    @Override
    public RaschsvFile get(long raschsvFileId) {
        RaschsvFile raschsvFile = getJdbcTemplate().queryForObject(
                "select * from " + RaschsvFile.TABLE_NAME + " f where f." + RaschsvFile.COL_ID + " = ?",
                new Object[]{raschsvFileId},
                new RaschsvFileDaoImpl.RaschsvFileRowMapper());

        return raschsvFile;
    }

    @Override
    public Integer insert(RaschsvFile raschsvFile) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        String sql = "INSERT INTO " + RaschsvFile.TABLE_NAME +
                " (" + RaschsvFile.COL_ID + ", " + RaschsvFile.COL_ID_FILE + ") VALUES (?, ?)";
        return jdbcTemplate.update(sql, generateId(RaschsvFile.SEQ, Long.class), raschsvFile.getIdFile());
    }

    private static final class RaschsvFileRowMapper implements RowMapper<RaschsvFile> {
        @Override
        public RaschsvFile mapRow(ResultSet rs, int index) throws SQLException {

            RaschsvFile raschsvFile = new RaschsvFile();
            raschsvFile.setId(SqlUtils.getLong(rs, RaschsvFile.COL_ID));
            raschsvFile.setIdFile(rs.getString(RaschsvFile.COL_ID_FILE));

            return raschsvFile;
        }
    }
}
