package com.aplana.sbrf.taxaccounting.service.script;

import java.util.List;
import java.util.Map;

/**
 * Сервис импорта данных в справочники ФИАС
 *
 * @author Andrey Drunk
 */
public interface ImportFiasDataService {

    /**
     *
     */
    void insertRecords(String table, List<Map<String, Object>> records);


}
