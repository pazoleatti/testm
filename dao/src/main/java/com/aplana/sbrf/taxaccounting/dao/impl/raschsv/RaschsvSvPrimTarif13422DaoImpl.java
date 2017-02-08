package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvSvSum1TipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif13422Dao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class RaschsvSvPrimTarif13422DaoImpl extends AbstractDao implements RaschsvSvPrimTarif13422Dao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    private static final String SVED_OBUCH_ALIAS = "o";
    private static final String TARIF_ALIAS = "t";
    private static final String SUM_ALIAS = "s";
    private static final String IT_ALIAS = "i";

    // Перечень столбцов таблицы СвПримТариф1.3.422
    private static final StringBuilder TARIF_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif13422.COLUMNS, null));
    private static final StringBuilder SV_PRIM_TARIF_13422_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif13422.COLUMNS, ":"));

    // Перечень столбцов таблицы СведОбуч
    private static final StringBuilder SVED_OBUCH_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedObuch.COLUMNS, null));
    private static final StringBuilder SVED_OBUCH_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedObuch.COLUMNS, ":"));

    // Перечень столбцов таблицы СвРеестрМДО
    private static final StringBuilder SV_REESTR_MDO_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvReestrMdo.COLUMNS, null));
    private static final StringBuilder SV_REESTR_MDO_COLS_MITH_ALIAS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvReestrMdo.COLUMNS, TARIF_ALIAS + "."));
    private static final StringBuilder SV_REESTR_MDO_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvReestrMdo.COLUMNS, ":"));

    // Перечень столбцов таблицы ВыплатИт
    private static final StringBuilder VYPLAT_IT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt422.COLUMNS, null));
    private static final StringBuilder VYPLAT_IT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt422.COLUMNS, ":"));

    // Перечень столбцов таблицы "СвСум1Тип"
    private static final String SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, SUM_ALIAS + ".");

    private static final String SQL_INSERT_TARIF = "INSERT INTO " + RaschsvSvPrimTarif13422.TABLE_NAME +
            " (" + TARIF_COLS + ") VALUES (" + SV_PRIM_TARIF_13422_FIELDS + ")";

    private static final String SQL_INSERT_IT = "INSERT INTO " + RaschsvVyplatIt422.TABLE_NAME +
            " (" + VYPLAT_IT_COLS + ") VALUES (" + VYPLAT_IT_FIELDS + ")";

    private static final String SQL_INSERT_SVED_OBUCH = "INSERT INTO " + RaschsvSvedObuch.TABLE_NAME +
            " (" + SVED_OBUCH_COLS + ") VALUES (" + SVED_OBUCH_FIELDS + ")";

    private static final String SQL_INSERT_SV_REEESTR_MDO = "INSERT INTO " + RaschsvSvReestrMdo.TABLE_NAME +
            " (" + SV_REESTR_MDO_COLS + ") VALUES (" + SV_REESTR_MDO_FIELDS + ")";

    private static final String SQL_SELECT_TARIF = "SELECT " + TARIF_COLS + " FROM " + RaschsvSvPrimTarif13422.TABLE_NAME +
            " WHERE " + RaschsvSvPrimTarif13422.COL_RASCHSV_OBYAZ_PLAT_SV_ID + " = :" + RaschsvSvPrimTarif13422.COL_RASCHSV_OBYAZ_PLAT_SV_ID;

    private static final String SQL_SELECT_SVED_OBUCH = "SELECT " + SVED_OBUCH_COLS + " FROM " + RaschsvSvedObuch.TABLE_NAME +
            " WHERE " + RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID + " = :" + RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID;

    private static final StringBuilder SQL_SELECT_SV_REESTR_MDO = new StringBuilder()
            .append("SELECT " + SV_REESTR_MDO_COLS_MITH_ALIAS + " FROM " + RaschsvSvReestrMdo.TABLE_NAME + " " + TARIF_ALIAS)
            .append(" INNER JOIN " + RaschsvSvedObuch.TABLE_NAME + " " + SVED_OBUCH_ALIAS +
                    " ON " + TARIF_ALIAS + "." + RaschsvSvReestrMdo.COL_RASCHSV_SVED_OBUCH_ID + " = " + SVED_OBUCH_ALIAS + "." + RaschsvSvedObuch.COL_ID)
            .append(" WHERE " + SVED_OBUCH_ALIAS + "." + RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID + " = :" + RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID);

    private static final String SQL_SELECT_IT = "SELECT " + VYPLAT_IT_COLS + " FROM " + RaschsvVyplatIt422.TABLE_NAME +
            " WHERE " + RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID + " = :" + RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID;

    private static final StringBuilder SQL_SELECT_SUM = new StringBuilder()
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvVyplatIt422.TABLE_NAME + " " + IT_ALIAS +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + IT_ALIAS + "." + RaschsvVyplatIt422.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append( " WHERE " + IT_ALIAS + "." + RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID + " = :" + RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID)
            .append(" UNION ALL ")
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvSvedObuch.TABLE_NAME + " " + SVED_OBUCH_ALIAS +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + SVED_OBUCH_ALIAS + "." + RaschsvSvedObuch.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append(" WHERE " + SVED_OBUCH_ALIAS + "." + RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID + " = :" + RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID);

    public Long insertRaschsvSvPrimTarif13422(RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422) {
        // Генерация идентификатора
        raschsvSvPrimTarif13422.setId(generateId(RaschsvSvPrimTarif13422.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvPrimTarif13422.COL_ID, raschsvSvPrimTarif13422.getId())
                .addValue(RaschsvSvPrimTarif13422.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvPrimTarif13422.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_TARIF.toString(), params);

        // Сохранение "Сведения об обучающихся"
        List<RaschsvSvedObuch> raschsvSvedObuchList = new ArrayList<RaschsvSvedObuch>();
        List<RaschsvSvReestrMdo> raschsvSvReestrMdoList = new ArrayList<RaschsvSvReestrMdo>();
        for (RaschsvSvedObuch raschsvSvedObuch : raschsvSvPrimTarif13422.getRaschsvSvedObuchList()) {
            // Генерация идентификатора
            raschsvSvedObuch.setId(generateId(RaschsvSvedObuch.SEQ, Long.class));

            for (RaschsvSvReestrMdo raschsvSvReestrMdo : raschsvSvedObuch.getRaschsvSvReestrMdoList()) {
                raschsvSvReestrMdo.setId(generateId(RaschsvSvReestrMdo.SEQ, Long.class));
                raschsvSvReestrMdo.setRaschsvSvedObuchId(raschsvSvedObuch.getId());
                raschsvSvReestrMdoList.add(raschsvSvReestrMdo);
            }

            // Установка внешнего ключа
            raschsvSvedObuch.setRaschsvSvPrimTarif1422Id(raschsvSvPrimTarif13422.getId());

            // Сохранение "Сведения по суммам (тип 1)"
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvedObuch.getRaschsvSvSum1Tip());
            raschsvSvedObuch.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvSvedObuchList.add(raschsvSvedObuch);
        }
        // Сохранение "Сведения об обучающихся"
        insertRaschsvSvedObuch(raschsvSvedObuchList);

        // Сохранение "Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой"
        insertRaschsvSvReestrMdo(raschsvSvReestrMdoList);

        // Установка внешнего ключа
        RaschsvVyplatIt422 raschsvVyplatIt422 = raschsvSvPrimTarif13422.getRaschsvVyplatIt422();
        raschsvVyplatIt422.setRaschsvSvPrimTarif1422Id(raschsvSvPrimTarif13422.getId());

        // Сохранение "Сведения по суммам (тип 1)"
        Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvVyplatIt422.getRaschsvSvSum1Tip());
        raschsvVyplatIt422.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

        // Сохранение "Итого выплат"
        insertRaschsvVyplatIt422(raschsvVyplatIt422);

        return raschsvSvPrimTarif13422.getId();
    }

    /**
     * Сохранение "Сведения об обучающихся"
     * @param raschsvSvedObuchList
     * @return
     */
    private Integer insertRaschsvSvedObuch(List<RaschsvSvedObuch> raschsvSvedObuchList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvedObuchList.size());
        for (RaschsvSvedObuch raschsvSvedObuch : raschsvSvedObuchList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvSvedObuch.getRaschsvSvPrimTarif1422Id())
                            .addValue(RaschsvSvedObuch.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvedObuch.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvedObuch.COL_ID, raschsvSvedObuch.getId())
                            .addValue(RaschsvSvedObuch.COL_UNIK_NOMER, raschsvSvedObuch.getUnikNomer())
                            .addValue(RaschsvSvedObuch.COL_FAMILIA, raschsvSvedObuch.getFamilia())
                            .addValue(RaschsvSvedObuch.COL_IMYA, raschsvSvedObuch.getImya())
                            .addValue(RaschsvSvedObuch.COL_OTCHESTVO, raschsvSvedObuch.getOtchestvo())
                            .addValue(RaschsvSvedObuch.COL_SPRAV_NOMER, raschsvSvedObuch.getSpravNomer())
                            .addValue(RaschsvSvedObuch.COL_SPRAV_DATA, raschsvSvedObuch.getSpravData())
                            .addValue(RaschsvSvedObuch.COL_SPRAV_NODE_NAME, raschsvSvedObuch.getSpravNodeName())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SVED_OBUCH,
                batchValues.toArray(new Map[raschsvSvedObuchList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой"
     * @param raschsvSvReestrMdoList
     * @return
     */
    private Integer insertRaschsvSvReestrMdo(List<RaschsvSvReestrMdo> raschsvSvReestrMdoList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvReestrMdoList.size());
        for (RaschsvSvReestrMdo raschsvSvReestrMdo : raschsvSvReestrMdoList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvReestrMdo.COL_RASCHSV_SVED_OBUCH_ID, raschsvSvReestrMdo.getRaschsvSvedObuchId())
                            .addValue(RaschsvSvReestrMdo.COL_ID, raschsvSvReestrMdo.getId())
                            .addValue(RaschsvSvReestrMdo.COL_NAIM_MDO, raschsvSvReestrMdo.getNaimMdo())
                            .addValue(RaschsvSvReestrMdo.COL_DATA_ZAPIS, raschsvSvReestrMdo.getDataZapis())
                            .addValue(RaschsvSvReestrMdo.COL_NOMER_ZAPIS, raschsvSvReestrMdo.getNomerZapis())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SV_REEESTR_MDO,
                batchValues.toArray(new Map[raschsvSvReestrMdoList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Итого выплат"
     * @param raschsvVyplatIt422
     * @return
     */
    private Long insertRaschsvVyplatIt422(RaschsvVyplatIt422 raschsvVyplatIt422) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvVyplatIt422.getRaschsvSvPrimTarif1422Id())
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvVyplatIt422.getRaschsvSvSum1Tip().getId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_IT.toString(), params);

        return raschsvVyplatIt422.getRaschsvSvSum1Tip().getId();
    }

    public RaschsvSvPrimTarif13422 findRaschsvSvPrimTarif13422(Long obyazPlatSvId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvSvPrimTarif13422.COL_RASCHSV_OBYAZ_PLAT_SV_ID, obyazPlatSvId);
            RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422 =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_TARIF, params, new RaschsvSvPrimTarif13422RowMapper());

            // Выборка из СвСум1Тип
            List<RaschsvSvSum1Tip> raschsvSvSum1TipList = findRaschsvSvSum1Tip(raschsvSvPrimTarif13422.getId());
            Map<Long, RaschsvSvSum1Tip> mapRaschsvSvSum1Tip = new HashMap<Long, RaschsvSvSum1Tip>();
            for (RaschsvSvSum1Tip raschsvSvSum1Tip : raschsvSvSum1TipList) {
                mapRaschsvSvSum1Tip.put(raschsvSvSum1Tip.getId(), raschsvSvSum1Tip);
            }

            // Выборка из ВыплатИт
            RaschsvVyplatIt422 raschsvVyplatIt422 = findRaschsvVyplatIt422(raschsvSvPrimTarif13422.getId());
            raschsvVyplatIt422.setRaschsvSvSum1Tip(mapRaschsvSvSum1Tip.get(raschsvVyplatIt422.getRaschsvSvSum1TipId()));
            raschsvSvPrimTarif13422.setRaschsvVyplatIt422(raschsvVyplatIt422);

            // Выборка из СведОбуч
            List<RaschsvSvedObuch> raschsvSvedObuchList = findRaschsvSvedObuch(raschsvSvPrimTarif13422.getId());
            Map<Long, RaschsvSvedObuch> mapRaschsvSvedObuch = new HashMap<Long, RaschsvSvedObuch>();
            for (RaschsvSvedObuch raschsvSvedObuch : raschsvSvedObuchList) {
                raschsvSvedObuch.setRaschsvSvSum1Tip(mapRaschsvSvSum1Tip.get(raschsvSvedObuch.getRaschsvSvSum1TipId()));
                mapRaschsvSvedObuch.put(raschsvSvedObuch.getId(), raschsvSvedObuch);
            }

            // Выборка из СвРеестрМДО
            List<RaschsvSvReestrMdo> raschsvSvReestrMdoList = findRaschsvSvReestrMdo(raschsvSvPrimTarif13422.getId());
            for (RaschsvSvReestrMdo raschsvSvReestrMdo : raschsvSvReestrMdoList) {
                RaschsvSvedObuch raschsvSvedObuch = mapRaschsvSvedObuch.get(raschsvSvReestrMdo.getRaschsvSvedObuchId());
                raschsvSvedObuch.addRaschsvSvReestrMdo(raschsvSvReestrMdo);
            }

            raschsvSvPrimTarif13422.setRaschsvSvedObuchList(raschsvSvedObuchList);

            return raschsvSvPrimTarif13422;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из ВыплатИт
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private RaschsvVyplatIt422 findRaschsvVyplatIt422(Long raschsvSvPrimTarif13422Id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvSvPrimTarif13422Id);
            return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_IT, params, new RaschsvVyplatIt422RowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из СведОбуч
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private List<RaschsvSvedObuch> findRaschsvSvedObuch(Long raschsvSvPrimTarif13422Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvSvPrimTarif13422Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SVED_OBUCH, params, new RaschsvSvedObuchRowMapper());
    }

    /**
     * Выборка из СвРеестрМДО
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private List<RaschsvSvReestrMdo> findRaschsvSvReestrMdo(Long raschsvSvPrimTarif13422Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvSvPrimTarif13422Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SV_REESTR_MDO.toString(), params, new RaschsvSvReestrMdoRowMapper());
    }

    /**
     * Выборка из СвСум1Тип
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private List<RaschsvSvSum1Tip> findRaschsvSvSum1Tip(Long raschsvSvPrimTarif13422Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID, raschsvSvPrimTarif13422Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SUM.toString(), params, new RaschsvSvSum1TipRowMapper());
    }

    /**
     * Маппинг для СвПримТариф1.3.422
     */
    private static final class RaschsvSvPrimTarif13422RowMapper implements RowMapper<RaschsvSvPrimTarif13422> {
        @Override
        public RaschsvSvPrimTarif13422 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422 = new RaschsvSvPrimTarif13422();
            raschsvSvPrimTarif13422.setId(SqlUtils.getLong(rs, RaschsvSvPrimTarif13422.COL_ID));
            raschsvSvPrimTarif13422.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvSvPrimTarif13422.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            return raschsvSvPrimTarif13422;
        }
    }

    /**
     * Маппинг для ВыплатИт
     */
    private static final class RaschsvVyplatIt422RowMapper implements RowMapper<RaschsvVyplatIt422> {
        @Override
        public RaschsvVyplatIt422 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplatIt422 raschsvVyplatIt422 = new RaschsvVyplatIt422();
            raschsvVyplatIt422.setRaschsvSvPrimTarif1422Id(SqlUtils.getLong(rs, RaschsvVyplatIt422.COL_RASCHSV_SV_PRIM_TARIF1_422_ID));
            raschsvVyplatIt422.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvVyplatIt422.COL_RASCHSV_SV_SUM1_TIP_ID));
            return raschsvVyplatIt422;
        }
    }

    /**
     * Маппинг для СведОбуч
     */
    private static final class RaschsvSvedObuchRowMapper implements RowMapper<RaschsvSvedObuch> {
        @Override
        public RaschsvSvedObuch mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvedObuch raschsvSvedObuch = new RaschsvSvedObuch();
            raschsvSvedObuch.setId(SqlUtils.getLong(rs, RaschsvSvedObuch.COL_ID));
            raschsvSvedObuch.setRaschsvSvPrimTarif1422Id(SqlUtils.getLong(rs, RaschsvSvedObuch.COL_RASCHSV_SV_PRIM_TARIF1_422_ID));
            raschsvSvedObuch.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvSvedObuch.COL_RASCHSV_SV_SUM1_TIP_ID));
            raschsvSvedObuch.setUnikNomer(rs.getString(RaschsvSvedObuch.COL_RASCHSV_SV_SUM1_TIP_ID));
            raschsvSvedObuch.setFamilia(rs.getString(RaschsvSvedObuch.COL_FAMILIA));
            raschsvSvedObuch.setImya(rs.getString(RaschsvSvedObuch.COL_IMYA));
            raschsvSvedObuch.setOtchestvo(rs.getString(RaschsvSvedObuch.COL_OTCHESTVO));
            raschsvSvedObuch.setSpravNomer(rs.getString(RaschsvSvedObuch.COL_SPRAV_NOMER));
            raschsvSvedObuch.setSpravData(rs.getDate(RaschsvSvedObuch.COL_SPRAV_DATA));
            raschsvSvedObuch.setSpravNodeName(rs.getString(RaschsvSvedObuch.COL_SPRAV_NODE_NAME));
            return raschsvSvedObuch;
        }
    }

    /**
     * Маппинг для СвРеестрМДО
     */
    private static final class RaschsvSvReestrMdoRowMapper implements RowMapper<RaschsvSvReestrMdo> {
        @Override
        public RaschsvSvReestrMdo mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvReestrMdo raschsvSvReestrMdo = new RaschsvSvReestrMdo();
            raschsvSvReestrMdo.setId(SqlUtils.getLong(rs, RaschsvSvReestrMdo.COL_ID));
            raschsvSvReestrMdo.setRaschsvSvedObuchId(SqlUtils.getLong(rs, RaschsvSvReestrMdo.COL_RASCHSV_SVED_OBUCH_ID));
            raschsvSvReestrMdo.setNaimMdo(rs.getString(RaschsvSvReestrMdo.COL_NAIM_MDO));
            raschsvSvReestrMdo.setDataZapis(rs.getDate(RaschsvSvReestrMdo.COL_DATA_ZAPIS));
            raschsvSvReestrMdo.setNomerZapis(rs.getString(RaschsvSvReestrMdo.COL_NOMER_ZAPIS));
            return raschsvSvReestrMdo;
        }
    }
}
