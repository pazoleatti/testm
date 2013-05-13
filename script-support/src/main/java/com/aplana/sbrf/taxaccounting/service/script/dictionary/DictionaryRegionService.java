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
    Boolean isValidCode(String code);

    /**
     * Получает регион по  полному коду ОКАТО органищации
     * @param okato код окато организации полностью
     * @return
     */
    DictionaryRegion getRegionByOkatoOrg(String okato);

    /**
     *
     * @param name
     * @return
     */
    DictionaryRegion getRegionByName(String name);

    /**
     *
     * @param code
     * @return
     */
    DictionaryRegion getRegionByCode(String code) throws IllegalArgumentException;
}
