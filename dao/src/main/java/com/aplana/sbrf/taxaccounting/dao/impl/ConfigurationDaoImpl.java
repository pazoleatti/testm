package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.CommonConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QConfiguration.configuration;


/**
 * Реализация ДАО для работа с параметрами приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:41
 */
@Repository
@Transactional
public class ConfigurationDaoImpl extends AbstractDao implements ConfigurationDao {
    private static final Log LOG = LogFactory.getLog(ConfigurationDaoImpl.class);

    class ConfigurationRowCallbackHandler implements RowCallbackHandler {
        final ConfigurationParamModel model;

        public ConfigurationRowCallbackHandler(ConfigurationParamModel model) {
            this.model = model;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            try {
                model.setFullStringValue(ConfigurationParam.valueOf(rs.getString("code")),
                        rs.getInt("department_id"),
                        rs.getString("value"));
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в ConfiguratioжnParam, то он просто пропускается (не виден на клиенте)
            }
        }
    }

    //метод преобразующий конфигурационные параметры в ConfigurationParamModel используемый в gwt
    private void configInToConfigurationParamModel(List<Configuration> listConfig, ConfigurationParamModel configurationParamModel) {
        for (Configuration config : listConfig) {
            try {
                configurationParamModel.setFullStringValue(ConfigurationParam.valueOf(config.getCode().toString()),
                        config.getDepartmentId().intValue(), config.getValue());
            } catch (Exception e) {
                //пропускается
            }
        }
    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link CommonConfigurationParam}
     */
    private RowMapper<CommonConfigurationParam> commonConfigurationParamMapper = new RowMapper<CommonConfigurationParam>() {
        @Override
        public CommonConfigurationParam mapRow(ResultSet rs, int index) throws SQLException {
            CommonConfigurationParam param = new CommonConfigurationParam();
            param.setName(ConfigurationParam.valueOf(rs.getString("code")).getCaption());
            param.setValue(rs.getString("value"));
            return param;
        }
    };

    private RowMapper<Configuration> configurationRowMapper = new RowMapper<Configuration>() {
        @Override
        public Configuration mapRow(ResultSet rs, int index) throws SQLException {
            Configuration param = new Configuration();
            param.setCode(rs.getString("code"));
            param.setDepartmentId(rs.getInt("department_id"));
            param.setValue(rs.getString("value"));
            return param;
        }
    };

    @Override
    public List<Configuration> getAllConfiguration() {
        List<Configuration> configurations = getJdbcTemplate().query(
                "select code, department_id, value from configuration", configurationRowMapper);
        for (Configuration config : configurations) {
            try {
                config.setCode(ConfigurationParam.valueOf(config.getCode()).getCaption());
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в result, то он просто пропускается (не виден на клиенте)
            }
        }
        return configurations;
    }

    @Override
    public ConfigurationParamModel getAll() {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        getJdbcTemplate().query("select code, value, department_id from configuration", new ConfigurationRowCallbackHandler(model));
        return model;
    }

    @Override
    public ConfigurationParamModel getConfigByGroup(final ConfigurationParamGroup group) {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        getJdbcTemplate().query("select code, value, department_id from configuration", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                try {
                    ConfigurationParam configurationParam = ConfigurationParam.valueOf(rs.getString("code"));
                    if (configurationParam.getGroup().equals(group)) {
                        model.setFullStringValue(configurationParam, rs.getInt("department_id"), rs.getString("value"));
                    }
                } catch (IllegalArgumentException e) {
                    // Если параметр не найден в ConfigurationParam, то он просто пропускается (не виден на клиенте)
                }
            }
        });
        return model;
    }

    @Override
    public List<Configuration> getListConfigByGroup(final ConfigurationParamGroup group) {
        List<Configuration> resultList = new LinkedList<>();
        List<Configuration> listConfig = getJdbcTemplate().query(
                "select code, department_id, value from configuration", configurationRowMapper);
        for (Configuration config : listConfig) {
            try {
                ConfigurationParam configurationParam = ConfigurationParam.valueOf(config.getCode());
                if (configurationParam.getGroup().equals(group)) {
                    ConfigurationParam.valueOf(config.getCode());
                    config.setCode(ConfigurationParam.valueOf(config.getCode()).getCaption());
                    resultList.add(config);
                }
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в result, то он просто пропускается (не виден на клиенте)
            }
        }
        return resultList;
    }

    @Override
    public ConfigurationParamModel getByDepartment(Integer departmentId) {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        List<Configuration> listConfig = getJdbcTemplate().query(
                "select code, department_id, value from configuration where department_id = ?",
                new Object[]{departmentId}, configurationRowMapper);
        configInToConfigurationParamModel(listConfig, model);
        return model;
    }

    @Override
    public void save(ConfigurationParamModel model) {
        ConfigurationParamModel oldModel = getAll();
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

    public void create(Configuration config) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", ConfigurationParam.getNameValueAsDB(config.getCode()));
        params.addValue("departmentId", config.getDepartmentId());
        params.addValue("value", config.getValue());
        getNamedParameterJdbcTemplate().update(
                "insert into configuration(code, department_id, value) " +
                        "values(:code, :departmentId, :value)",
                params);
    }

    public void update(Configuration config) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", ConfigurationParam.getNameValueAsDB(config.getCode()));
        params.addValue("departmentId", config.getDepartmentId());
        params.addValue("value", config.getValue());
        getNamedParameterJdbcTemplate().update(
                "update configuration set value = :value " +
                        "where code = :code and department_id = :departmentId",
                params);
    }

    @Override
    public void setCommonParamsDefault(List<Configuration> listDefaultConfig) {
        //Общие параметры просто обновляются, так как не предусмотренно удаление общих параметров
        for (Configuration config : listDefaultConfig) {
            update(config);
        }
    }

    @Override
    public PagingResult<CommonConfigurationParam> fetchAllCommonParam(PagingParams pagingParams) {
        String where = " where " + SqlUtils.transformToSqlInStatementForStringFromObject("code", ConfigurationParam.getParamsByGroup(ConfigurationParamGroup.COMMON));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        List<CommonConfigurationParam> asyncTaskTypeDataList = getNamedParameterJdbcTemplate().query(
                "select * from (" +
                        "   select rownum rn, ordered.* from (select code, value from configuration" + where + ") ordered " +
                        ") numbered " +
                        "where rn between :start and :end order by code",
                params, commonConfigurationParamMapper
        );
        int totalCount = getJdbcTemplate().queryForObject("select count(*) from (select code, value from configuration" + where + ")", Integer.class);
        return new PagingResult<>(asyncTaskTypeDataList, totalCount);
    }

    @Override
    public void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId) {
        List<Object[]> updateParams = new LinkedList<Object[]>();

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
