package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Реализация ДАО для работа с параметрами приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:41
 */
@Repository
public class ConfigurationDaoImpl extends AbstractDao implements ConfigurationDao {

    class ConfigurationRowCallbackHandler implements RowCallbackHandler {
        final ConfigurationParamModel model;
        ConfigurationParamGroup byGroup;

        public ConfigurationRowCallbackHandler(ConfigurationParamModel model) {
            this.model = model;
        }

        public ConfigurationRowCallbackHandler(ConfigurationParamModel model, ConfigurationParamGroup byGroup) {
            this.model = model;
            this.byGroup = byGroup;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            try {
                ConfigurationParam param = ConfigurationParam.valueOf(rs.getString("code"));
                if (byGroup == null || param.getGroup() != null && param.getGroup().equals(byGroup)) {
                    model.setFullStringValue(param,
                            rs.getInt("department_id"),
                            rs.getString("value"));
                }
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в ConfigurationParam, то он просто пропускается (не виден на клиенте)
            }
        }
    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link Configuration}
     * для таблицы CONFIGURATION
     */
    private RowMapper<Configuration> configurationRowMapper = new RowMapper<Configuration>() {
        @Override
        public Configuration mapRow(ResultSet rs, int index) throws SQLException {
            Configuration param = new Configuration();
            param.setCode(rs.getString("code"));
            param.setDescription(param.getCode());
            try {
                ConfigurationParam paramEnum = ConfigurationParam.valueOf(param.getCode());
                param.setDescription(paramEnum.getCaption());
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в ConfigurationParam, то пропускаем
            }
            param.setDepartmentId(rs.getInt("department_id"));
            param.setValue(rs.getString("value"));
            return param;
        }
    };

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link Configuration}
     * для таблицы CONFIGURATION_EMAIL
     */
    private RowMapper<Configuration> emailConfigurationRowMapper = new RowMapper<Configuration>() {
        @Override
        public Configuration mapRow(ResultSet rs, int index) throws SQLException {
            Configuration param = new Configuration();
            param.setId(rs.getInt("id"));
            param.setCode(rs.getString("name"));
            param.setDescription(rs.getString("description"));
            param.setValue(rs.getString("value"));
            return param;
        }
    };

    @Override
    public Configuration fetchByEnum(ConfigurationParam param) {
        try {
            return getJdbcTemplate().queryForObject("SELECT code, department_id, value FROM configuration WHERE code = ?",
                    new Object[]{param.name()}, configurationRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public ConfigurationParamModel fetchAllAsModel() {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        getJdbcTemplate().query("SELECT code, value, department_id FROM configuration", new ConfigurationRowCallbackHandler(model));
        return model;
    }

    @Override
    public PagingResult<Configuration> fetchAllByGroupAndPaging(ConfigurationParamGroup group, PagingParams pagingParams) {
        String where = " where " + SqlUtils.transformToSqlInStatementForStringFromObject("code", ConfigurationParam.getParamsByGroup(group));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        List<Configuration> asyncTaskTypeDataList = getNamedParameterJdbcTemplate().query(
                "select * from (" +
                        "   select rownum rn, ordered.* from (select code, department_id, value from configuration" + where + ") ordered " +
                        ") numbered " +
                        "where rn between :start and :end order by code",
                params, configurationRowMapper
        );
        int totalCount = getJdbcTemplate().queryForObject("select count(*) from (select code, value from configuration" + where + ")", Integer.class);
        return new PagingResult<>(asyncTaskTypeDataList, totalCount);
    }

    @Override
    public ConfigurationParamModel fetchAllAsModelByGroup(final ConfigurationParamGroup group) {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        getJdbcTemplate().query("SELECT code, value, department_id FROM configuration", new ConfigurationRowCallbackHandler(model, group));
        return model;
    }

    @Override
    public ConfigurationParamModel fetchAllByDepartment(Integer departmentId) {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        getJdbcTemplate().query(
                "SELECT code, department_id, value FROM configuration WHERE department_id = ?",
                new Object[]{departmentId},
                new ConfigurationRowCallbackHandler(model));
        return model;
    }

    @Override
    public void save(ConfigurationParamModel model) {
        ConfigurationParamModel oldModel = fetchAllAsModel();
        List<Object[]> insertParams = new LinkedList<>();
        List<Object[]> updateParams = new LinkedList<>();
        List<Object[]> deleteParams = new LinkedList<>();

        for (ConfigurationParam configurationParam : model.keySet()) {
            Map<Integer, List<String>> map = model.get(configurationParam);
            for (int departmentId : map.keySet()) {
                Object[] entity = new Object[]{model.getFullStringValue(configurationParam, departmentId),
                        configurationParam.name(), departmentId};
                if (!oldModel.containsKey(configurationParam)
                        || (oldModel.get(configurationParam) != null && !oldModel.get(configurationParam).containsKey(departmentId))) {
                    insertParams.add(entity);
                } else {
                    updateParams.add(entity);
                }
            }
        }

        for (Map.Entry<ConfigurationParam, Map<Integer, List<String>>> entry : oldModel.entrySet()) {
            if (entry.getValue() != null) {
                for (Integer departmentId : entry.getValue().keySet()) {
                    if (!model.containsKey(entry.getKey(), departmentId)) {
                        deleteParams.add(new Object[]{entry.getKey().name(), departmentId});
                    }
                }
            }
        }

        if (!insertParams.isEmpty()) {
            getJdbcTemplate().batchUpdate("INSERT INTO configuration (value, code, department_id) VALUES (?, ?, ?)", insertParams);
        }
        if (!updateParams.isEmpty()) {
            getJdbcTemplate().batchUpdate("UPDATE configuration SET VALUE = ? WHERE code = ? AND department_id = ?", updateParams);
        }
        if (!deleteParams.isEmpty()) {
            getJdbcTemplate().batchUpdate("DELETE FROM configuration WHERE code = ? AND department_id = ?", deleteParams);
        }
    }

    @Override
    public void update(Configuration config) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", ConfigurationParam.getNameValueAsDB(config.getCode()));
        params.addValue("departmentId", config.getDepartmentId());
        params.addValue("value", config.getValue());
        getNamedParameterJdbcTemplate().update(
                "update configuration set value = :value " +
                        "where code = :code " +
                        (config.getDepartmentId() != null ? " and department_id = :departmentId" : ""),
                params);
    }

    @Override
    public void update(List<Configuration> configurations) {
        //Общие параметры просто обновляются, так как не предусмотренно удаление общих параметров
        for (Configuration config : configurations) {
            update(config);
        }
    }

    @Override
    public void createCommonParam(Configuration configuration) {
        getJdbcTemplate().update("INSERT INTO CONFIGURATION VALUES (?, 0, ?) ",
                new Object[]{configuration.getCode(), configuration.getValue()},
                new int[]{Types.VARCHAR, Types.VARCHAR});
    }

    @Override
    public void removeCommonParam(List<ConfigurationParam> params) {
        String where = " where " + SqlUtils.transformToSqlInStatementForStringFromObject("code", params);
        getJdbcTemplate().update("DELETE FROM CONFIGURATION" + where);
    }

    @Override
    public void updateAsyncParam(AsyncTaskTypeData asyncParam) {
        getJdbcTemplate().update("UPDATE ASYNC_TASK_TYPE SET TASK_LIMIT=?, SHORT_QUEUE_LIMIT=? WHERE ID=?",
                new Object[]{
                        (asyncParam.getTaskLimit()),
                        (asyncParam.getShortQueueLimit()),
                        asyncParam.getId()
                },
                new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC}
        );
    }

    @Override
    public PagingResult<Configuration> fetchEmailParams(PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        List<Configuration> emailParamList = getNamedParameterJdbcTemplate().query(
                "select * from (" +
                        "   select rownum rn, ordered.* from (select id, name, value, description from configuration_email) ordered " +
                        ") numbered " +
                        "where rn between :start and :end order by name",
                params, emailConfigurationRowMapper
        );
        int totalCount = getJdbcTemplate().queryForObject("select count(*) from (select id, value from configuration_email)", Integer.class);
        return new PagingResult<>(emailParamList, totalCount);
    }

    @Override
    public void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId) {
        List<Object[]> updateParams = new LinkedList<>();

        for (Map.Entry<ConfigurationParam, String> entry : configurationParamMap.entrySet()) {
            Object[] entity = new Object[]{
                    entry.getKey().name(),
                    entry.getValue(),
                    departmentId
            };

            updateParams.add(entity);
        }

        if (!updateParams.isEmpty()) {
            getJdbcTemplate().batchUpdate(
                    "MERGE INTO configuration dest " +
                            "USING (SELECT ? code, ? value, ? department_id FROM dual) src " +
                            "ON (dest.code = src.code) " +
                            "WHEN MATCHED THEN UPDATE SET dest.value = src.value " +
                            "WHEN NOT MATCHED THEN INSERT(code, value, department_id) VALUES(src.code, src.value, src.department_id)",
                    updateParams
            );
        }
    }
}
