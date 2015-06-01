package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * DAO для получения данных для кэшей для скриптов налоговых форм
 * @author Dmitriy Levykin
 */
public interface FormDataCacheDao {

	/**
	 * Разыменовывает все ссылки НФ
	 * @param formData
	 * @return
	 */
    Map<String, Map<String, RefBookValue>> getRefBookMap(FormData formData);
}
