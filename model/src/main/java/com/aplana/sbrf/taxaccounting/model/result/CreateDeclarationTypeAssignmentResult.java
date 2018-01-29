package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат назначения подразделениям налоговых форм
 */
public class CreateDeclarationTypeAssignmentResult extends ActionResult {
    /**
     * Пользователь пытается создать уже существующие назначения
     */
    private boolean creatingExistingRelations;

    public boolean isCreatingExistingRelations() {
        return creatingExistingRelations;
    }

    public void setCreatingExistingRelations(boolean creatingExistingRelations) {
        this.creatingExistingRelations = creatingExistingRelations;
    }
}
