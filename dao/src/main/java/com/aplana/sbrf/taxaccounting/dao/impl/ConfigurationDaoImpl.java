package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Реализация ДАО для работа с параметрами приложения
 */
@Repository
public class ConfigurationDaoImpl extends AbstractDao implements ConfigurationDao {

    private static final List<String> SMTP_CONNECTION_PARAMS = Arrays.asList("mail.smtp.user", "mail.smtp.password", "mail.smtp.host", "mail.smtp.port");


    class ConfigurationRowCallbackHandler implements RowCallbackHandler {
        final ConfigurationParamModel model;
        ConfigurationParamGroup byGroup;

        ConfigurationRowCallbackHandler(ConfigurationParamModel model) {
            this.model = model;
        }

        ConfigurationRowCallbackHandler(ConfigurationParamModel model, ConfigurationParamGroup byGroup) {
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
    public List<Configuration> fetchAllByEnums(Collection<ConfigurationParam> params) {
        try {
            MapSqlParameterSource sqlParams = new MapSqlParameterSource();
            Collection<String> codes = Collections2.transform(params, new Function<ConfigurationParam, String>() {
                @Override
                public String apply(ConfigurationParam input) {
                    return input.name();
                }
            });
            sqlParams.addValue("codes", codes);
            return getNamedParameterJdbcTemplate().query("SELECT code, department_id, value FROM configuration WHERE code IN (:codes)",
                    sqlParams, configurationRowMapper);
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
    public void updateEmailParam(Configuration emailParam) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", ConfigurationParam.getNameValueAsDB(emailParam.getCode()));
        params.addValue("value", emailParam.getValue());
        getNamedParameterJdbcTemplate().update(
                "UPDATE configuration_email SET value = :value WHERE name = :code",
                params);
    }

    @Override
    public PagingResult<Configuration> fetchEmailParams(PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        List<Configuration> emailParamList = getNamedParameterJdbcTemplate().query(
                "SELECT * FROM (" +
                        "   SELECT rownum rn, ordered.* FROM (SELECT id, name, value, description FROM configuration_email) ordered " +
                        ") numbered " +
                        "WHERE rn BETWEEN :start AND :end ORDER BY id",
                params, emailConfigurationRowMapper
        );
        int totalCount = getJdbcTemplate().queryForObject("SELECT count(*) FROM (SELECT id, value FROM configuration_email)", Integer.class);
        return new PagingResult<>(emailParamList, totalCount);
    }

    @Override
    public List<Configuration> fetchAuthEmailParams() {
        return getJdbcTemplate().query("select name, value from configuration_email where " + SqlUtils.transformToSqlInStatementForString("name", SMTP_CONNECTION_PARAMS), new RowMapper<Configuration>() {
            @Override
            public Configuration mapRow(ResultSet rs, int rowNum) throws SQLException {
                Configuration configuration = new Configuration();
                configuration.setCode(rs.getString("name"));
                configuration.setValue(rs.getString("value"));
                return configuration;
            }
        });
    }
}
