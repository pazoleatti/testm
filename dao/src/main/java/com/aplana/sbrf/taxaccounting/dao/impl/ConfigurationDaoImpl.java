package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QConfiguration.configuration;
import static com.querydsl.core.types.Projections.bean;


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
    private static final String GET_ALL_ERROR = "Ошибка получения конфигурационных параметров!";

    final private SQLQueryFactory sqlQueryFactory;

    public ConfigurationDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<Configuration> configurationBean = bean(Configuration.class, configuration.all());

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


    @Override
    public List<Configuration> getAllConfiguration() {
        return sqlQueryFactory.select(configuration.code).from(configuration)
                .transform(GroupBy.groupBy(configuration.code).list(configurationBean));
    }


    @Override
    public ConfigurationParamModel getAll() {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        List<Configuration> listConfig = getAllConfiguration();
        configInToConfigurationParamModel(listConfig, model);
        return model;
    }

    @Override
    public ConfigurationParamModel getConfigByGroup(final ConfigurationParamGroup group) {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        List<Configuration> listConfig = sqlQueryFactory.select(configuration.code).from(configuration)
                .transform(GroupBy.groupBy(configuration.code).list(configurationBean));
        for (Configuration config : listConfig) {
            try {
                ConfigurationParam configurationParam = ConfigurationParam.valueOf(config.getCode());
                if (configurationParam.getGroup().equals(group)) {
                    model.setFullStringValue(configurationParam, config.getDepartmentId().intValue(), config.getValue());
                }
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в ConfigurationParam, то он просто пропускается (не виден на клиенте)
            }
        }
        return model;
    }

    @Override
    public List<Configuration> getListConfigByGroup(final ConfigurationParamGroup group) {
        List<Configuration> resultList = new LinkedList<>();
        List<Configuration> listConfig = sqlQueryFactory.select(configuration.code).from(configuration)
                .transform(GroupBy.groupBy(configuration.code).list(configurationBean));
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
        List<Configuration> listConfig = sqlQueryFactory.select(configuration.code).from(configuration)
                .where(configuration.departmentId.eq(departmentId))
                .transform(GroupBy.groupBy(configuration.code).list(configurationBean));
        configInToConfigurationParamModel(listConfig, model);
        return model;
    }

    @Override
    public void save(ConfigurationParamModel model) {
        ConfigurationParamModel oldModel = getAll();
        List<Configuration> insertParams = new LinkedList<>();
        List<Configuration> updateParams = new LinkedList<>();
        List<Object[]> deleteParams = new LinkedList<>();

        for (ConfigurationParam configurationParam : model.keySet()) {
            Map<Integer, List<String>> map = model.get(configurationParam);
            for (int departmentId : map.keySet()) {
                Configuration entity = new Configuration(configurationParam.name(), departmentId, model.getFullStringValue(configurationParam, departmentId));
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
            for (Configuration config : insertParams) {
                insert(config);
            }
        }

        if (!updateParams.isEmpty()) {
            for (Configuration config : updateParams) {
                update(config);
            }
        }

        if (!deleteParams.isEmpty()) {
            for (Object[] lObj : deleteParams) {
                Integer i = new Integer(lObj[1].toString());
                sqlQueryFactory.delete(configuration)
                        .where(configuration.code.eq(((String) lObj[0])), configuration.departmentId.eq(i))
                        .execute();
            }
        }
    }

    public void insert(Configuration config) {
        config.setCode(ConfigurationParam.getNameValueAsDB(config.getCode()));
        sqlQueryFactory.insert(configuration)
                .columns(configuration.departmentId, configuration.code, configuration.value)
                .values(config.getDepartmentId(), config.getCode(), config.getValue())
                .execute();
    }

    public void update(Configuration config) {
        config.setCode(ConfigurationParam.getNameValueAsDB(config.getCode()));
        sqlQueryFactory.update(configuration)
                .where(configuration.departmentId.eq(config.getDepartmentId()), configuration.code.eq(config.getCode()))
                .set(configuration.value, config.getValue())
                .execute();
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

    public void delete(Configuration config) {
        sqlQueryFactory.delete(configuration)
                .where(configuration.departmentId.eq(config.getDepartmentId()), configuration.code.eq((config.getCode())))
                .execute();
    }


    @Override
    public void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId) {
        List<Configuration> updateParams = new LinkedList<>();

        for (Map.Entry<ConfigurationParam, String> entry : configurationParamMap.entrySet()) {
            Configuration entity = new Configuration(entry.getKey().name(), (int)departmentId, entry.getValue());
            updateParams.add(entity);
        }

        if (!updateParams.isEmpty()) {
            for (Configuration config : updateParams) {
                if (sqlQueryFactory.select(SQLExpressions.all).from(configuration)
                        .where(configuration.code.eq(config.getCode()), configuration.departmentId.eq(config.getDepartmentId()))
                        .transform(GroupBy.groupBy(configuration.code).list(configurationBean)).isEmpty()) {
                    insert(config);
                } else {
                    update(config);
                }
            }
        }
    }


    @Override
    public boolean save(Configuration config) {
        try {
            sqlQueryFactory.insert(configuration)
                    .columns(configuration.code, configuration.departmentId, configuration.value)
                    .values(config.getCode(), config.getDepartmentId(), config.getValue())
                    .execute();

        } catch (Exception e) {

        }
        return true;
    }
}
