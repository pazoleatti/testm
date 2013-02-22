package com.aplana.sbrf.taxaccounting.service.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface DictionaryRegionService {
    /**
     * Проверяет есть ли запись с указанным кодом и именем
     * @param code
     * @param name
     * @return
     */
    public Boolean isValidCodeAndName(Integer code, String name);

    /**
     * Получает регион по  полному коду ОКАТО органищации
     * @param okado код окато организации полностью
     * @return
     */
    public DictionaryRegion getRegionByOkadoOrg(String okado);
}
