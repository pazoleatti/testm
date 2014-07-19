package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Реализация ДАО для работа с параметрами приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:41
 */

@Repository
@Transactional
public class ConfigurationDaoImpl extends AbstractDao implements ConfigurationDao {
    private final static String GET_ALL_ERROR = "Ошибка получения конфигурационных параметров!";

    @Override
    public ConfigurationParamModel getAll() {
        final ConfigurationParamModel model = new ConfigurationParamModel();
        getJdbcTemplate().query("select code, value, department_id from configuration", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                try {
                    Clob clobValue = rs.getClob("value");
                    String value = null;
                    if (clobValue != null) {
                        char clobVal[] = new char[(int) clobValue.length()];
                        clobValue.getCharacterStream().read(clobVal);
                        value = new String(clobVal);
                    }
                    model.setFullStringValue(ConfigurationParam.valueOf(rs.getString("code")), rs.getInt("department_id"), value);
                } catch (IllegalArgumentException e) {
                    // Если параметр не найден в ConfigurationParam, то он просто пропускается (не виден на клиенте)
                } catch (IOException e) {
                    throw new DaoException(GET_ALL_ERROR);
                } catch (SQLException e) {
                    throw new DaoException(GET_ALL_ERROR);
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
                    new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            try {
                                Clob clobValue = rs.getClob("value");
                                String value = null;
                                if (clobValue != null) {
                                    char clobVal[] = new char[(int) clobValue.length()];
                                    clobValue.getCharacterStream().read(clobVal);
                                    value = new String(clobVal);
                                }
                                model.setFullStringValue(ConfigurationParam.valueOf(rs.getString("code")), rs.getInt("department_id"), value);
                            } catch (IllegalArgumentException e) {
                                // Если параметр не найден в ConfigurationParam, то он просто пропускается (не виден на клиенте)
                            } catch (IOException e) {
                                throw new DaoException(GET_ALL_ERROR);
                            } catch (SQLException e) {
                                throw new DaoException(GET_ALL_ERROR);
                            }
                        }
                    });
            return model;
        } catch (DataAccessException e){
            logger.error("", e);
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
                        || !oldModel.get(configurationParam).containsKey(departmentId)) {
                    insertParams.add(entity);
                } else {
                    updateParams.add(entity);
                }
            }
        }

        for (Map.Entry<ConfigurationParam, Map<Integer, List<String>>> entry : oldModel.entrySet()) {
            for (Integer departmentId : entry.getValue().keySet()) {
                if (!model.containsKey(entry.getKey(), departmentId)) {
                    deleteParams.add(new Object[]{entry.getKey().name(), departmentId});
                }
            }
        }

        if (insertParams.size() > 0) {
            getJdbcTemplate().batchUpdate("insert into configuration (value, code, department_id) values (?, ?, ?)",
                    insertParams);
        }
        if (updateParams.size() > 0) {
            getJdbcTemplate().batchUpdate("update configuration set value = ? where code = ? and department_id = ?",
                    updateParams);
        }
        if (deleteParams.size() > 0) {
            getJdbcTemplate().batchUpdate("delete from configuration where code = ? and department_id = ?",
                    deleteParams);
        }
    }
}
