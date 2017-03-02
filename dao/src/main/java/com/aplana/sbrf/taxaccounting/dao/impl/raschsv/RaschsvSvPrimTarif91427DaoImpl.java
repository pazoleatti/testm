package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.RaschsvSvSum1TipRowMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif91427Dao;
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
public class RaschsvSvPrimTarif91427DaoImpl extends AbstractDao implements RaschsvSvPrimTarif91427Dao {

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    private static final String SVED_PATENT = "p";
    private static final String SUM_ALIAS = "s";
    private static final String IT_ALIAS = "i";

    // Перечень столбцов таблицы СвПримТариф9.1.427
    private static final StringBuilder TARIF_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif91427.COLUMNS, null));
    private static final StringBuilder SV_PRIM_TARIF_91427_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvPrimTarif91427.COLUMNS, ":"));

    // Перечень столбцов таблицы СведПатент
    private static final StringBuilder SVED_PATENT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedPatent.COLUMNS, null));
    private static final StringBuilder SVED_PATENT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvSvedPatent.COLUMNS, ":"));

    // Перечень столбцов таблицы ВыплатИт
    private static final StringBuilder VYPLAT_IT_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt427.COLUMNS, null));
    private static final StringBuilder VYPLAT_IT_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplatIt427.COLUMNS, ":"));

    // Перечень столбцов таблицы "СвСум1Тип"
    private static final String SUM_COLS_WITH_ALIAS = SqlUtils.getColumnsToString(RaschsvSvSum1Tip.COLUMNS, SUM_ALIAS + ".");

    private static final String SQL_INSERT_TARIF = "INSERT INTO " + RaschsvSvPrimTarif91427.TABLE_NAME +
            " (" + TARIF_COLS + ") VALUES (" + SV_PRIM_TARIF_91427_FIELDS + ")";

    private static final String SQL_INSERT_SVED_PATENT = "INSERT INTO " + RaschsvSvedPatent.TABLE_NAME +
            " (" + SVED_PATENT_COLS + ") VALUES (" + SVED_PATENT_FIELDS + ")";

    private static final String SQL_INSERT_IT = "INSERT INTO " + RaschsvVyplatIt427.TABLE_NAME +
            " (" + VYPLAT_IT_COLS + ") VALUES (" + VYPLAT_IT_FIELDS + ")";

    private static final String SQL_SELECT = "SELECT " + SqlUtils.getColumnsToString(RaschsvSvPrimTarif91427.COLUMNS, "pt.") +
            " FROM raschsv_sv_prim_tarif9_1_427 pt " +
            " INNER JOIN raschsv_obyaz_plat_sv ob ON pt.raschsv_obyaz_plat_sv_id = ob.id " +
            " WHERE ob.declaration_data_id = :declaration_data_id";

    private static final String SQL_SELECT_IT = "SELECT " + VYPLAT_IT_COLS + " FROM " + RaschsvVyplatIt427.TABLE_NAME +
            " WHERE " + RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID + " = :" + RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID;

    private static final String SQL_SELECT_SVED_PATENT = "SELECT " + SVED_PATENT_COLS + " FROM " + RaschsvSvedPatent.TABLE_NAME +
            " WHERE " + RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID + " = :" + RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID;

    private static final StringBuilder SQL_SELECT_SUM = new StringBuilder()
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvVyplatIt427.TABLE_NAME + " " + IT_ALIAS +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + IT_ALIAS + "." + RaschsvVyplatIt427.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append( " WHERE " + IT_ALIAS + "." + RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID + " = :" + RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID)
            .append(" UNION ALL ")
            .append("SELECT " + SUM_COLS_WITH_ALIAS + " FROM " + RaschsvSvSum1Tip.TABLE_NAME + " " + SUM_ALIAS)
            .append(" INNER JOIN " + RaschsvSvedPatent.TABLE_NAME + " " + SVED_PATENT +
                    " ON " + SUM_ALIAS + "." + RaschsvSvSum1Tip.COL_ID + " = " + SVED_PATENT + "." + RaschsvSvedPatent.COL_RASCHSV_SV_SUM1_TIP_ID)
            .append(" WHERE " + SVED_PATENT + "." + RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID + " = :" + RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID);

    public Long insertRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427) {
        raschsvSvPrimTarif91427.setId(generateId(RaschsvSvPrimTarif91427.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvPrimTarif91427.COL_ID, raschsvSvPrimTarif91427.getId())
                .addValue(RaschsvSvPrimTarif91427.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvSvPrimTarif91427.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_TARIF.toString(), params);

        // Сохранение "Сведения о патенте"
        List<RaschsvSvedPatent> raschsvSvedPatentList = new ArrayList<RaschsvSvedPatent>();
        for (RaschsvSvedPatent raschsvSvedPatent : raschsvSvPrimTarif91427.getRaschsvSvedPatentList()) {
            // Установка внешнего ключа
            raschsvSvedPatent.setRaschsvSvPrimTarif91427Id(raschsvSvPrimTarif91427.getId());

            // Сохранение "Сведения по суммам (тип 1)"
            Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvedPatent.getRaschsvSvSum1Tip());
            raschsvSvedPatent.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

            raschsvSvedPatentList.add(raschsvSvedPatent);
        }
        insertRaschsvSvedPatent(raschsvSvedPatentList);

        // Установка внешнего ключа
        RaschsvVyplatIt427 raschsvVyplatIt427 = raschsvSvPrimTarif91427.getRaschsvVyplatIt427();
        raschsvVyplatIt427.setRaschsvSvPrimTarif91427Id(raschsvSvPrimTarif91427.getId());

        // Сохранение "Сведения по суммам (тип 1)"
        Long raschsvSvSum1TipId = raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvVyplatIt427.getRaschsvSvSum1Tip());
        raschsvVyplatIt427.getRaschsvSvSum1Tip().setId(raschsvSvSum1TipId);

        // Сохранение "Итого выплат"
        insertRaschsvVyplatIt427(raschsvVyplatIt427);

        return raschsvSvPrimTarif91427.getId();
    }

    /**
     * Сохранение "Сведения о патенте"
     * @param raschsvSvedPatentList
     * @return
     */
    private Integer insertRaschsvSvedPatent(List<RaschsvSvedPatent> raschsvSvedPatentList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvSvedPatentList.size());
        for (RaschsvSvedPatent raschsvSvedPatent : raschsvSvedPatentList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvSvedPatent.getRaschsvSvPrimTarif91427Id())
                            .addValue(RaschsvSvedPatent.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvSvedPatent.getRaschsvSvSum1Tip().getId())
                            .addValue(RaschsvSvedPatent.COL_NOM_PATENT, raschsvSvedPatent.getNomPatent())
                            .addValue(RaschsvSvedPatent.COL_VYD_DEYAT_PATENT, raschsvSvedPatent.getVydDeyatPatent())
                            .addValue(RaschsvSvedPatent.COL_DATA_NACH_DEYST, raschsvSvedPatent.getDataNachDeyst())
                            .addValue(RaschsvSvedPatent.COL_DATA_KON_DEYST, raschsvSvedPatent.getDataKonDeyst())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SVED_PATENT,
                batchValues.toArray(new Map[raschsvSvedPatentList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Итого выплат"
     * @param raschsvVyplatIt427
     * @return
     */
    private Long insertRaschsvVyplatIt427(RaschsvVyplatIt427 raschsvVyplatIt427) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvVyplatIt427.getRaschsvSvPrimTarif91427Id())
                .addValue(RaschsvVyplatIt427.COL_RASCHSV_SV_SUM1_TIP_ID, raschsvVyplatIt427.getRaschsvSvSum1Tip().getId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_IT.toString(), params);

        return raschsvVyplatIt427.getRaschsvSvSum1Tip().getId();
    }

    public RaschsvSvPrimTarif91427 findRaschsvSvPrimTarif91427(Long declarationDataId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID, declarationDataId);
            RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427 =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT, params, new RaschsvSvPrimTarif91427RowMapper());

            // Выборка из СвСум1Тип
            List<RaschsvSvSum1Tip> raschsvSvSum1TipList = findRaschsvSvSum1Tip(raschsvSvPrimTarif91427.getId());
            Map<Long, RaschsvSvSum1Tip> mapRaschsvSvSum1Tip = new HashMap<Long, RaschsvSvSum1Tip>();
            for (RaschsvSvSum1Tip raschsvSvSum1Tip : raschsvSvSum1TipList) {
                mapRaschsvSvSum1Tip.put(raschsvSvSum1Tip.getId(), raschsvSvSum1Tip);
            }

            // Выборка из ВыплатИт
            RaschsvVyplatIt427 raschsvVyplatIt427 = findRaschsvVyplatIt427(raschsvSvPrimTarif91427.getId());
            raschsvVyplatIt427.setRaschsvSvSum1Tip(mapRaschsvSvSum1Tip.get(raschsvVyplatIt427.getRaschsvSvSum1TipId()));
            raschsvSvPrimTarif91427.setRaschsvVyplatIt427(raschsvVyplatIt427);

            // Выборка из СведПатент
            List<RaschsvSvedPatent> raschsvSvedPatentList = findRaschsvSvedPatent(raschsvSvPrimTarif91427.getId());
            for (RaschsvSvedPatent raschsvSvedPatent : raschsvSvedPatentList) {
                raschsvSvedPatent.setRaschsvSvSum1Tip(mapRaschsvSvSum1Tip.get(raschsvSvedPatent.getRaschsvSvSum1TipId()));
                raschsvSvPrimTarif91427.addRaschsvSvedPatent(raschsvSvedPatent);
            }
            return raschsvSvPrimTarif91427;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из ВыплатИт
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private RaschsvVyplatIt427 findRaschsvVyplatIt427(Long raschsvSvPrimTarif13422Id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvSvPrimTarif13422Id);
            return getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_IT, params, new RaschsvVyplatIt427RowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из СведПатент
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private List<RaschsvSvedPatent> findRaschsvSvedPatent(Long raschsvSvPrimTarif13422Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvSvPrimTarif13422Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SVED_PATENT, params, new RaschsvSvedPatentRowMapper());
    }

    /**
     * Выборка из СвСум1Тип
     * @param raschsvSvPrimTarif13422Id
     * @return
     */
    private List<RaschsvSvSum1Tip> findRaschsvSvSum1Tip(Long raschsvSvPrimTarif13422Id) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID, raschsvSvPrimTarif13422Id);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_SUM.toString(), params, new RaschsvSvSum1TipRowMapper());
    }

    /**
     * Маппинг для СвПримТариф9.1.427
     */
    private static final class RaschsvSvPrimTarif91427RowMapper implements RowMapper<RaschsvSvPrimTarif91427> {
        @Override
        public RaschsvSvPrimTarif91427 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427 = new RaschsvSvPrimTarif91427();
            raschsvSvPrimTarif91427.setId(SqlUtils.getLong(rs, RaschsvSvPrimTarif91427.COL_ID));
            raschsvSvPrimTarif91427.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvSvPrimTarif91427.COL_RASCHSV_OBYAZ_PLAT_SV_ID));
            return raschsvSvPrimTarif91427;
        }
    }

    /**
     * Маппинг для ВыплатИт
     */
    private static final class RaschsvVyplatIt427RowMapper implements RowMapper<RaschsvVyplatIt427> {
        @Override
        public RaschsvVyplatIt427 mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplatIt427 raschsvVyplatIt427 = new RaschsvVyplatIt427();
            raschsvVyplatIt427.setRaschsvSvPrimTarif91427Id(SqlUtils.getLong(rs, RaschsvVyplatIt427.COL_RASCHSV_SV_PRIM_TARIF9_427_ID));
            raschsvVyplatIt427.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvVyplatIt427.COL_RASCHSV_SV_SUM1_TIP_ID));
            return raschsvVyplatIt427;
        }
    }

    /**
     * Маппинг для СведПатент
     */
    private static final class RaschsvSvedPatentRowMapper implements RowMapper<RaschsvSvedPatent> {
        @Override
        public RaschsvSvedPatent mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvSvedPatent raschsvSvedPatent = new RaschsvSvedPatent();
            raschsvSvedPatent.setRaschsvSvPrimTarif91427Id(SqlUtils.getLong(rs, RaschsvSvedPatent.COL_RASCHSV_SV_PRIM_TARIF9_427_ID));
            raschsvSvedPatent.setRaschsvSvSum1TipId(SqlUtils.getLong(rs, RaschsvSvedPatent.COL_RASCHSV_SV_SUM1_TIP_ID));
            raschsvSvedPatent.setNomPatent(rs.getString(RaschsvSvedPatent.COL_NOM_PATENT));
            raschsvSvedPatent.setVydDeyatPatent(rs.getString(RaschsvSvedPatent.COL_VYD_DEYAT_PATENT));
            raschsvSvedPatent.setDataNachDeyst(rs.getDate(RaschsvSvedPatent.COL_DATA_NACH_DEYST));
            raschsvSvedPatent.setDataKonDeyst(rs.getDate(RaschsvSvedPatent.COL_DATA_KON_DEYST));
            return raschsvSvedPatent;
        }
    }
}
