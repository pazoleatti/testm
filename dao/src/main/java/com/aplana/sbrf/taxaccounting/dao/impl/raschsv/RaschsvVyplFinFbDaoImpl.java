package com.aplana.sbrf.taxaccounting.dao.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvVyplFinFbDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplPrichina;
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
public class RaschsvVyplFinFbDaoImpl extends AbstractDao implements RaschsvVyplFinFbDao {

    private static final String VYPL_FIN_FB_ALIAS = "f";
    private static final String VYPL_PRICHINA_ALIAS = "p";
    private static final String RASH_VYPL_ALIAS = "v";

    // Перечень столбцов таблицы ВыплФинФБ
    private static final StringBuilder VYPL_FIN_FB_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplFinFb.COLUMNS, null));
    private static final StringBuilder VYPL_FIN_FB_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplFinFb.COLUMNS, ":"));

    // Перечень столбцов таблицы "Причина ВыплФинФБ"
    private static final StringBuilder VYPL_PRICHINA_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplPrichina.COLUMNS, null));
    private static final StringBuilder VYPL_PRICHINA_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvVyplPrichina.COLUMNS, ":"));

    // Перечень столбцов таблицы "Информация по конкретной ВыплФинФБ"
    private static final StringBuilder RASH_VYPL_COLS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashVypl.COLUMNS, null));
    private static final StringBuilder RASH_VYPL_COLS_WITH_ALIAS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashVypl.COLUMNS, RASH_VYPL_ALIAS + "."));
    private static final StringBuilder RASH_VYPL_FIELDS = new StringBuilder(SqlUtils.getColumnsToString(RaschsvRashVypl.COLUMNS, ":"));

    private static final String SQL_INSERT_VYPL_FIN_FB = "INSERT INTO " + RaschsvVyplFinFb.TABLE_NAME +
            " (" + VYPL_FIN_FB_COLS + ") VALUES (" + VYPL_FIN_FB_FIELDS + ")";

    private static final String SQL_INSERT_VYPL_PRICHINA = "INSERT INTO " + RaschsvVyplPrichina.TABLE_NAME +
            " (" + VYPL_PRICHINA_COLS + ") VALUES (" + VYPL_PRICHINA_FIELDS + ")";

    private static final String SQL_INSERT_RASH_VYPL = "INSERT INTO " + RaschsvRashVypl.TABLE_NAME +
            " (" + RASH_VYPL_COLS + ") VALUES (" + RASH_VYPL_FIELDS + ")";

    private static final String SQL_SELECT_VYPL_FIN_FB = "SELECT " + SqlUtils.getColumnsToString(RaschsvVyplFinFb.COLUMNS, "vf.") +
            " FROM raschsv_vypl_fin_fb vf " +
            " INNER JOIN raschsv_obyaz_plat_sv ob ON vf.raschsv_obyaz_plat_sv_id = ob.id " +
            " WHERE ob.declaration_data_id = :declaration_data_id";

    private static final String SQL_SELECT_VYPL_PRICHINA = "SELECT " + VYPL_PRICHINA_COLS + " FROM " + RaschsvVyplPrichina.TABLE_NAME +
            " WHERE " + RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID + " = :" + RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID;

    private static final StringBuilder SQL_SELECT_RASH_VYPL = new StringBuilder()
            .append("SELECT " + RASH_VYPL_COLS_WITH_ALIAS + " FROM " + RaschsvRashVypl.TABLE_NAME + " " + RASH_VYPL_ALIAS)
            .append(" INNER JOIN " + RaschsvVyplPrichina.TABLE_NAME + " " + VYPL_PRICHINA_ALIAS +
                    " ON " + RASH_VYPL_ALIAS + "." + RaschsvRashVypl.COL_RASCHSV_VYPL_PRICHINA_ID + " = " + VYPL_PRICHINA_ALIAS + "." + RaschsvVyplPrichina.COL_ID)
            .append(" WHERE " + VYPL_PRICHINA_ALIAS + "." + RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID + " = :" + RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID);

    @Override
    public Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb) {
        raschsvVyplFinFb.setId(generateId(RaschsvVyplFinFb.SEQ, Long.class));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplFinFb.COL_ID, raschsvVyplFinFb.getId())
                .addValue(RaschsvVyplFinFb.COL_RASCHSV_OBYAZ_PLAT_SV_ID, raschsvVyplFinFb.getRaschsvObyazPlatSvId());
        getNamedParameterJdbcTemplate().update(SQL_INSERT_VYPL_FIN_FB.toString(), params);

        List<RaschsvVyplPrichina> raschsvVyplPrichinaList = new ArrayList<RaschsvVyplPrichina>();
        List<RaschsvRashVypl> raschsvRashVyplList = new ArrayList<RaschsvRashVypl>();

        for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplFinFb.getRaschsvVyplPrichinaList()) {
            // Установка внешнего ключа для "Причина ВыплФинФБ"
            raschsvVyplPrichina.setRaschsvVyplFinFbId(raschsvVyplFinFb.getId());
            raschsvVyplPrichina.setId(generateId(RaschsvVyplPrichina.SEQ, Long.class));
            raschsvVyplPrichinaList.add(raschsvVyplPrichina);

            // Установка внешнего ключа для "Информация по конкретной ВыплФинФБ"
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.getRaschsvRashVyplList()) {
                raschsvRashVypl.setRaschsvVyplPrichinaId(raschsvVyplPrichina.getId());
                raschsvRashVypl.setId(generateId(RaschsvRashVypl.SEQ, Long.class));
                raschsvRashVyplList.add(raschsvRashVypl);
            }
        }

        // Сохранение "Причина ВыплФинФБ"
        insertRaschsvVyplPrichina(raschsvVyplPrichinaList);

        // Сохранение "Информация по конкретной ВыплФинФБ"
        insertRaschsvRashVypl(raschsvRashVyplList);

        return raschsvVyplFinFb.getId();
    }

    /**
     * Сохранение "Причина ВыплФинФБ"
     * @param raschsvVyplPrichinaList
     * @return
     */
    private Integer insertRaschsvVyplPrichina(List<RaschsvVyplPrichina> raschsvVyplPrichinaList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvVyplPrichinaList.size());
        for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplPrichinaList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvVyplPrichina.COL_ID, raschsvVyplPrichina.getId())
                            .addValue(RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID, raschsvVyplPrichina.getRaschsvVyplFinFbId())
                            .addValue(RaschsvVyplPrichina.COL_NODE_NAME, raschsvVyplPrichina.getNodeName())
                            .addValue(RaschsvVyplPrichina.COL_SV_VNF_UHOD_INV, raschsvVyplPrichina.getSvVnfUhodInv())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_VYPL_PRICHINA,
                batchValues.toArray(new Map[raschsvVyplPrichinaList.size()]));

        return res.length;
    }

    /**
     * Сохранение "Информация по конкретной ВыплФинФБ"
     * @param raschsvRashVyplList
     * @return
     */
    private Integer insertRaschsvRashVypl(List<RaschsvRashVypl> raschsvRashVyplList) {
        List<Map<String, Object>> batchValues = new ArrayList<Map<String, Object>>(raschsvRashVyplList.size());
        for (RaschsvRashVypl raschsvRashVypl : raschsvRashVyplList) {
            batchValues.add(
                    new MapSqlParameterSource(RaschsvRashVypl.COL_ID, raschsvRashVypl.getId())
                            .addValue(RaschsvRashVypl.COL_RASCHSV_VYPL_PRICHINA_ID, raschsvRashVypl.getRaschsvVyplPrichinaId())
                            .addValue(RaschsvRashVypl.COL_NODE_NAME, raschsvRashVypl.getNodeName())
                            .addValue(RaschsvRashVypl.COL_CHISL_POLUCH, raschsvRashVypl.getChislPoluch())
                            .addValue(RaschsvRashVypl.COL_KOL_VYPL, raschsvRashVypl.getKolVypl())
                            .addValue(RaschsvRashVypl.COL_RASHOD, raschsvRashVypl.getRashod())
                            .getValues());
        }
        int [] res = getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_RASH_VYPL,
                batchValues.toArray(new Map[raschsvRashVyplList.size()]));

        return res.length;
    }

    @Override
    public RaschsvVyplFinFb findRaschsvVyplFinFb(Long declarationDataId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(RaschsvObyazPlatSv.COL_DECLARATION_DATA_ID, declarationDataId);
            // Выборка из РасчСВ_ОСС.ВНМ
            RaschsvVyplFinFb raschsvVyplFinFb =
                    getNamedParameterJdbcTemplate().queryForObject(SQL_SELECT_VYPL_FIN_FB, params, new RaschsvVyplFinFbRowMapper());

            // Выборка из "Причина ВыплФинФБ"
            raschsvVyplFinFb.setRaschsvVyplPrichinaList(findRaschsvVyplPrichina(raschsvVyplFinFb.getId()));
            Map<Long, RaschsvVyplPrichina> mapRaschsvVyplPrichina = new HashMap<Long, RaschsvVyplPrichina>();
            for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplFinFb.getRaschsvVyplPrichinaList()) {
                mapRaschsvVyplPrichina.put(raschsvVyplPrichina.getId(), raschsvVyplPrichina);
            }

            // Выборка из "Информация по конкретной ВыплФинФБ"
            List<RaschsvRashVypl> raschsvRashVyplList = findRaschsvRashVypl(raschsvVyplFinFb.getId());
            for (RaschsvRashVypl raschsvRashVypl : raschsvRashVyplList) {
                RaschsvVyplPrichina raschsvVyplPrichina = mapRaschsvVyplPrichina.get(raschsvRashVypl.getRaschsvVyplPrichinaId());
                raschsvVyplPrichina.addRaschsvRashVypl(raschsvRashVypl);
            }
            return raschsvVyplFinFb;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Выборка из "Причина ВыплФинФБ"
     * @param raschsvVyplFinFbId
     * @return
     */
    private List<RaschsvVyplPrichina> findRaschsvVyplPrichina(Long raschsvVyplFinFbId){
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID, raschsvVyplFinFbId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_VYPL_PRICHINA.toString(), params, new RaschsvVyplPrichinaRowMapper());
    }

    /**
     * Выборка из "Информация по конкретной ВыплФинФБ"
     * @param raschsvVyplFinFbId
     * @return
     */
    private List<RaschsvRashVypl> findRaschsvRashVypl(Long raschsvVyplFinFbId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID, raschsvVyplFinFbId);
        return getNamedParameterJdbcTemplate().query(SQL_SELECT_RASH_VYPL.toString(), params, new RaschsvRashVyplRowMapper());
    }

    /**
     * Маппинг для ВыплФинФБ
     */
    private static final class RaschsvVyplFinFbRowMapper implements RowMapper<RaschsvVyplFinFb> {
        @Override
        public RaschsvVyplFinFb mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplFinFb raschsvVyplFinFb = new RaschsvVyplFinFb();
            raschsvVyplFinFb.setId(SqlUtils.getLong(rs, RaschsvVyplFinFb.COL_ID));
            raschsvVyplFinFb.setRaschsvObyazPlatSvId(SqlUtils.getLong(rs, RaschsvVyplFinFb.COL_RASCHSV_OBYAZ_PLAT_SV_ID));

            return raschsvVyplFinFb;
        }
    }

    /**
     * Маппинг для "Причина ВыплФинФБ"
     */
    private static final class RaschsvVyplPrichinaRowMapper implements RowMapper<RaschsvVyplPrichina> {
        @Override
        public RaschsvVyplPrichina mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvVyplPrichina raschsvVyplPrichina = new RaschsvVyplPrichina();
            raschsvVyplPrichina.setId(SqlUtils.getLong(rs, RaschsvVyplPrichina.COL_ID));
            raschsvVyplPrichina.setRaschsvVyplFinFbId(SqlUtils.getLong(rs, RaschsvVyplPrichina.COL_RASCHSV_VYPL_FIN_FB_ID));
            raschsvVyplPrichina.setNodeName(rs.getString(RaschsvVyplPrichina.COL_NODE_NAME));
            raschsvVyplPrichina.setSvVnfUhodInv(rs.getBigDecimal(RaschsvVyplPrichina.COL_SV_VNF_UHOD_INV));

            return raschsvVyplPrichina;
        }
    }

    /**
     * Маппинг для "Информация по конкретной ВыплФинФБ"
     */
    private static final class RaschsvRashVyplRowMapper implements RowMapper<RaschsvRashVypl> {
        @Override
        public RaschsvRashVypl mapRow(ResultSet rs, int index) throws SQLException {
            RaschsvRashVypl raschsvRashVypl = new RaschsvRashVypl();
            raschsvRashVypl.setId(SqlUtils.getLong(rs, RaschsvRashVypl.COL_ID));
            raschsvRashVypl.setRaschsvVyplPrichinaId(SqlUtils.getLong(rs, RaschsvRashVypl.COL_RASCHSV_VYPL_PRICHINA_ID));
            raschsvRashVypl.setNodeName(rs.getString(RaschsvRashVypl.COL_NODE_NAME));
            raschsvRashVypl.setChislPoluch(rs.getInt(RaschsvRashVypl.COL_CHISL_POLUCH));
            raschsvRashVypl.setKolVypl(rs.getInt(RaschsvRashVypl.COL_KOL_VYPL));
            raschsvRashVypl.setRashod(rs.getBigDecimal(RaschsvRashVypl.COL_RASHOD));

            return raschsvRashVypl;
        }
    }
}
