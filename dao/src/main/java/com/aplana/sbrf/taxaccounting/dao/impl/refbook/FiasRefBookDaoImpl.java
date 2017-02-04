package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class FiasRefBookDaoImpl extends AbstractDao implements FiasRefBookDao {


    @Override
    public void insertRecordsBatch(String tableName, List<Map<String, Object>> records) {
        if (records != null && !records.isEmpty()) {
            String[] columns = records.get(0).keySet().toArray(new String[]{});
            StringBuilder sqlStatement = new StringBuilder();
            sqlStatement.append("insert into ");
            sqlStatement.append(tableName);
            sqlStatement.append("(").append(SqlUtils.getColumnsToString(columns, null)).append(")");
            sqlStatement.append(" VALUES ");
            sqlStatement.append("(").append(SqlUtils.getColumnsToString(columns, ":")).append(")");
            getNamedParameterJdbcTemplate().batchUpdate(sqlStatement.toString(), records.toArray(new Map[records.size()]));
        }
    }

    @Override
    public void clearAll() {
        getJdbcTemplate().update("delete from fias_room");
        getJdbcTemplate().update("delete from fias_houseint");
        getJdbcTemplate().update("delete from fias_house");
        getJdbcTemplate().update("delete from fias_addrobj");
        getJdbcTemplate().update("delete from fias_socrbase");
        getJdbcTemplate().update("delete from fias_operstat");
    }

    public static String FIND_ADDRESS_SQL = buidFindAddress();

    private static String buidFindAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append("WITH parent_to_child AS \n");
        sb.append("  (SELECT DISTINCT fa.id, fa.parentguid AS pid, fa.regioncode, fa.formalname AS fname, level AS aolevel, connect_by_isleaf AS isleaf, fa.currstatus AS status, sys_connect_by_path(fa.formalname, '\\') AS aopath \n");
        sb.append("  FROM fias_addrobj fa \n");
        sb.append("  WHERE REPLACE(lower(fa.formalname), ' ', '') = :formalName \n");
        sb.append("    START WITH fa.parentguid                  IS NULL \n");
        sb.append("  AND fa.regioncode                            = :regionCode \n");
        sb.append("    CONNECT BY prior fa.id                     = fa.parentguid \n");
        sb.append("  ) \n");
        sb.append("SELECT ptc.id, ptc.pid, ptc.regioncode, ptc.fname, ptc.aolevel, ptc.isleaf, ptc.aopath \n");
        sb.append("FROM parent_to_child ptc \n");
        sb.append("WHERE REPLACE(lower(ptc.aopath), ' ', '') = :formalPath \n");
        sb.append("AND ptc.status                            = 0");
        return sb.toString();
    }

    class AddressObjectRowMapper implements RowMapper<AddressObject> {
        @Override
        public AddressObject mapRow(ResultSet rs, int i) throws SQLException {
            AddressObject addressObject = new AddressObject();
            addressObject.setId(SqlUtils.getLong(rs, "id"));
            addressObject.setParentId(SqlUtils.getLong(rs, "pid"));
            addressObject.setRegionCode(rs.getString("regioncode"));
            addressObject.setFormalName(rs.getString("fname"));
            addressObject.setLevel(SqlUtils.getInteger(rs, "aolevel"));
            addressObject.setLeaaf(SqlUtils.getInteger(rs, "isleaf") != 0);
            addressObject.setAddressPath(rs.getString("aopath"));
            return addressObject;
        }
    }

    @Override
    public List<AddressObject> findAddress(String regionCode, String area, String city, String locality, String street) {

        if (regionCode == null || regionCode.isEmpty()) {
            throw new IllegalArgumentException("Не задан обязательный параметр код региона!");
        }
        String formalName = getLeaf(area, city, locality, street);
        String formalPath = getLeaf(area, city, locality, street);
        if (formalName != null && formalPath != null) {
            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("regionCode", regionCode);
            param.addValue("formalName", formalName.replaceAll("\\s", "").toLowerCase());
            param.addValue("formalPath", formalPath.replaceAll("\\s", "").toLowerCase());
            return getNamedParameterJdbcTemplate().query(FIND_ADDRESS_SQL, param, new AddressObjectRowMapper());
        } else {
            return Collections.emptyList();
        }
    }

    public static String FIND_REGION_BY_CODE_SQL = "SELECT fa.id, fa.regioncode, fa.formalname AS fname FROM fias_addrobj fa WHERE fa.parentguid IS NULL AND fa.regioncode = :regionCode AND fa.currstatus = 0";

    @Override
    public AddressObject findRegionByCode(String regionCode) {

        if (regionCode == null || regionCode.isEmpty()) {
            throw new IllegalArgumentException("Не задан обязательный параметр код региона!");
        }

        try {
            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("regionCode", regionCode);
            return getNamedParameterJdbcTemplate().queryForObject(FIND_REGION_BY_CODE_SQL, param, new RowMapper<AddressObject>() {
                @Override
                public AddressObject mapRow(ResultSet rs, int i) throws SQLException {
                    AddressObject addressObject = new AddressObject();
                    addressObject.setId(SqlUtils.getLong(rs, "id"));
                    addressObject.setRegionCode(rs.getString("regioncode"));
                    addressObject.setFormalName(rs.getString("fname"));
                    addressObject.setLevel(1);
                    addressObject.setLeaaf(false);
                    return addressObject;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            throw new ServiceException("В БД не найден регион с кодом " + regionCode + "!");
        }
    }

    /**
     * Получить листовой объект в иерархии адреса
     *
     * @param names наименование адресных объектов в порядке 'район -> город -> нас.пункт -> улица'
     * @return
     */

    public static String getLeaf(String... names) {
        if (names != null && names.length > 0) {
            for (int i = names.length; i > 0; i--) {
                String name = names[i - 1];
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Сформировать строку адресса в виде пути до листового объекта
     *
     * @param names наименование адресных объектов в порядке 'район -> город -> нас.пункт -> улица'
     * @return адрес в виде 'район\\город\\нас.пункт\\улица'
     */
    public static String createPath(String... names) {
        String result = null;
        if (names != null && names.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String name : names) {
                if (name != null && !name.isEmpty()) {
                    sb.append("\\\\").append(name);
                }
            }
            result = sb.toString();
        }
        return result != null && !result.isEmpty() ? result : null;
    }

}
