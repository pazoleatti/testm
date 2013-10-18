package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * DAO для получения данных для кэшей для скриптов налоговых форм
 * @author Dmitriy Levykin
 */
public interface FormDataCacheDao {
    Map<Long, Map<String, RefBookValue>> getRefBookMap(Long formDataId);
}
