package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo;
import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
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
        getJdbcTemplate().update("delete from fias_addrobj");
    }

    @Override
    public Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationId, int p_check_type) {
        Map<Long, CheckAddressResult> result = new HashMap<Long, CheckAddressResult>();
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("fias_pkg").withFunctionName("CheckExistsAddrByFias");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, new CheckExistAddressByFiasRowHandler(result)), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_check_type", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationId);
        params.addValue("p_check_type", p_check_type);
        call.execute(params);
        return result;
    }

    @Override
    public void refreshViews() {
        getJdbcTemplate().execute("call fias_pkg.RefreshViews()");
    }

    /**
     * [ID, POST_INDEX, REGION_CODE, AREA, CITY, LOCALITY, STREET, NDFL_FULL_ADDR, AREA_TYPE, AREA_FNAME, CITY_TYPE, CITY_FNAME, LOC_TYPE, LOC_FNAME, STREET_TYPE, STREET_FNAME, CHK_INDEX, CHK_REGION, CHK_AREA, CHK_CITY, CHK_LOC, CHK_STREET]
     */
    class CheckExistAddressByFiasRowHandler implements RowCallbackHandler {

        Map<Long, CheckAddressResult> result;

        public CheckExistAddressByFiasRowHandler(Map<Long, CheckAddressResult> result) {
            this.result = result;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {

            CheckAddressResult checkAddressResult = new CheckAddressResult();

            Long ndflPersonId = SqlUtils.getLong(rs, "id");

            //NdflPerson
            checkAddressResult.setNdflPerson(createNdflPersonAddress(ndflPersonId, rs));
            checkAddressResult.setPrimaryAddressPath(rs.getString("ndfl_full_addr"));

            checkAddressResult.setArea(createAddressElement(rs, "area_type", "area_fname", "chk_area", null));
            checkAddressResult.setCity(createAddressElement(rs, "city_type", "city_fname", "chk_city", null));
            checkAddressResult.setLocation(createAddressElement(rs, "loc_type", "loc_fname", "chk_loc", null));
            checkAddressResult.setStreet(createAddressElement(rs, "street_type", "street_fname", "chk_street", null));

            checkAddressResult.setPostalCodeValid(SqlUtils.getInteger(rs, "chk_index") != 0);
            checkAddressResult.setRegionValid(SqlUtils.getInteger(rs, "chk_region") != 0);

            //Long fiasId = SqlUtils.getLong(rs, "fias_id");
            this.result.put(ndflPersonId, checkAddressResult);
        }

        private NdflPerson createNdflPersonAddress(Long ndflPersonId, ResultSet rs) throws SQLException {
            NdflPerson ndflPerson = new NdflPerson(null, null, null);
            ndflPerson.setId(ndflPersonId);
            ndflPerson.setPostIndex(rs.getString("post_index"));
            ndflPerson.setRegionCode(rs.getString("region_code"));
            ndflPerson.setArea(rs.getString("area"));
            ndflPerson.setCity(rs.getString("city"));
            ndflPerson.setLocality(rs.getString("locality"));
            ndflPerson.setStreet(rs.getString("street"));
            return ndflPerson;
        }

        private AddressObject createAddressElement(ResultSet rs, String typeColumnLabel, String nameColumnLabel, String chkColumnLabel, String leafColumnLabel) throws SQLException {

            AddressObject addressObject = new AddressObject();
            addressObject.setFormalName(rs.getString(nameColumnLabel));
            addressObject.setShortName(rs.getString(typeColumnLabel));

            if (chkColumnLabel != null) {
                //Результат проверки элемента адреса 1-существует, 0- не существует
                Integer checkAddressElement = SqlUtils.getInteger(rs, chkColumnLabel);
                addressObject.setValid(checkAddressElement != 0);
            }

            if (leafColumnLabel != null) {
                Integer leafAddressElement = SqlUtils.getInteger(rs, leafColumnLabel);
                addressObject.setLeaf(leafAddressElement != 0);
            }

            return addressObject;
        }

    }


    @Override
    public Map<Long, FiasCheckInfo> checkAddressByFias(Long declarationId, int p_check_type) {
        Map<Long, FiasCheckInfo> result = new HashMap<Long, FiasCheckInfo>();
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("fias_pkg").withFunctionName("CheckAddrByFias");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, new CheckAddressByFiasRowHandler(result)), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_check_type", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationId);
        params.addValue("p_check_type", p_check_type);
        call.execute(params);
        return result;
    }


    /**
     * [ID, POST_INDEX, REGION_CODE, AREA, CITY, LOCALITY, STREET, NDFL_FULL_ADDR, AREA_TYPE, AREA_FNAME, CITY_TYPE, CITY_FNAME, LOC_TYPE, LOC_FNAME, STREET_TYPE, STREET_FNAME, FIAS_ID, FIAS_INDEX, FIAS_STREET, FIAS_STREET_TYPE, FIAS_CITY_ID, FIAS_CITY_NAME]
     */
    class CheckAddressByFiasRowHandler implements RowCallbackHandler {

        Map<Long, FiasCheckInfo> result;

        public CheckAddressByFiasRowHandler(Map<Long, FiasCheckInfo> result) {
            this.result = result;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            Long ndflPersonId = SqlUtils.getLong(rs, "id");

            FiasCheckInfo fiasCheckInfo = new FiasCheckInfo();
            fiasCheckInfo.setFiasId(SqlUtils.getLong(rs, "fias_id"));
            fiasCheckInfo.setValidIndex(SqlUtils.getInteger(rs, "chk_index") == 1);
            fiasCheckInfo.setValidRegion(SqlUtils.getInteger(rs, "chk_region") == 1);
            fiasCheckInfo.setValidArea(SqlUtils.getInteger(rs, "chk_area") == 1);
            fiasCheckInfo.setValidCity(SqlUtils.getInteger(rs, "chk_city") == 1);
            fiasCheckInfo.setValidLoc(SqlUtils.getInteger(rs, "chk_loc") == 1);
            fiasCheckInfo.setValidStreet(SqlUtils.getInteger(rs, "chk_street") == 1);

            this.result.put(ndflPersonId, fiasCheckInfo);
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
     * Сформировать строку адреса в виде пути до листового объекта
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
                    sb.append("#").append(name);
                }
            }
            result = sb.toString();
        }
        return result != null && !result.isEmpty() ? result : null;
    }

}
