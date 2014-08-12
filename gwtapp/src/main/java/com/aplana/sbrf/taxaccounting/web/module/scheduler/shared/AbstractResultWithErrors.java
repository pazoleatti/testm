package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author auldanov on 12.08.2014.
 */
public abstract class AbstractResultWithErrors implements Result {
    /** Ошибки при обновлении */
    private boolean hasErrors;

    /** Список ошибок */
    private String errorMessage;

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
