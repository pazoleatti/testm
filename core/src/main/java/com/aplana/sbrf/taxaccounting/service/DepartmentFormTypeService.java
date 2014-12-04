package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;

import java.util.List;

/**
 * User: avanteev
 */
public interface DepartmentFormTypeService {
    /**
     * @param performerDepId подразделение исполнитель
     * @return возвращает назначения типов НФ подразделениям
     */
    List<DepartmentFormType> getByPerformerId(int performerDepId);

    List<Long> getIdsByPerformerId(int performerDepId);

    void deleteByIds(List<Long> ids);
}
