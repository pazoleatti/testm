package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigFetchingAction;
import com.aplana.sbrf.taxaccounting.model.result.DepartmentConfigFetchingResult;

import java.util.List;

/**
 * Сервис для работы с настройками подразделениями
 */
public interface DepartmentConfigService {
    /**
     * Получает настройки подразделений для отображения на представлении
     * @param action    объект содержащих данные используемые для фильтрации
     * @return  список объектов содержащих данные о настройках подразделений
     */
    List<DepartmentConfigFetchingResult> fetchDepartmentConfigs(DepartmentConfigFetchingAction action);
}
