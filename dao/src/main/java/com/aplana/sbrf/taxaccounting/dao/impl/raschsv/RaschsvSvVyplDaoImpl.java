package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvVyplDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional
public class RaschsvSvVyplDaoImpl extends AbstractDao implements RaschsvSvVyplDao {

    private static final Log LOG = LogFactory.getLog(RaschsvSvVyplDaoImpl.class);

    public Integer insert(final List<RaschsvSvVypl> raschsvSvVyplList) {
        String sql = "INSERT INTO " + RaschsvSvVypl.TABLE_NAME +
                " (" + RaschsvSvVypl.COL_ID + ", " + RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID + ", " +
                RaschsvSvVypl.COL_SUM_VYPL_VS3 + ", " + RaschsvSvVypl.COL_VYPL_OPS_VS3 + ", " +
                RaschsvSvVypl.COL_VYPL_OPS_DOG_VS3 + ", " + RaschsvSvVypl.COL_NACHISL_SV_VS3 + ") VALUES (?, ?, ?, ?, ?, ?)";

        // Генерация идентификаторов
        for (RaschsvSvVypl raschsvSvVypl : raschsvSvVyplList) {
            raschsvSvVypl.setId(generateId(RaschsvSvVypl.SEQ, Long.class));
            LOG.debug(raschsvSvVypl.getId());
        }

        int [] res = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RaschsvSvVypl raschsvSvVypl = raschsvSvVyplList.get(i);
                ps.setLong(1, raschsvSvVypl.getId());
                ps.setLong(2, raschsvSvVypl.getRaschsvPersSvStrahLicId());
                ps.setDouble(3, raschsvSvVypl.getSumVyplVs3());
                ps.setDouble(4, raschsvSvVypl.getVyplOpsVs3());
                ps.setDouble(5, raschsvSvVypl.getVyplOpsDogVs3());
                ps.setDouble(6, raschsvSvVypl.getNachislSvVs3());
            }

            @Override
            public int getBatchSize() {
                return raschsvSvVyplList.size();
            }
        });
        return res.length;
    }

    private static final class RaschsvSvVyplRowMapper implements RowMapper<RaschsvSvVypl> {
        @Override
        public RaschsvSvVypl mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvVypl raschsvSvVypl = new RaschsvSvVypl();
            raschsvSvVypl.setId(SqlUtils.getLong(rs, RaschsvSvVypl.COL_ID));
            raschsvSvVypl.setRaschsvPersSvStrahLicId(SqlUtils.getLong(rs, RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID));
            raschsvSvVypl.setSumVyplVs3(rs.getDouble(RaschsvSvVypl.COL_SUM_VYPL_VS3));
            raschsvSvVypl.setVyplOpsVs3(rs.getDouble(RaschsvSvVypl.COL_VYPL_OPS_VS3));
            raschsvSvVypl.setVyplOpsDogVs3(rs.getDouble(RaschsvSvVypl.COL_VYPL_OPS_DOG_VS3));
            raschsvSvVypl.setNachislSvVs3(rs.getDouble(RaschsvSvVypl.COL_NACHISL_SV_VS3));

            return raschsvSvVypl;
        }
    }
}
