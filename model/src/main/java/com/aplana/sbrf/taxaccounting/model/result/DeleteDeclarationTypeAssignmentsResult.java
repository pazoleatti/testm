package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат удаления назначений подразделениям налоговых форм
 */
public class DeleteDeclarationTypeAssignmentsResult extends ActionResult {
    /**
     * Пользователь пытается отменить назначения, для которого существуют налоговые формы
     */
    private boolean deletingAssignmentsWithDeclarations;

    public boolean isDeletingAssignmentsWithDeclarations() {
        return deletingAssignmentsWithDeclarations;
    }

    public void setDeletingAssignmentsWithDeclarations(boolean deletingAssignmentsWithDeclarations) {
        this.deletingAssignmentsWithDeclarations = deletingAssignmentsWithDeclarations;
    }
}
