package com.aplana.sbrf.taxaccounting.service.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface DictionaryRegionService {
    /**
     * Проверяет есть ли запись с указанным кодом и именем
     *
     * @param code
     * @return
     */
    public Boolean isValidCode(Integer code);

    /**
     * Получает регион по  полному коду ОКАТО органищации
     * @param okato код окато организации полностью
     * @return
     */
    public DictionaryRegion getRegionByOkatoOrg(String okato);
}
