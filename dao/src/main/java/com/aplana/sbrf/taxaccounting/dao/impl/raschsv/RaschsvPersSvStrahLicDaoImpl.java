package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class RaschsvPersSvStrahLicDaoImpl extends AbstractDao implements RaschsvPersSvStrahLicDao {

    @Override
    public List<RaschsvPersSvStrahLic> findAll() {
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = getJdbcTemplate().query(
                "select * from " + RaschsvPersSvStrahLic.TABLE_NAME,
                new RaschsvPersSvStrahLicRowMapper());

        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {

            // Выгрузка "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
            raschsvPersSvStrahLic.setRaschsvSvVyplList(getRaschsvSvVypl(raschsvPersSvStrahLic.getId()));
        }
        return raschsvPersSvStrahLicList;
    }

    /**
     * Выгрузка из "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
     * по идентификатору "Персонифицированные сведения о застрахованных лицах"
     * @param raschsvPersSvStrahLicId
     * @return
     */
    private List<RaschsvSvVypl> getRaschsvSvVypl(long raschsvPersSvStrahLicId){
        return getJdbcTemplate().query(
                "select * from " + RaschsvSvVypl.TABLE_NAME + " svSl where svSl." + RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID + " = ?",
                new Object[]{raschsvPersSvStrahLicId},
                new RaschsvSvVyplRowMapper());
    }

    @Override
    public Integer insert(final List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList) {
        String sql = "INSERT INTO " + RaschsvPersSvStrahLic.TABLE_NAME +
                " (" + RaschsvPersSvStrahLic.COL_ID + ", " + RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID + ", " + RaschsvPersSvStrahLic.COL_NOM_KORR + ", " +
                RaschsvPersSvStrahLic.COL_PERIOD + ", " + RaschsvPersSvStrahLic.COL_OTCHET_GOD + ", " +
                RaschsvPersSvStrahLic.COL_NOMER + ", " + RaschsvPersSvStrahLic.COL_SV_DATA + ", " +
                RaschsvPersSvStrahLic.COL_INNFL + ", " + RaschsvPersSvStrahLic.COL_SNILS + ", " +
                RaschsvPersSvStrahLic.COL_DATA_ROZD + ", " + RaschsvPersSvStrahLic.COL_GRAZD + ", " +
                RaschsvPersSvStrahLic.COL_POL + ", " + RaschsvPersSvStrahLic.COL_KOD_VID_DOC + ", " +
                RaschsvPersSvStrahLic.COL_SER_NOM_DOC + ", " + RaschsvPersSvStrahLic.COL_PRIZ_OPS + ", " +
                RaschsvPersSvStrahLic.COL_PRIZ_OMS + ", " + RaschsvPersSvStrahLic.COL_PRIZ_OSS + ", " +
                RaschsvPersSvStrahLic.COL_FAMILIA + ", " + RaschsvPersSvStrahLic.COL_IMYA + ", " +
                RaschsvPersSvStrahLic.COL_OTCHESTVO + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Генерация идентификаторов
        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            raschsvPersSvStrahLic.setId(generateId(RaschsvPersSvStrahLic.SEQ, Long.class));
        }

        int [] res = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RaschsvPersSvStrahLic raschsvPersSvStrahLic = raschsvPersSvStrahLicList.get(i);
                ps.setLong(1, raschsvPersSvStrahLic.getId());
                ps.setLong(2, raschsvPersSvStrahLic.getDeclarationDataId());
                ps.setInt(3, raschsvPersSvStrahLic.getNomKorr());
                ps.setString(4, raschsvPersSvStrahLic.getPeriod());
                ps.setString(5, raschsvPersSvStrahLic.getOtchetGod());
                ps.setInt(6, raschsvPersSvStrahLic.getNomer());
                ps.setDate(7, raschsvPersSvStrahLic.getSvData());
                ps.setString(8, raschsvPersSvStrahLic.getInnfl());
                ps.setString(9, raschsvPersSvStrahLic.getSnils());
                ps.setDate(10, raschsvPersSvStrahLic.getDataRozd());
                ps.setString(11, raschsvPersSvStrahLic.getGrazd());
                ps.setString(12, raschsvPersSvStrahLic.getPol());
                ps.setString(13, raschsvPersSvStrahLic.getKodVidDoc());
                ps.setString(14, raschsvPersSvStrahLic.getSerNomDoc());
                ps.setString(15, raschsvPersSvStrahLic.getPrizOps());
                ps.setString(16, raschsvPersSvStrahLic.getPrizOms());
                ps.setString(17, raschsvPersSvStrahLic.getPrizOss());
                ps.setString(18, raschsvPersSvStrahLic.getFamilia());
                ps.setString(19, raschsvPersSvStrahLic.getImya());
                ps.setString(20, raschsvPersSvStrahLic.getOtchestvo());
            }

            @Override
            public int getBatchSize() {
                return raschsvPersSvStrahLicList.size();
            }
        });

        // Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица
        List<RaschsvSvVypl> raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            for (RaschsvSvVypl raschsvSvVypl : raschsvPersSvStrahLic.getRaschsvSvVyplList()) {
                raschsvSvVypl.setRaschsvPersSvStrahLicId(raschsvPersSvStrahLic.getId());
                raschsvSvVyplList.add(raschsvSvVypl);
            }
        }

        // Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
        if (!raschsvSvVyplList.isEmpty()) {
            insertRaschsvSvVypl(raschsvSvVyplList);
        }

        return res.length;
    }

    /**
     * Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
     * @param raschsvSvVyplList - перечень сведений о сумме выплат
     * @return
     */
    private Integer insertRaschsvSvVypl(final List<RaschsvSvVypl> raschsvSvVyplList) {
        String sql = "INSERT INTO " + RaschsvSvVypl.TABLE_NAME +
                " (" + RaschsvSvVypl.COL_ID + ", " + RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID + ", " +
                RaschsvSvVypl.COL_SUM_VYPL_VS3 + ", " + RaschsvSvVypl.COL_VYPL_OPS_VS3 + ", " +
                RaschsvSvVypl.COL_VYPL_OPS_DOG_VS3 + ", " + RaschsvSvVypl.COL_NACHISL_SV_VS3 + ") VALUES (?, ?, ?, ?, ?, ?)";

        // Генерация идентификаторов
        for (RaschsvSvVypl raschsvSvVypl : raschsvSvVyplList) {
            raschsvSvVypl.setId(generateId(RaschsvSvVypl.SEQ, Long.class));
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

    /**
     * Маппинг для "Персонифицированные сведения о застрахованных лицах"
     */
    private static final class RaschsvPersSvStrahLicRowMapper implements RowMapper<RaschsvPersSvStrahLic> {
        @Override
        public RaschsvPersSvStrahLic mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvPersSvStrahLic raschsvPersSvStrahLic = new RaschsvPersSvStrahLic();
            raschsvPersSvStrahLic.setId(SqlUtils.getLong(rs, RaschsvPersSvStrahLic.COL_ID));
            raschsvPersSvStrahLic.setDeclarationDataId(SqlUtils.getLong(rs, RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID));
            raschsvPersSvStrahLic.setNomKorr(SqlUtils.getInteger(rs, RaschsvPersSvStrahLic.COL_NOM_KORR));
            raschsvPersSvStrahLic.setPeriod(rs.getString(RaschsvPersSvStrahLic.COL_PERIOD));
            raschsvPersSvStrahLic.setOtchetGod(rs.getString(RaschsvPersSvStrahLic.COL_OTCHET_GOD));
            raschsvPersSvStrahLic.setNomer(SqlUtils.getInteger(rs, RaschsvPersSvStrahLic.COL_NOMER));
            raschsvPersSvStrahLic.setSvData(rs.getDate(RaschsvPersSvStrahLic.COL_SV_DATA));
            raschsvPersSvStrahLic.setInnfl(rs.getString(RaschsvPersSvStrahLic.COL_INNFL));
            raschsvPersSvStrahLic.setSnils(rs.getString(RaschsvPersSvStrahLic.COL_SNILS));
            raschsvPersSvStrahLic.setDataRozd(rs.getDate(RaschsvPersSvStrahLic.COL_DATA_ROZD));
            raschsvPersSvStrahLic.setGrazd(rs.getString(RaschsvPersSvStrahLic.COL_GRAZD));
            raschsvPersSvStrahLic.setPol(rs.getString(RaschsvPersSvStrahLic.COL_POL));
            raschsvPersSvStrahLic.setKodVidDoc(rs.getString(RaschsvPersSvStrahLic.COL_KOD_VID_DOC));
            raschsvPersSvStrahLic.setSerNomDoc(rs.getString(RaschsvPersSvStrahLic.COL_SER_NOM_DOC));
            raschsvPersSvStrahLic.setPrizOps(rs.getString(RaschsvPersSvStrahLic.COL_PRIZ_OPS));
            raschsvPersSvStrahLic.setPrizOms(rs.getString(RaschsvPersSvStrahLic.COL_PRIZ_OMS));
            raschsvPersSvStrahLic.setPrizOss(rs.getString(RaschsvPersSvStrahLic.COL_PRIZ_OSS));
            raschsvPersSvStrahLic.setFamilia(rs.getString(RaschsvPersSvStrahLic.COL_FAMILIA));
            raschsvPersSvStrahLic.setImya(rs.getString(RaschsvPersSvStrahLic.COL_IMYA));
            raschsvPersSvStrahLic.setOtchestvo(rs.getString(RaschsvPersSvStrahLic.COL_OTCHESTVO));

            return raschsvPersSvStrahLic;
        }
    }

    /**
     * Маппинг для "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
     */
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
