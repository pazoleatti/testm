package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional
public class RaschsvPersSvStrahLicDaoImpl extends AbstractDao implements RaschsvPersSvStrahLicDao {

    private static final String SV_VYPL_ALIAS = "sv";
    private static final String SV_VYPL_MT_ALIAS = "svm";
    private static final String VYPL_SV_DOP_ALIAS = "svd";
    private static final String VYPL_SV_DOP_MT_ALIAS = "svdm";
    private static final String SUBREPORT_PARAM_FAMILIA_ALIAS = "familiya";
    private static final String SUBREPORT_PARAM_IMYA_ALIAS = "imya";
    private static final String SUBREPORT_PARAM_OTCHESTVO_ALIAS = "otchestvo";
    private static final String SUBREPORT_PARAM_SNILS_ALIAS = "snils";
    private static final String SUBREPORT_PARAM_INN_ALIAS = "inn";

    // Перечень столбцов таблицы ПерсСвСтрахЛиц
    private static final String PERS_SV_STRAH_LIC_COLS = SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, null);
    private static final String PERS_SV_STRAH_LIC_FIELDS = SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, ":");

    // Перечень столбцов таблицы СвВыпл
    private static final String SV_VYPL_COLS = SqlUtils.getColumnsToString(RaschsvSvVypl.COLUMNS, null);
    private static final String SV_VYPL_FIELDS = SqlUtils.getColumnsToString(RaschsvSvVypl.COLUMNS, ":");

    // Перечень столбцов таблицы СвВыплМК
    private static final String SV_VYPL_MK_COLS = SqlUtils.getColumnsToString(RaschsvSvVyplMt.COLUMNS, null);
    private static final String SV_VYPL_MK_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvVyplMt.COLUMNS, SV_VYPL_MT_ALIAS + ".");
    private static final String SV_VYPL_MK_FIELDS = SqlUtils.getColumnsToString(RaschsvSvVyplMt.COLUMNS, ":");

    // Перечень столбцов таблицы ВыплСВДоп
    private static final String VYPL_SV_DOP_COLS = SqlUtils.getColumnsToString(RaschsvVyplSvDop.COLUMNS, null);
    private static final String VYPL_SV_DOP_FIELDS = SqlUtils.getColumnsToString(RaschsvVyplSvDop.COLUMNS, ":");

    // Перечень столбцов таблицы ВыплСВДопМТ
    private static final String VYPL_SV_DOP_MT_COLS = SqlUtils.getColumnsToString(RaschsvVyplSvDopMt.COLUMNS, null);
    private static final String VYPL_SV_DOP_MT_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvVyplSvDopMt.COLUMNS, VYPL_SV_DOP_MT_ALIAS + ".");
    private static final String VYPL_SV_DOP_MT_FIELDS = SqlUtils.getColumnsToString(RaschsvVyplSvDopMt.COLUMNS, ":");

    // sql запрос для сохранения в ПерсСвСтрахЛиц
    private static final String SQL_INSERT_PERS_SV_STRAH_LIC = "INSERT INTO " + RaschsvPersSvStrahLic.TABLE_NAME +
            " (" + PERS_SV_STRAH_LIC_COLS + ") VALUES (" + PERS_SV_STRAH_LIC_FIELDS + ")";

    // sql запрос для сохранения в СвВыпл
    private static final String SQL_INSERT_SV_VYPL = "INSERT INTO " + RaschsvSvVypl.TABLE_NAME +
            " (" + SV_VYPL_COLS + ") VALUES (" + SV_VYPL_FIELDS + ")";

    // sql запрос для сохранения в СвВыплМК
    private static final String SQL_INSERT_SV_VYPL_MT = "INSERT INTO " + RaschsvSvVyplMt.TABLE_NAME +
            " (" + SV_VYPL_MK_COLS + ") VALUES (" + SV_VYPL_MK_FIELDS + ")";

    // sql запрос для сохранения в ВыплСВДоп
    private static final String SQL_INSERT_VYPL_SV_DOP = "INSERT INTO " + RaschsvVyplSvDop.TABLE_NAME +
            " (" + VYPL_SV_DOP_COLS + ") VALUES (" + VYPL_SV_DOP_FIELDS + ")";

    // sql запрос для сохранения в ВыплСВДопМТ
    private static final String SQL_INSERT_VYPL_SV_DOP_MT = "INSERT INTO " + RaschsvVyplSvDopMt.TABLE_NAME +
            " (" + VYPL_SV_DOP_MT_COLS + ") VALUES (" + VYPL_SV_DOP_MT_FIELDS + ")";

    // sql запрос для выборки из ПерсСвСтрахЛиц по ИНН ФЛ и идентификатору декларации
    private static final String SQL_SELECT_PERSONS_BY_INNFL = "SELECT " + PERS_SV_STRAH_LIC_COLS + " FROM " + RaschsvPersSvStrahLic.TABLE_NAME +
            " WHERE " + RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID + " = :" + RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID + " AND " +
            RaschsvPersSvStrahLic.COL_INNFL + " = :" + RaschsvPersSvStrahLic.COL_INNFL;

    // sql запрос для выборки из ПерсСвСтрахЛиц по идентификатору декларации
    private static final String SQL_SELECT_PERSONS = "SELECT " + PERS_SV_STRAH_LIC_COLS + " FROM " + RaschsvPersSvStrahLic.TABLE_NAME +
            " WHERE " + RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID + " = :" + RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID;

    // sql запрос для выборки из СвВыпл
    private static final String SQL_SELECT_SV_VYPL_BY_PERSON_IDS = "SELECT " + SV_VYPL_COLS + " FROM " + RaschsvSvVypl.TABLE_NAME +
            " WHERE ";

    // sql запрос для выборки из СвВыплМК
    private static final StringBuilder SQL_SELECT_SV_VYPL_MT_BY_PERSON_IDS = new StringBuilder()
            .append("SELECT " + SV_VYPL_MK_COLS_WITH_ALIAS + " FROM " + RaschsvSvVyplMt.TABLE_NAME + " " + SV_VYPL_MT_ALIAS)
            .append(" INNER JOIN " + RaschsvSvVypl.TABLE_NAME + " " + SV_VYPL_ALIAS +
                    " ON " + SV_VYPL_MT_ALIAS + "." + RaschsvSvVyplMt.COL_RASCHSV_SV_VYPL_ID + " = " + SV_VYPL_ALIAS + "." + RaschsvSvVypl.COL_ID)
            .append( " WHERE ");

    // sql запрос для выборки из ВыплСВДоп
    private static final String SQL_SELECT_VYPL_SV_DOP_BY_PERSON_IDS = "SELECT " + VYPL_SV_DOP_COLS + " FROM " + RaschsvVyplSvDop.TABLE_NAME +
            " WHERE ";

    // sql запрос для выборки из ВыплСВДопМТ
    private static final StringBuilder SQL_SELECT_VYPL_SV_DOP_MT_BY_PERSON_IDS = new StringBuilder()
            .append("SELECT " + VYPL_SV_DOP_MT_COLS_WITH_ALIAS + " FROM " + RaschsvVyplSvDopMt.TABLE_NAME + " " + VYPL_SV_DOP_MT_ALIAS)
            .append(" INNER JOIN " + RaschsvVyplSvDop.TABLE_NAME + " " + VYPL_SV_DOP_ALIAS +
                    " ON " + VYPL_SV_DOP_MT_ALIAS + "." + RaschsvVyplSvDopMt.COL_RASCHSV_VYPL_SV_DOP_ID + " = " + VYPL_SV_DOP_ALIAS + "." + RaschsvVyplSvDop.COL_ID)
            .append( " WHERE ");

    @Override
    public List<RaschsvPersSvStrahLic> findPersons(Long declarationDataId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID, declarationDataId);
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList =
                getNamedParameterJdbcTemplate().query(SQL_SELECT_PERSONS, params, new RaschsvPersSvStrahLicRowMapper());

        return findSvVyplAndVyplSvDopByPersons(raschsvPersSvStrahLicList);
    }

    @Override
    public RaschsvPersSvStrahLic findPersonByInn(Long declarationDataId, String innfl) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID, declarationDataId)
                    .addValue(RaschsvPersSvStrahLic.COL_INNFL, innfl);
            RaschsvPersSvStrahLic raschsvPersSvStrahLic =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_PERSONS_BY_INNFL, params, new RaschsvPersSvStrahLicRowMapper());
            List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = findSvVyplAndVyplSvDopByPersons(Arrays.asList(raschsvPersSvStrahLic));

            return raschsvPersSvStrahLicList.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public RaschsvPersSvStrahLic findPersonBySubreportParams(Long declarationDataId, Map<String, Object> subreportParams) {
        try {
            MapSqlParameterSource sqlParams = new MapSqlParameterSource();
            String query = new String(SQL_SELECT_PERSONS);
            sqlParams.addValue(RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID, declarationDataId);
            for (String alias : subreportParams.keySet()) {
                Object paramValue = subreportParams.get(alias);
                if (paramValue != null) {
                    if (alias.equalsIgnoreCase(SUBREPORT_PARAM_FAMILIA_ALIAS)) {
                        query += " AND " + RaschsvPersSvStrahLic.COL_FAMILIA + " = :" + RaschsvPersSvStrahLic.COL_FAMILIA;
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_FAMILIA, (String) paramValue);
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_IMYA_ALIAS)) {
                        query += " AND " + RaschsvPersSvStrahLic.COL_IMYA + " = :" + RaschsvPersSvStrahLic.COL_IMYA;
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_IMYA, (String) paramValue);
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_OTCHESTVO_ALIAS)) {
                        query += " AND " + RaschsvPersSvStrahLic.COL_MIDDLE_NAME + " = :" + RaschsvPersSvStrahLic.COL_MIDDLE_NAME;
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_MIDDLE_NAME, (String) paramValue);
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_SNILS_ALIAS)) {
                        query += " AND " + RaschsvPersSvStrahLic.COL_SNILS + " = :" + RaschsvPersSvStrahLic.COL_SNILS;
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_SNILS, (String) paramValue);
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_INN_ALIAS)) {
                        query += " AND " + RaschsvPersSvStrahLic.COL_INNFL + " = :" + RaschsvPersSvStrahLic.COL_INNFL;
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_INNFL, (String) paramValue);
                    }
                }
            }
            List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = new ArrayList<RaschsvPersSvStrahLic>();
            raschsvPersSvStrahLicList.add(getNamedParameterJdbcTemplate().queryForObject(query, sqlParams, new RaschsvPersSvStrahLicRowMapper()));
            return findSvVyplAndVyplSvDopByPersons(raschsvPersSvStrahLicList).get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Получение записей из таблиц СвВыпл, СвВыплМК, ВыплСВДоп, ВыплСВДопМТ
     * @param raschsvPersSvStrahLicList
     * @return
     */
    private List<RaschsvPersSvStrahLic> findSvVyplAndVyplSvDopByPersons(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList) {
        if (!raschsvPersSvStrahLicList.isEmpty()) {
            // Перечень идентификаторов ПерсСвСтрахЛиц
            List<Long> persSvStrahLicIds = new ArrayList<Long>(raschsvPersSvStrahLicList.size());

            Map<Long, RaschsvPersSvStrahLic> mapPersSvStrahLic = new HashMap<Long, RaschsvPersSvStrahLic>();
            for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
                mapPersSvStrahLic.put(raschsvPersSvStrahLic.getId(), raschsvPersSvStrahLic);
                persSvStrahLicIds.add(raschsvPersSvStrahLic.getId());
            }

            // Получим СвВыпл
            List<RaschsvSvVypl> raschsvSvVyplList = findSvVyplListByPersonIds(persSvStrahLicIds);
            Map<Long, RaschsvSvVypl> mapSvVypl = new HashMap<Long, RaschsvSvVypl>();
            for (RaschsvSvVypl raschsvSvVypl : raschsvSvVyplList) {
                RaschsvPersSvStrahLic raschsvPersSvStrahLic = mapPersSvStrahLic.get(raschsvSvVypl.getRaschsvPersSvStrahLicId());
                raschsvPersSvStrahLic.setRaschsvSvVypl(raschsvSvVypl);

                mapSvVypl.put(raschsvSvVypl.getId(), raschsvSvVypl);
            }

            // Получим СвВыплМК
            List<RaschsvSvVyplMt> raschsvSvVyplMtList = findSvVyplMtListByPersonIds(persSvStrahLicIds);
            for (RaschsvSvVyplMt raschsvSvVyplMt : raschsvSvVyplMtList) {
                RaschsvSvVypl raschsvSvVypl = mapSvVypl.get(raschsvSvVyplMt.getRaschsvSvVyplId());
                raschsvSvVypl.addRaschsvSvVyplMt(raschsvSvVyplMt);
            }

            // Получим ВыплСВДоп
            List<RaschsvVyplSvDop> raschsvVyplSvDopList = findVyplSvDopListByPersonIds(persSvStrahLicIds);
            Map<Long, RaschsvVyplSvDop> mapVyplSvDop = new HashMap<Long, RaschsvVyplSvDop>();
            for (RaschsvVyplSvDop raschsvVyplSvDop : raschsvVyplSvDopList) {
                RaschsvPersSvStrahLic raschsvPersSvStrahLic = mapPersSvStrahLic.get(raschsvVyplSvDop.getRaschsvPersSvStrahLicId());
                raschsvPersSvStrahLic.setRaschsvVyplSvDop(raschsvVyplSvDop);

                mapVyplSvDop.put(raschsvVyplSvDop.getId(), raschsvVyplSvDop);
            }

            // Получим ВыплСВДопМТ
            List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = findVyplSvDopMtListByPersonIds(persSvStrahLicIds);
            for (RaschsvVyplSvDopMt raschsvVyplSvDopMt : raschsvVyplSvDopMtList) {
                RaschsvVyplSvDop raschsvVyplSvDop = mapVyplSvDop.get(raschsvVyplSvDopMt.getRaschsvVyplSvDopId());
                raschsvVyplSvDop.addRaschsvVyplSvDopMt(raschsvVyplSvDopMt);
            }
        }
        return raschsvPersSvStrahLicList;
    }

    /**
     * Выборка из СвВыпл
     * @param persSvStrahLicIds - перечень идентификаторов застрахованных лиц
     * @return
     */
    private List<RaschsvSvVypl> findSvVyplListByPersonIds(List<Long> persSvStrahLicIds) {
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SV_VYPL_BY_PERSON_IDS +
                SqlUtils.transformToSqlInStatement(RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, persSvStrahLicIds), new RaschsvSvVyplRowMapper());
    }

    /**
     * Выборка из СвВыплМК
     * @param persSvStrahLicIds - перечень идентификаторов застрахованных лиц
     * @return
     */
    private List<RaschsvSvVyplMt> findSvVyplMtListByPersonIds(List<Long> persSvStrahLicIds) {
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SV_VYPL_MT_BY_PERSON_IDS +
                SqlUtils.transformToSqlInStatement(SV_VYPL_ALIAS + "." + RaschsvSvVypl.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, persSvStrahLicIds), new RaschsvSvVyplMtRowMapper());
    }

    /**
     * Выборка из ВыплСВДоп
     * @param persSvStrahLicIds - перечень идентификаторов застрахованных лиц
     * @return
     */
    private List<RaschsvVyplSvDop> findVyplSvDopListByPersonIds(List<Long> persSvStrahLicIds) {
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_VYPL_SV_DOP_BY_PERSON_IDS.toString() +
                SqlUtils.transformToSqlInStatement(RaschsvVyplSvDop.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, persSvStrahLicIds), new RaschsvVyplSvDopRowMapper());
    }

    /**
     * Выборка из ВыплСВДопМТ
     * @param persSvStrahLicIds - перечень идентификаторов застрахованных лиц
     * @return
     */
    private List<RaschsvVyplSvDopMt> findVyplSvDopMtListByPersonIds(List<Long> persSvStrahLicIds) {
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_VYPL_SV_DOP_MT_BY_PERSON_IDS.toString() +
                SqlUtils.transformToSqlInStatement(VYPL_SV_DOP_ALIAS + "." + RaschsvVyplSvDop.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, persSvStrahLicIds), new RaschsvVyplSvDopMtRowMapper());
    }

    /**
     * Сохранение ПерсСвСтрахЛиц
     * @param raschsvPersSvStrahLicList - перечень сведений о застрахованных лицах
     * @return
     */
    @Override
    public Integer insertPersSvStrahLic(final List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList) {
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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_PERS_SV_STRAH_LIC,
                batchValues.toArray(new Map[raschsvPersSvStrahLicList.size()]));

        List<RaschsvSvVypl> raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
        List<RaschsvSvVyplMt> raschsvSvVyplMtList = new ArrayList<RaschsvSvVyplMt>();

        List<RaschsvVyplSvDop> raschsvVyplSvDopList = new ArrayList<RaschsvVyplSvDop>();
        List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = new ArrayList<RaschsvVyplSvDopMt>();

        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            RaschsvSvVypl raschsvSvVypl = raschsvPersSvStrahLic.getRaschsvSvVypl();
            if (raschsvSvVypl != null) {
                raschsvSvVypl.setId(generateId(RaschsvSvVypl.SEQ, Long.class));
                raschsvSvVypl.setRaschsvPersSvStrahLicId(raschsvPersSvStrahLic.getId());
                raschsvSvVyplList.add(raschsvSvVypl);

                for (RaschsvSvVyplMt raschsvSvVyplMt : raschsvSvVypl.getRaschsvSvVyplMtList()) {
                    raschsvSvVyplMt.setRaschsvSvVyplId(raschsvSvVypl.getId());
                    raschsvSvVyplMt.setId(generateId(RaschsvSvVyplMt.SEQ, Long.class));
                    raschsvSvVyplMtList.add(raschsvSvVyplMt);
                }
            }

            RaschsvVyplSvDop raschsvVyplSvDop = raschsvPersSvStrahLic.getRaschsvVyplSvDop();
            if (raschsvVyplSvDop != null) {
                raschsvVyplSvDop.setId(generateId(RaschsvVyplSvDop.SEQ, Long.class));
                raschsvVyplSvDop.setRaschsvPersSvStrahLicId(raschsvPersSvStrahLic.getId());
                raschsvVyplSvDopList.add(raschsvVyplSvDop);

                for (RaschsvVyplSvDopMt raschsvVyplSvDopMt : raschsvVyplSvDop.getRaschsvVyplSvDopMtList()) {
                    raschsvVyplSvDopMt.setRaschsvVyplSvDopId(raschsvVyplSvDop.getId());
                    raschsvVyplSvDopMt.setId(generateId(RaschsvVyplSvDopMt.SEQ, Long.class));
                    raschsvVyplSvDopMtList.add(raschsvVyplSvDopMt);
                }
            }
        }

        // Сохранение СвВыпл
        if (!raschsvSvVyplList.isEmpty()) {
            insertRaschsvSvVypl(raschsvSvVyplList);
        }

        // Сохранение ВыплСВДоп
        if (!raschsvVyplSvDopList.isEmpty()) {
            insertRaschsvVyplSvDop(raschsvVyplSvDopList);
        }

        // Сохранение "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица"
        if (!raschsvSvVyplMtList.isEmpty()) {
            insertRaschsvSvVyplMt(raschsvSvVyplMtList);
        }

        // Сохранение ВыплСВДопМТ
        if (!raschsvVyplSvDopMtList.isEmpty()) {
            insertRaschsvVyplSvDopMt(raschsvVyplSvDopMtList);
        }

        return res.length;
    }

    /**
     * Сохранение СвВыпл
     * @param raschsvSvVyplList - перечень сведений о сумме выплат
     * @return
     */
    private Integer insertRaschsvSvVypl(final List<RaschsvSvVypl> raschsvSvVyplList) {
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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SV_VYPL,
                batchValues.toArray(new Map[raschsvSvVyplList.size()]));

        return res.length;
    }

    /**
     * Сохранение СвВыплМК
     * @param raschsvSvVyplMtList - перечень сведений о сумме выплат по месяцу и коду категории застрахованного лица
     * @return
     */
    private Integer insertRaschsvSvVyplMt(final List<RaschsvSvVyplMt> raschsvSvVyplMtList) {
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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SV_VYPL_MT,
                batchValues.toArray(new Map[raschsvSvVyplMtList.size()]));

        return res.length;
    }

    /**
     * Сохранение ВыплСВДоп
     * @param raschsvVyplSvDopList - перечень сведений о сумме выплат по дополнительному тарифу
     * @return
     */
    private Integer insertRaschsvVyplSvDop(final List<RaschsvVyplSvDop> raschsvVyplSvDopList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvVyplSvDopList.size());
        for (RaschsvVyplSvDop raschsvVyplSvDop : raschsvVyplSvDopList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvVyplSvDop.COL_ID, raschsvVyplSvDop.getId())
                            .addValue(RaschsvVyplSvDop.COL_RASCHSV_PERS_SV_STRAH_LIC_ID, raschsvVyplSvDop.getRaschsvPersSvStrahLicId())
                            .addValue(RaschsvVyplSvDop.COL_VYPL_SV_VS3, raschsvVyplSvDop.getVyplSvVs3())
                            .addValue(RaschsvVyplSvDop.COL_NACHISL_SV_VS3, raschsvVyplSvDop.getNachislSvVs3())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_VYPL_SV_DOP,
                batchValues.toArray(new Map[raschsvVyplSvDopList.size()]));

        return res.length;
    }

    /**
     * Сохранение ВыплСВДопМТ
     * @param raschsvVyplSvDopMtList - перечень сведений о сумме выплат по дополнительному тарифу по месяцу и коду
     * @return
     */
    private Integer insertRaschsvVyplSvDopMt(final List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList) {
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
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_VYPL_SV_DOP_MT,
                batchValues.toArray(new Map[raschsvVyplSvDopMtList.size()]));

        return res.length;
    }

    /**
     * Маппинг для ПерсСвСтрахЛиц
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
     * Маппинг для СвВыпл
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
     * Маппинг для СвВыплМК
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
     * Маппинг для ВыплСВДоп
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
     * Маппинг для ВыплСВДопМТ
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