package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Модельный класс для передачи данных о блокировке формы
 */
public class DeclarationLockResult extends ActionResult {
    private boolean declarationDataLocked;

    public boolean isDeclarationDataLocked() {
        return declarationDataLocked;
    }

    public void setDeclarationDataLocked(boolean declarationDataLocked) {
        this.declarationDataLocked = declarationDataLocked;
    }
}
