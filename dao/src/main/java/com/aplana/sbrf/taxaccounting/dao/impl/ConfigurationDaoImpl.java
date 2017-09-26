package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
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
@Transactional
public class ConfigurationDaoImpl extends AbstractDao implements ConfigurationDao {

	private static final Log LOG = LogFactory.getLog(ConfigurationDaoImpl.class);
    private static final String GET_ALL_ERROR = "Ошибка получения конфигурационных параметров!";

    class ConfigurationRowCallbackHandler implements RowCallbackHandler {
        final ConfigurationParamModel model;

        public ConfigurationRowCallbackHandler(ConfigurationParamModel model) {
            this.model = model;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            try {
                model.setFullStringValue(ConfigurationParam.valueOf(rs.getString("code")), rs.getInt("department_id"), rs.getString("value"));
            } catch (IllegalArgumentException e) {
                // Если параметр не найден в ConfiguratioжnParam, то он просто пропускается (не виден на клиенте)
            } catch (SQLException e) {
                throw new DaoException(GET_ALL_ERROR, e);
            }
        }
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
                } catch (SQLException e) {
                    throw new DaoException(GET_ALL_ERROR, e);
                }
            }
        });
        return model;
    }


    @Override
    public ConfigurationParamModel getByDepartment(Integer departmentId) {
        try {
            final ConfigurationParamModel model = new ConfigurationParamModel();
            getJdbcTemplate().query("select code, value, department_id from configuration where department_id = ?",
                    new Object[]{departmentId},
                    new ConfigurationRowCallbackHandler(model));
            return model;
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void save(ConfigurationParamModel model) {
        ConfigurationParamModel oldModel = getAll();
        List<Object[]> insertParams = new LinkedList<Object[]>();
        List<Object[]> updateParams = new LinkedList<Object[]>();
        List<Object[]> deleteParams = new LinkedList<Object[]>();

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
    public void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId) {
        List<Object[]> updateParams = new LinkedList<Object[]>();

        for (Map.Entry<ConfigurationParam, String> entry : configurationParamMap.entrySet()) {
            Object[] entity = new Object[] {
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
