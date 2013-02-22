package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

public interface DictionaryRegionDao {

    /**
     * По коду ОКАТО организации получает список
     * @return
     */
    public List<DictionaryRegion> getListRegions();
}
