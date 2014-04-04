package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.gwtplatform.dispatch.shared.Result;


import java.util.List;
import java.util.Set;

/**
 * @author Dmitriy Levykin
 */
public class GetCheckDeclarationResult implements Result {

    // Список всех подразделений
    private String uuid;
    private boolean declarationFormFound = false;
    private boolean hasError = false;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isDeclarationFormFound() {
        return declarationFormFound;
    }

    public void setDeclarationFormFound(boolean declarationFormFound) {
        this.declarationFormFound = declarationFormFound;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
