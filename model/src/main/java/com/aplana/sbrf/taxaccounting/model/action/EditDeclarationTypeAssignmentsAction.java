package com.aplana.sbrf.taxaccounting.model.action;

import java.util.Collections;
import java.util.List;

/**
 * Данные для редактирования назначений подразделениям налоговых форм
 */
public class EditDeclarationTypeAssignmentsAction {
    /**
     * Список ID назначений
     */
    private List<Integer> assignmentIds;

    /**
     * Список ID исполнителей
     */
    private List<Integer> performerIds;

    public List<Integer> getAssignmentIds() {
        return assignmentIds;
    }

    public void setAssignmentIds(List<Integer> assignmentIds) {
        this.assignmentIds = assignmentIds;
    }

    public List<Integer> getPerformerIds() {
        return performerIds;
    }

    public void setPerformerIds(List<Integer> performerIds) {
        this.performerIds = performerIds;
    }
}
