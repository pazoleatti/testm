package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;

/**
 * Результат выполнения операции ввода/вывода из действия макета
 */
public class UpdateTemplateStatusResult extends ActionResult {

    /**
     * Изменение прошло успешно
     */
    private boolean success;

    /**
     * Нужно подтверждение на выполнение операции
     */
    private boolean confirmNeeded;

    private VersionedObjectStatus status;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isConfirmNeeded() {
        return confirmNeeded;
    }

    public void setConfirmNeeded(boolean confirmNeeded) {
        this.confirmNeeded = confirmNeeded;
    }

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }
}
