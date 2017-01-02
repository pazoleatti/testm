package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvPersSvStrahLicDaoImpl extends AbstractDao implements RaschsvPersSvStrahLicDao {

    // Перечень столбцов таблицы "Персонифицированные сведения о застрахованных лицах"
    private static final StringBuilder PERS_SV_STRAH_LIC_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, null));
    private static final StringBuilder PERS_SV_STRAH_LIC_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
    private static final StringBuilder SV_VYPL_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvVypl.COLUMNS, null));
    private static final StringBuilder SV_VYPL_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvVypl.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица,
    // по месяцу и коду категории застрахованного лица"
    private static final StringBuilder SV_VYPL_MK_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvVyplMt.COLUMNS, null));
    private static final StringBuilder SV_VYPL_MK_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvVyplMt.COLUMNS, ":"));

    // Перечень столбцов таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица,
    // на которые исчислены страховые взносы по дополнительному тарифу"
    private static final StringBuilder VYPL_SV_DOP_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplSvDop.COLUMNS, null));
    private static final StringBuilder VYPL_SV_DOP_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplSvDop.COLUMNS, ":"));

    // Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица,
    // на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа"
    private static final StringBuilder VYPL_SV_DOP_MT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplSvDopMt.COLUMNS, null));
    private static final StringBuilder VYPL_SV_DOP_MT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplSvDopMt.COLUMNS, ":"));

    /**
     * Выгрузка всех сведений о застрахованных лицах
     * @return
     */
    @Override
    public List<RaschsvPersSvStrahLic> findAll() {
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = getJdbcTemplate().query(
                "select " + PERS_SV_STRAH_LIC_COLS + " from " + RaschsvPersSvStrahLic.TABLE_NAME,
                new RaschsvPersSvStrahLicRowMapper());

        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {

            // Выгрузка "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
            raschsvPersSvStrahLic.setRaschsvSvVyplList(getRaschsvSvVypl(raschsvPersSvStrahLic.getId()));
        }
        return raschsvPersSvStrahLicList;
    }

    /**
     * Выгрузка из "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
     * @param raschsvPersSvStrahLicId - идентификатор "Персонифицированные сведения о застрахованных лицах"
     * @return
     */
    private List<RaschsvSvVypl> getRaschsvSvVypl(long raschsvPersSvStrahLicId){
        return getJdbcTemplate().query(
                "select " + SV_VYPL_COLS + " from " + RaschsvSvVypl.TABLE_NAME + " sV where sV." + RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID + " = ?",
                new Object[]{raschsvPersSvStrahLicId},
                new RaschsvSvVyplRowMapper());
    }

    /**
     * Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица
     * @param raschsvSvVyplId - идентификатор "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
     * @return
     */
    private List<RaschsvSvVyplMt> getRaschsvSvVyplMt(long raschsvSvVyplId){
        return getJdbcTemplate().query(
                "select " + SV_VYPL_MK_COLS + " from " + RaschsvSvVyplMt.TABLE_NAME + " sVMt where sVMt." + RaschsvSvVyplMt.COL_RASCHSV_SV_VYPL_ID + " = ?",
                new Object[]{raschsvSvVyplId},
                new RaschsvSvVyplMtRowMapper());
    }

    /**
     * Сохранение "Персонифицированные сведения о застрахованных лицах"
     * @param raschsvPersSvStrahLicList - перечень сведений о застрахованных лицах
     * @return
     */
    @Override
    public Integer insert(final List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList) {
        String sql = "INSERT INTO " + RaschsvPersSvStrahLic.TABLE_NAME +
                " (" + PERS_SV_STRAH_LIC_COLS + ") VALUES (" + PERS_SV_STRAH_LIC_FIELDS + ")";

        // Генерация идентификаторов
        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            raschsvPersSvStrahLic.setId(generateId(RaschsvPersSvStrahLic.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvPersSvStrahLicList.size());
        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvPersSvStrahLic.COL_ID, raschsvPersSvStrahLic.getId())
                            .addValue(RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID, raschsvPersSvStrahLic.getDeclarationDataId())
                            .addValue(RaschsvPersSvStrahLic.COL_NOM_KORR, raschsvPersSvStrahLic.getNomKorr())
                            .addValue(RaschsvPersSvStrahLic.COL_PERIOD, raschsvPersSvStrahLic.getPeriod())
                            .addValue(RaschsvPersSvStrahLic.COL_OTCHET_GOD, raschsvPersSvStrahLic.getOtchetGod())
                            .addValue(RaschsvPersSvStrahLic.COL_NOMER, raschsvPersSvStrahLic.getNomer())
                            .addValue(RaschsvPersSvStrahLic.COL_SV_DATA, raschsvPersSvStrahLic.getSvData())
                            .addValue(RaschsvPersSvStrahLic.COL_INNFL, raschsvPersSvStrahLic.getInnfl())
                            .addValue(RaschsvPersSvStrahLic.COL_SNILS, raschsvPersSvStrahLic.getSnils())
                            .addValue(RaschsvPersSvStrahLic.COL_DATA_ROZD, raschsvPersSvStrahLic.getDataRozd())
                            .addValue(RaschsvPersSvStrahLic.COL_GRAZD, raschsvPersSvStrahLic.getGrazd())
                            .addValue(RaschsvPersSvStrahLic.COL_POL, raschsvPersSvStrahLic.getPol())
                            .addValue(RaschsvPersSvStrahLic.COL_KOD_VID_DOC, raschsvPersSvStrahLic.getKodVidDoc())
                            .addValue(RaschsvPersSvStrahLic.COL_SER_NOM_DOC, raschsvPersSvStrahLic.getSerNomDoc())
                            .addValue(RaschsvPersSvStrahLic.COL_PRIZ_OPS, raschsvPersSvStrahLic.getPrizOps())
                            .addValue(RaschsvPersSvStrahLic.COL_PRIZ_OMS, raschsvPersSvStrahLic.getPrizOms())
                            .addValue(RaschsvPersSvStrahLic.COL_PRIZ_OSS, raschsvPersSvStrahLic.getPrizOss())
                            .addValue(RaschsvPersSvStrahLic.COL_FAMILIA, raschsvPersSvStrahLic.getFamilia())
                            .addValue(RaschsvPersSvStrahLic.COL_IMYA, raschsvPersSvStrahLic.getImya())
                            .addValue(RaschsvPersSvStrahLic.COL_MIDDLE_NAME, raschsvPersSvStrahLic.getMiddleName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvPersSvStrahLicList.size()]));

        List<RaschsvSvVypl> raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
        List<RaschsvVyplSvDop> raschsvVyplSvDopList = new ArrayList<RaschsvVyplSvDop>();

        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {

            // Установка внешнего ключа для "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
            for (RaschsvSvVypl raschsvSvVypl : raschsvPersSvStrahLic.getRaschsvSvVyplList()) {
                raschsvSvVypl.setRaschsvPersSvStrahLicId(raschsvPersSvStrahLic.getId());
                raschsvSvVyplList.add(raschsvSvVypl);
            }

            // Установка внешнего ключа для "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица,
            // на которые исчислены страховые взносы по дополнительному тарифу"
            for (RaschsvVyplSvDop raschsvVyplSvDop : raschsvPersSvStrahLic.getRaschsvVyplSvDopList()) {
                raschsvVyplSvDop.setRaschsvPersSvStrahLicId(raschsvPersSvStrahLic.getId());
                raschsvVyplSvDopList.add(raschsvVyplSvDop);
            }
        }

        // Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
        if (!raschsvSvVyplList.isEmpty()) {
            insertRaschsvSvVypl(raschsvSvVyplList);
        }

        // Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица,
        // на которые исчислены страховые взносы по дополнительному тарифу"
        if (!raschsvVyplSvDopList.isEmpty()) {
            insertRaschsvVyplSvDop(raschsvVyplSvDopList);
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
                " (" + SV_VYPL_COLS + ") VALUES (" + SV_VYPL_FIELDS + ")";

        // Генерация идентификаторов
        for (RaschsvSvVypl raschsvSvVypl : raschsvSvVyplList) {
            raschsvSvVypl.setId(generateId(RaschsvSvVypl.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvVyplList.size());
        for (RaschsvSvVypl raschsvSvVypl : raschsvSvVyplList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvVypl.COL_ID, raschsvSvVypl.getId())
                            .addValue(RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, raschsvSvVypl.getRaschsvPersSvStrahLicId())
                            .addValue(RaschsvSvVypl.COL_SUM_VYPL_VS3, raschsvSvVypl.getSumVyplVs3())
                            .addValue(RaschsvSvVypl.COL_VYPL_OPS_VS3, raschsvSvVypl.getVyplOpsVs3())
                            .addValue(RaschsvSvVypl.COL_VYPL_OPS_DOG_VS3, raschsvSvVypl.getVyplOpsDogVs3())
                            .addValue(RaschsvSvVypl.COL_NACHISL_SV_VS3, raschsvSvVypl.getNachislSvVs3())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvVyplList.size()]));

        // Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица"
        List<RaschsvSvVyplMt> raschsvSvVyplMtList = new ArrayList<RaschsvSvVyplMt>();
        for (RaschsvSvVypl raschsvSvVypl : raschsvSvVyplList) {
            for (RaschsvSvVyplMt raschsvSvVyplMt : raschsvSvVypl.getRaschsvSvVyplMtList()) {
                raschsvSvVyplMt.setRaschsvSvVyplId(raschsvSvVypl.getId());
                raschsvSvVyplMtList.add(raschsvSvVyplMt);
            }
        }
        if (!raschsvSvVyplMtList.isEmpty()) {
            insertRaschsvSvVyplMt(raschsvSvVyplMtList);
        }

        return res.length;
    }

    /**
     * Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица,
     * по месяцу и коду категории застрахованного лица"
     * @param raschsvSvVyplMtList - перечень сведений о сумме выплат по месяцу и коду категории застрахованного лица
     * @return
     */
    private Integer insertRaschsvSvVyplMt(final List<RaschsvSvVyplMt> raschsvSvVyplMtList) {
        String sql = "INSERT INTO " + RaschsvSvVyplMt.TABLE_NAME +
                " (" + SV_VYPL_MK_COLS + ") VALUES (" + SV_VYPL_MK_FIELDS + ")";

        // Генерация идентификаторов
        for (RaschsvSvVyplMt raschsvSvVyplMt : raschsvSvVyplMtList) {
            raschsvSvVyplMt.setId(generateId(RaschsvSvVyplMt.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvVyplMtList.size());
        for (RaschsvSvVyplMt raschsvSvVyplMt : raschsvSvVyplMtList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvVyplMt.COL_ID, raschsvSvVyplMt.getId())
                            .addValue(RaschsvSvVyplMt.COL_RASCHSV_SV_VYPL_ID, raschsvSvVyplMt.getRaschsvSvVyplId())
                            .addValue(RaschsvSvVyplMt.COL_MESYAC, raschsvSvVyplMt.getMesyac())
                            .addValue(RaschsvSvVyplMt.COL_KOD_KAT_LIC, raschsvSvVyplMt.getKodKatLic())
                            .addValue(RaschsvSvVyplMt.COL_SUM_VYPL, raschsvSvVyplMt.getSumVypl())
                            .addValue(RaschsvSvVyplMt.COL_VYPL_OPS, raschsvSvVyplMt.getVyplOps())
                            .addValue(RaschsvSvVyplMt.COL_VYPL_OPS_DOG, raschsvSvVyplMt.getVyplOpsDog())
                            .addValue(RaschsvSvVyplMt.COL_NACHISL_SV, raschsvSvVyplMt.getNachislSv())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvSvVyplMtList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица,
     * на которые исчислены страховые взносы по дополнительному тарифу"
     * @param raschsvVyplSvDopList - перечень сведений о сумме выплат по дополнительному тарифу
     * @return
     */
    private Integer insertRaschsvVyplSvDop(final List<RaschsvVyplSvDop> raschsvVyplSvDopList) {
        String sql = "INSERT INTO " + RaschsvVyplSvDop.TABLE_NAME +
                " (" + VYPL_SV_DOP_COLS + ") VALUES (" + VYPL_SV_DOP_FIELDS + ")";

        // Генерация идентификаторов
        for (RaschsvVyplSvDop raschsvVyplSvDop : raschsvVyplSvDopList) {
            raschsvVyplSvDop.setId(generateId(RaschsvVyplSvDop.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvVyplSvDopList.size());
        for (RaschsvVyplSvDop raschsvVyplSvDop : raschsvVyplSvDopList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvVyplSvDop.COL_ID, raschsvVyplSvDop.getId())
                            .addValue(RaschsvVyplSvDop.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, raschsvVyplSvDop.getRaschsvPersSvStrahLicId())
                            .addValue(RaschsvVyplSvDop.COL_VYPL_SV_VS3, raschsvVyplSvDop.getVyplSvVs3())
                            .addValue(RaschsvVyplSvDop.COL_NACHISL_SV_VS3, raschsvVyplSvDop.getNachislSvVs3())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvVyplSvDopList.size()]));

        // Сохранение "Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа"
        List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = new ArrayList<RaschsvVyplSvDopMt>();
        for (RaschsvVyplSvDop raschsvVyplSvDop : raschsvVyplSvDopList) {
            for (RaschsvVyplSvDopMt raschsvVyplSvDopMt : raschsvVyplSvDop.getRaschsvVyplSvDopMtList()) {
                raschsvVyplSvDopMt.setRaschsvVyplSvDopId(raschsvVyplSvDop.getId());
                raschsvVyplSvDopMtList.add(raschsvVyplSvDopMt);
            }
        }
        if (!raschsvVyplSvDopMtList.isEmpty()) {
            insertRaschsvVyplSvDopMt(raschsvVyplSvDopMtList);
        }

        return res.length;
    }

    /**
     * Сохранение "Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица,
     * на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа"
     * @param raschsvVyplSvDopMtList - перечень сведений о сумме выплат по дополнительному тарифу по месяцу и коду
     * @return
     */
    private Integer insertRaschsvVyplSvDopMt(final List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList) {
        String sql = "INSERT INTO " + RaschsvVyplSvDopMt.TABLE_NAME +
                " (" + VYPL_SV_DOP_MT_COLS + ") VALUES (" + VYPL_SV_DOP_MT_FIELDS + ")";

        // Генерация идентификаторов
        for (RaschsvVyplSvDopMt raschsvVyplSvDopMt : raschsvVyplSvDopMtList) {
            raschsvVyplSvDopMt.setId(generateId(RaschsvVyplSvDopMt.SEQ, Long.class));
        }

        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvVyplSvDopMtList.size());
        for (RaschsvVyplSvDopMt raschsvVyplSvDopMt : raschsvVyplSvDopMtList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvVyplSvDopMt.COL_ID, raschsvVyplSvDopMt.getId())
                            .addValue(RaschsvVyplSvDopMt.COL_RASCHSV_VYPL_SV_DOP_ID, raschsvVyplSvDopMt.getRaschsvVyplSvDopId())
                            .addValue(RaschsvVyplSvDopMt.COL_MESYAC, raschsvVyplSvDopMt.getMesyac())
                            .addValue(RaschsvVyplSvDopMt.COL_TARIF, raschsvVyplSvDopMt.getTarif())
                            .addValue(RaschsvVyplSvDopMt.COL_VYPL_SV, raschsvVyplSvDopMt.getVyplSv())
                            .addValue(RaschsvVyplSvDopMt.COL_NACHISL_SV, raschsvVyplSvDopMt.getNachislSv())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(sql,
                batchValues.toArray(new Map[raschsvVyplSvDopMtList.size()]));

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
            raschsvPersSvStrahLic.setMiddleName(rs.getString(RaschsvPersSvStrahLic.COL_MIDDLE_NAME));

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

    /**
     * Маппинг для "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица"
     */
    private static final class RaschsvSvVyplMtRowMapper implements RowMapper<RaschsvSvVyplMt> {
        @Override
        public RaschsvSvVyplMt mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvVyplMt raschsvSvVyplMt = new RaschsvSvVyplMt();
            raschsvSvVyplMt.setId(SqlUtils.getLong(rs, RaschsvSvVyplMt.COL_ID));
            raschsvSvVyplMt.setRaschsvSvVyplId(SqlUtils.getLong(rs, RaschsvSvVyplMt.COL_RASCHSV_SV_VYPL_ID));
            raschsvSvVyplMt.setMesyac(rs.getString(RaschsvSvVyplMt.COL_MESYAC));
            raschsvSvVyplMt.setKodKatLic(rs.getString(RaschsvSvVyplMt.COL_KOD_KAT_LIC));
            raschsvSvVyplMt.setSumVypl(rs.getDouble(RaschsvSvVyplMt.COL_SUM_VYPL));
            raschsvSvVyplMt.setVyplOps(rs.getDouble(RaschsvSvVyplMt.COL_VYPL_OPS));
            raschsvSvVyplMt.setVyplOpsDog(rs.getDouble(RaschsvSvVyplMt.COL_VYPL_OPS_DOG));
            raschsvSvVyplMt.setNachislSv(rs.getDouble(RaschsvSvVyplMt.COL_NACHISL_SV));

            return raschsvSvVyplMt;
        }
    }

    /**
     * Маппинг для "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
     */
    private static final class RaschsvVyplSvDopRowMapper implements RowMapper<RaschsvVyplSvDop> {
        @Override
        public RaschsvVyplSvDop mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplSvDop raschsvVyplSvDop = new RaschsvVyplSvDop();
            raschsvVyplSvDop.setId(SqlUtils.getLong(rs, RaschsvVyplSvDop.COL_ID));
            raschsvVyplSvDop.setRaschsvPersSvStrahLicId(SqlUtils.getLong(rs, RaschsvVyplSvDop.COL_RASCHSV_PERS_SV_STRAH_LIC_ID));
            raschsvVyplSvDop.setVyplSvVs3(rs.getDouble(RaschsvVyplSvDop.COL_VYPL_SV_VS3));
            raschsvVyplSvDop.setNachislSvVs3(rs.getDouble(RaschsvVyplSvDop.COL_NACHISL_SV_VS3));

            return raschsvVyplSvDop;
        }
    }

    /**
     * Маппинг для "Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа"
     */
    private static final class RaschsvVyplSvDopMtRowMapper implements RowMapper<RaschsvVyplSvDopMt> {
        @Override
        public RaschsvVyplSvDopMt mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplSvDopMt raschsvVyplSvDopMt = new RaschsvVyplSvDopMt();
            raschsvVyplSvDopMt.setId(SqlUtils.getLong(rs, RaschsvVyplSvDop.COL_ID));
            raschsvVyplSvDopMt.setRaschsvVyplSvDopId(SqlUtils.getLong(rs, RaschsvVyplSvDopMt.COL_RASCHSV_VYPL_SV_DOP_ID));
            raschsvVyplSvDopMt.setMesyac(rs.getString(RaschsvVyplSvDopMt.COL_MESYAC));
            raschsvVyplSvDopMt.setTarif(rs.getString(RaschsvVyplSvDopMt.COL_TARIF));
            raschsvVyplSvDopMt.setVyplSv(rs.getDouble(RaschsvVyplSvDopMt.COL_VYPL_SV));
            raschsvVyplSvDopMt.setNachislSv(rs.getDouble(RaschsvVyplSvDopMt.COL_NACHISL_SV));

            return raschsvVyplSvDopMt;
        }
    }
}
