package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

import java.util.Map;

/**
 * ДАО для работы с параметрами приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:34
 */

public interface ConfigurationDao {

	/**
	 * Название столбца в базе данных, соответствующее коду параметра
	 */
	static final String CODE_COLUMN_NAME = "code";

	/**
	 * Название столбца в базе данных, соответствующее значению параметра
	 */
	static final String VALUE_COLUMN_NAME = "value";

	/**
	 * Читает из БД значения всех параметров.
	 * @return
	 */
	Map<ConfigurationParam, String> loadParams();

	/**
	 * Сохраняет значения параметров в БД. Если параметр в БД отсутствует, то он создается
	 * @param params
	 */
	void saveParams(Map<ConfigurationParam, String> params);

}
