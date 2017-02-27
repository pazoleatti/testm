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

    private static final String PERS_SV_STRAH_LIC_ALIAS = "p";
    private static final String SV_VYPL_ALIAS = "sv";
    private static final String SV_VYPL_MT_ALIAS = "svm";
    private static final String VYPL_SV_DOP_ALIAS = "svd";
    private static final String VYPL_SV_DOP_MT_ALIAS = "svdm";
    private static final String SUBREPORT_PARAM_LASTNAME_ALIAS = "lastname";
    private static final String SUBREPORT_PARAM_NAME_ALIAS = "name";
    private static final String SUBREPORT_PARAM_MIDDLENAME_ALIAS = "middlename";
    private static final String SUBREPORT_PARAM_SNILS_ALIAS = "snils";
    private static final String SUBREPORT_PARAM_INN_ALIAS = "inn";
    private static final String SUBREPORT_PARAM_BIRHDAY_FROM_ALIAS = "birthday_from";
    private static final String SUBREPORT_PARAM_BIRHDAY_BEFORE_ALIAS = "birthday_before";
    private static final String SUBREPORT_PARAM_DOC_ALIAS = "doc";

    // Перечень столбцов таблицы ПерсСвСтрахЛиц
    private static final String PERS_SV_STRAH_LIC_COLS = SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, null);
    private static final String PERS_SV_STRAH_LIC_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, PERS_SV_STRAH_LIC_ALIAS + ".");
    private static final String PERS_SV_STRAH_LIC_FIELDS = SqlUtils.getColumnsToString(RaschsvPersSvStrahLic.COLUMNS, ":");

    // Перечень столбцов таблицы СвВыпл
    private static final String SV_VYPL_COLS = SqlUtils.getColumnsToString(RaschsvSvVypl.COLUMNS, null);
    private static final String SV_VYPL_FIELDS = SqlUtils.getColumnsToString(RaschsvSvVypl.COLUMNS, ":");

    // Перечень столбцов таблицы СвВыплМК
    private static final String SV_VYPL_MK_COLS = SqlUtils.getColumnsToString(RaschsvSvVyplMk.COLUMNS, null);
    private static final String SV_VYPL_MK_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvVyplMk.COLUMNS, SV_VYPL_MT_ALIAS + ".");
    private static final String SV_VYPL_MK_FIELDS = SqlUtils.getColumnsToString(RaschsvSvVyplMk.COLUMNS, ":");

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

    // sql запрос для обновления ПерсСвСтрахЛиц
    private static final String SQL_UPDATE_PERS_SV_STRAH_LIC = "UPDATE " + RaschsvPersSvStrahLic.TABLE_NAME +
            " SET " + RaschsvPersSvStrahLic.COL_PERSON_ID + " = :" + RaschsvPersSvStrahLic.COL_PERSON_ID +
            " WHERE " + RaschsvPersSvStrahLic.COL_ID + " = :" + RaschsvPersSvStrahLic.COL_ID;

    // sql запрос для сохранения в СвВыпл
    private static final String SQL_INSERT_SV_VYPL = "INSERT INTO " + RaschsvSvVypl.TABLE_NAME +
            " (" + SV_VYPL_COLS + ") VALUES (" + SV_VYPL_FIELDS + ")";

    // sql запрос для сохранения в СвВыплМК
    private static final String SQL_INSERT_SV_VYPL_MT = "INSERT INTO " + RaschsvSvVyplMk.TABLE_NAME +
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
            .append("SELECT " + SV_VYPL_MK_COLS_WITH_ALIAS + " FROM " + RaschsvSvVyplMk.TABLE_NAME + " " + SV_VYPL_MT_ALIAS)
            .append(" INNER JOIN " + RaschsvSvVypl.TABLE_NAME + " " + SV_VYPL_ALIAS +
                    " ON " + SV_VYPL_MT_ALIAS + "." + RaschsvSvVyplMk.COL_RASCHSV_SV_VYPL_ID + " = " + SV_VYPL_ALIAS + "." + RaschsvSvVypl.COL_ID)
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
    public RaschsvPersSvStrahLic get(long id) {
        String query = "SELECT " + PERS_SV_STRAH_LIC_COLS + " FROM " + RaschsvPersSvStrahLic.TABLE_NAME + " WHERE ID = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        RaschsvPersSvStrahLic raschsvPersSvStrahLic = getNamedParameterJdbcTemplate().queryForObject(query, params, new RaschsvPersSvStrahLicRowMapper());
        return findSvVyplAndVyplSvDopByPersons(Arrays.asList(raschsvPersSvStrahLic)).get(0);
    }

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
    public List<RaschsvPersSvStrahLic> findPersonBySubreportParams(Long declarationDataId, Map<String, Object> subreportParams) {
            MapSqlParameterSource sqlParams = new MapSqlParameterSource();
            String query = new String(SQL_SELECT_PERSONS);
            sqlParams.addValue(RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID, declarationDataId);
            for (String alias : subreportParams.keySet()) {
                Object paramValue = subreportParams.get(alias);
                if (paramValue != null) {
                    if (alias.equalsIgnoreCase(SUBREPORT_PARAM_LASTNAME_ALIAS)) {
                        query += " AND LOWER(" + RaschsvPersSvStrahLic.COL_FAMILIA + ") LIKE LOWER(:" + RaschsvPersSvStrahLic.COL_FAMILIA + ")";
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_FAMILIA, paramValue + "%");
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_NAME_ALIAS)) {
                        query += " AND LOWER(" + RaschsvPersSvStrahLic.COL_IMYA + ") LIKE LOWER(:" + RaschsvPersSvStrahLic.COL_IMYA + ")";
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_IMYA, paramValue + "%");
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_MIDDLENAME_ALIAS)) {
                        query += " AND LOWER(" + RaschsvPersSvStrahLic.COL_OTCHESTVO + ") LIKE LOWER(:" + RaschsvPersSvStrahLic.COL_OTCHESTVO + ")";
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_OTCHESTVO, paramValue + "%");
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_SNILS_ALIAS)) {
                        query += " AND LOWER(" + RaschsvPersSvStrahLic.COL_SNILS + ") LIKE LOWER(:" + RaschsvPersSvStrahLic.COL_SNILS + ")";
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_SNILS, paramValue + "%");
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_INN_ALIAS)) {
                        query += " AND LOWER(" + RaschsvPersSvStrahLic.COL_INNFL + ") LIKE LOWER(:" + RaschsvPersSvStrahLic.COL_INNFL + ")";
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_INNFL, paramValue + "%");
                    } else if (alias.equalsIgnoreCase(SUBREPORT_PARAM_DOC_ALIAS)) {
                        query += " AND LOWER(" + RaschsvPersSvStrahLic.COL_SER_NOM_DOC + ") LIKE LOWER(:" + RaschsvPersSvStrahLic.COL_SER_NOM_DOC + ")";
                        sqlParams.addValue(RaschsvPersSvStrahLic.COL_SER_NOM_DOC, paramValue + "%");
                    }
                }
            }
            if (subreportParams.get(SUBREPORT_PARAM_BIRHDAY_FROM_ALIAS) != null && subreportParams.get(SUBREPORT_PARAM_BIRHDAY_BEFORE_ALIAS) != null) {
                query += " AND " + RaschsvPersSvStrahLic.COL_DATA_ROZD + " BETWEEN :birthdayFrom AND :birthdayBefore";
                sqlParams.addValue("birthdayFrom", subreportParams.get(SUBREPORT_PARAM_BIRHDAY_FROM_ALIAS));
                sqlParams.addValue("birthdayBefore", subreportParams.get(SUBREPORT_PARAM_BIRHDAY_BEFORE_ALIAS));
            } else if (subreportParams.get(SUBREPORT_PARAM_BIRHDAY_FROM_ALIAS) != null) {
                query += " AND " + RaschsvPersSvStrahLic.COL_DATA_ROZD + " >= :" + RaschsvPersSvStrahLic.COL_DATA_ROZD;
                sqlParams.addValue(RaschsvPersSvStrahLic.COL_DATA_ROZD, subreportParams.get(SUBREPORT_PARAM_BIRHDAY_FROM_ALIAS));
            } else if (subreportParams.get(SUBREPORT_PARAM_BIRHDAY_BEFORE_ALIAS) != null) {
                query += " AND " + RaschsvPersSvStrahLic.COL_DATA_ROZD + " <= :" + RaschsvPersSvStrahLic.COL_DATA_ROZD;
                sqlParams.addValue(RaschsvPersSvStrahLic.COL_DATA_ROZD, subreportParams.get(SUBREPORT_PARAM_BIRHDAY_BEFORE_ALIAS));
            }
            List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = new ArrayList<RaschsvPersSvStrahLic>();
            raschsvPersSvStrahLicList.addAll(getNamedParameterJdbcTemplate().query(query, sqlParams, new RaschsvPersSvStrahLicRowMapper()));
            return findSvVyplAndVyplSvDopByPersons(raschsvPersSvStrahLicList);
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
            List<RaschsvSvVyplMk> raschsvSvVyplMkList = findSvVyplMtListByPersonIds(persSvStrahLicIds);
            for (RaschsvSvVyplMk raschsvSvVyplMk : raschsvSvVyplMkList) {
                RaschsvSvVypl raschsvSvVypl = mapSvVypl.get(raschsvSvVyplMk.getRaschsvSvVyplId());
                raschsvSvVypl.addRaschsvSvVyplMt(raschsvSvVyplMk);
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
    private List<RaschsvSvVyplMk> findSvVyplMtListByPersonIds(List<Long> persSvStrahLicIds) {
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
                            .addValue(RaschsvPersSvStrahLic.COL_OTCHESTVO, raschsvPersSvStrahLic.getOtchestvo())
                            .addValue(RaschsvPersSvStrahLic.COL_PERSON_ID, raschsvPersSvStrahLic.getPersonId())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_PERS_SV_STRAH_LIC,
                batchValues.toArray(new Map[raschsvPersSvStrahLicList.size()]));

        List<RaschsvSvVypl> raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
        List<RaschsvSvVyplMk> raschsvSvVyplMkList = new ArrayList<RaschsvSvVyplMk>();

        List<RaschsvVyplSvDop> raschsvVyplSvDopList = new ArrayList<RaschsvVyplSvDop>();
        List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = new ArrayList<RaschsvVyplSvDopMt>();

        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            RaschsvSvVypl raschsvSvVypl = raschsvPersSvStrahLic.getRaschsvSvVypl();
            if (raschsvSvVypl != null) {
                raschsvSvVypl.setId(generateId(RaschsvSvVypl.SEQ, Long.class));
                raschsvSvVypl.setRaschsvPersSvStrahLicId(raschsvPersSvStrahLic.getId());
                raschsvSvVyplList.add(raschsvSvVypl);

                for (RaschsvSvVyplMk raschsvSvVyplMk : raschsvSvVypl.getRaschsvSvVyplMkList()) {
                    raschsvSvVyplMk.setRaschsvSvVyplId(raschsvSvVypl.getId());
                    raschsvSvVyplMk.setId(generateId(RaschsvSvVyplMk.SEQ, Long.class));
                    raschsvSvVyplMkList.add(raschsvSvVyplMk);
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
        if (!raschsvSvVyplMkList.isEmpty()) {
            insertRaschsvSvVyplMt(raschsvSvVyplMkList);
        }

        // Сохранение ВыплСВДопМТ
        if (!raschsvVyplSvDopMtList.isEmpty()) {
            insertRaschsvVyplSvDopMt(raschsvVyplSvDopMtList);
        }

        return res.length;
    }

    @Override
    public Integer updatePersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicListList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvPersSvStrahLicListList.size());
        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicListList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvPersSvStrahLic.COL_ID, raschsvPersSvStrahLic.getId())
                            .addValue(RaschsvPersSvStrahLic.COL_PERSON_ID, raschsvPersSvStrahLic.getPersonId())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_UPDATE_PERS_SV_STRAH_LIC,
                batchValues.toArray(new Map[raschsvPersSvStrahLicListList.size()]));

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
     * @param raschsvSvVyplMkList - перечень сведений о сумме выплат по месяцу и коду категории застрахованного лица
     * @return
     */
    private Integer insertRaschsvSvVyplMt(final List<RaschsvSvVyplMk> raschsvSvVyplMkList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvVyplMkList.size());
        for (RaschsvSvVyplMk raschsvSvVyplMk : raschsvSvVyplMkList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvVyplMk.COL_ID, raschsvSvVyplMk.getId())
                            .addValue(RaschsvSvVyplMk.COL_RASCHSV_SV_VYPL_ID, raschsvSvVyplMk.getRaschsvSvVyplId())
                            .addValue(RaschsvSvVyplMk.COL_MESYAC, raschsvSvVyplMk.getMesyac())
                            .addValue(RaschsvSvVyplMk.COL_KOD_KAT_LIC, raschsvSvVyplMk.getKodKatLic())
                            .addValue(RaschsvSvVyplMk.COL_SUM_VYPL, raschsvSvVyplMk.getSumVypl())
                            .addValue(RaschsvSvVyplMk.COL_VYPL_OPS, raschsvSvVyplMk.getVyplOps())
                            .addValue(RaschsvSvVyplMk.COL_VYPL_OPS_DOG, raschsvSvVyplMk.getVyplOpsDog())
                            .addValue(RaschsvSvVyplMk.COL_NACHISL_SV, raschsvSvVyplMk.getNachislSv())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SV_VYPL_MT,
                batchValues.toArray(new Map[raschsvSvVyplMkList.size()]));

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

    @Override
    public List<RaschsvPersSvStrahLic> findDublicatePersonIdByDeclarationDataId(long declarationDataId) {
        String sql = "select * from (" +
                " select " + PERS_SV_STRAH_LIC_COLS +
                " ,count(*) over (partition by person_id) cnt " +
                " from RASCHSV_PERS_SV_STRAH_LIC " +
                " where declaration_data_id = :declaration_data_id and person_id is not null " +
                " ) where cnt > 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declaration_data_id", declarationDataId);
        return getNamedParameterJdbcTemplate().query(sql, params, new RaschsvPersSvStrahLicDaoImpl.RaschsvPersSvStrahLicRowMapper());
    }

    @Override
    public List<RaschsvPersSvStrahLic> findDublicatePersonIdByReportPeriodId(List<Long> personIdList, long reportPeriodId) {
        String sql = "select * from (" +
                " select " + PERS_SV_STRAH_LIC_COLS_WITH_ALIAS +
                " ,count(*) over (partition by p.person_id) cnt " +
                " from RASCHSV_PERS_SV_STRAH_LIC p " +
                " inner join DECLARATION_DATA dd on p.declaration_data_id = dd.id " +
                " inner join DEPARTMENT_REPORT_PERIOD drp on dd.department_report_period_id = drp.id " +
                " where drp.report_period_id = :report_period_id and (" + SqlUtils.transformToSqlInStatement("p.person_id", personIdList) + ") " +
                " ) where cnt > 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("report_period_id", reportPeriodId);
        return getNamedParameterJdbcTemplate().query(sql, params, new RaschsvPersSvStrahLicDaoImpl.RaschsvPersSvStrahLicRowMapper());
    }

    //>-------------------------<The DAO row mappers>-----------------------------<

    /**
     * Маппинг для ПерсСвСтрахЛиц
     */
    private static final class RaschsvPersSvStrahLicRowMapper implements RowMapper<RaschsvPersSvStrahLic> {
        @Override
        public RaschsvPersSvStrahLic mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvPersSvStrahLic raschsvPersSvStrahLic = new RaschsvPersSvStrahLic();
            raschsvPersSvStrahLic.setId(SqlUtils.getLong(rs, RaschsvPersSvStrahLic.COL_ID));
            raschsvPersSvStrahLic.setDeclarationDataId(SqlUtils.getLong(rs, RaschsvPersSvStrahLic.COL_DECLARATION_DATA_ID));
            raschsvPersSvStrahLic.setNomKorr(rs.getInt(RaschsvPersSvStrahLic.COL_NOM_KORR));
            raschsvPersSvStrahLic.setPeriod(rs.getString(RaschsvPersSvStrahLic.COL_PERIOD));
            raschsvPersSvStrahLic.setOtchetGod(rs.getString(RaschsvPersSvStrahLic.COL_OTCHET_GOD));
            raschsvPersSvStrahLic.setNomer(rs.getInt(RaschsvPersSvStrahLic.COL_NOMER));
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
            raschsvPersSvStrahLic.setPersonId(SqlUtils.getLong(rs, RaschsvPersSvStrahLic.COL_PERSON_ID));

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
            raschsvSvVypl.setSumVyplVs3(rs.getBigDecimal(RaschsvSvVypl.COL_SUM_VYPL_VS3));
            raschsvSvVypl.setVyplOpsVs3(rs.getBigDecimal(RaschsvSvVypl.COL_VYPL_OPS_VS3));
            raschsvSvVypl.setVyplOpsDogVs3(rs.getBigDecimal(RaschsvSvVypl.COL_VYPL_OPS_DOG_VS3));
            raschsvSvVypl.setNachislSvVs3(rs.getBigDecimal(RaschsvSvVypl.COL_NACHISL_SV_VS3));

            return raschsvSvVypl;
        }
    }

    /**
     * Маппинг для СвВыплМК
     */
    private static final class RaschsvSvVyplMtRowMapper implements RowMapper<RaschsvSvVyplMk> {
        @Override
        public RaschsvSvVyplMk mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvVyplMk raschsvSvVyplMk = new RaschsvSvVyplMk();
            raschsvSvVyplMk.setId(SqlUtils.getLong(rs, RaschsvSvVyplMk.COL_ID));
            raschsvSvVyplMk.setRaschsvSvVyplId(SqlUtils.getLong(rs, RaschsvSvVyplMk.COL_RASCHSV_SV_VYPL_ID));
            raschsvSvVyplMk.setMesyac(rs.getString(RaschsvSvVyplMk.COL_MESYAC));
            raschsvSvVyplMk.setKodKatLic(rs.getString(RaschsvSvVyplMk.COL_KOD_KAT_LIC));
            raschsvSvVyplMk.setSumVypl(rs.getBigDecimal(RaschsvSvVyplMk.COL_SUM_VYPL));
            raschsvSvVyplMk.setVyplOps(rs.getBigDecimal(RaschsvSvVyplMk.COL_VYPL_OPS));
            raschsvSvVyplMk.setVyplOpsDog(rs.getBigDecimal(RaschsvSvVyplMk.COL_VYPL_OPS_DOG));
            raschsvSvVyplMk.setNachislSv(rs.getBigDecimal(RaschsvSvVyplMk.COL_NACHISL_SV));

            return raschsvSvVyplMk;
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
            raschsvVyplSvDop.setVyplSvVs3(rs.getBigDecimal(RaschsvVyplSvDop.COL_VYPL_SV_VS3));
            raschsvVyplSvDop.setNachislSvVs3(rs.getBigDecimal(RaschsvVyplSvDop.COL_NACHISL_SV_VS3));

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
            raschsvVyplSvDopMt.setVyplSv(rs.getBigDecimal(RaschsvVyplSvDopMt.COL_VYPL_SV));
            raschsvVyplSvDopMt.setNachislSv(rs.getBigDecimal(RaschsvVyplSvDopMt.COL_NACHISL_SV));

            return raschsvVyplSvDopMt;
        }
    }
}