package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

/**
 * Реализация ДАО для работа с параметрами приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:41
 */

@Repository
@Transactional
public class ConfigurationDaoImpl extends AbstractDao implements ConfigurationDao {
	
	/**
	 * Название столбца в базе данных, соответствующее коду параметра
	 */
	private static final String CODE_COLUMN_NAME = "code";

	/**
	 * Название столбца в базе данных, соответствующее значению параметра
	 */
	private static final String VALUE_COLUMN_NAME = "value";

	private static final String PARAM_NOT_FOUND_EXCEPTION = "Обнаружен незарегистрированный параметр приложения с кодом = \"%s\"";

	@Override
	public Map<ConfigurationParam, String> loadParams() {
		final Map<ConfigurationParam, String> params = new HashMap<ConfigurationParam, String>();
		getJdbcTemplate().query("select code, value from configuration", new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String code = rs.getString(CODE_COLUMN_NAME);
				String value = rs.getString(VALUE_COLUMN_NAME);
				try {
					params.put(ConfigurationParam.valueOf(code), value);
				} catch (IllegalArgumentException e) {
					throw new DaoException(String.format(PARAM_NOT_FOUND_EXCEPTION, code));
				}
			}
			
		});
		return params;
	}

	@Override
	public void saveParams(Map<ConfigurationParam, String> newParams) {
		Map<ConfigurationParam, String> oldParams = loadParams();

		List<Object[]> insertParams = new ArrayList<Object[]>();
		List<Object[]> updateParams = new ArrayList<Object[]>();
		for (Map.Entry<ConfigurationParam, String> entry : newParams.entrySet()) {
			ConfigurationParam param = entry.getKey();
			String value = entry.getValue();

			if (oldParams.containsKey(entry.getKey())) {
				updateParams.add(new Object[] {value, param.toString()});
			} else {
				insertParams.add(new Object[] {value, param.toString()});
			}
		}
		if (insertParams.size() > 0) {
			getJdbcTemplate().batchUpdate("insert into configuration (value, code) values (?, ?)", insertParams);
		}
		if (updateParams.size() > 0) {
			getJdbcTemplate().batchUpdate("update configuration set value = ? where code = ?", updateParams);
		}
	}
}
