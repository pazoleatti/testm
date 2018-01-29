package com.aplana.sbrf.taxaccounting.model.action;

import java.util.Collections;
import java.util.List;

/**
 * Данные для создания назначений подразделениям налоговых форм
 */
public class CreateDeclarationTypeAssignmentAction {
    /**
     * Список ID подразделений
     */
    private List<Integer> departmentIds;

    /**
     * Список ID видов форм
     */
    private List<Long> declarationTypeIds;

    /**
     * Список ID исполнителей
     */
    private List<Integer> performerIds;

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<Long> getDeclarationTypeIds() {
        return declarationTypeIds;
    }

    public void setDeclarationTypeIds(List<Long> declarationTypeIds) {
        this.declarationTypeIds = declarationTypeIds;
    }

    public List<Integer> getPerformerIds() {
        if (performerIds == null) {
            performerIds = Collections.emptyList();
        }
        return performerIds;
    }

    public void setPerformerIds(List<Integer> performerIds) {
        this.performerIds = performerIds;
    }
}
