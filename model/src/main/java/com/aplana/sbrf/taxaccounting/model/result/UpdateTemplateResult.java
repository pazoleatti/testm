package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат выполнения операции изменения макета
 */
public class UpdateTemplateResult extends ActionResult {

    /**
     * Изменение прошло успешно
     */
    private boolean success;

    /**
     * Нужно подтверждение на выполнение операции
     */
    private boolean confirmNeeded;

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
}
