package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Dmitriy Levykin
 */
public class SaveDepartmentCombinedResult implements Result {


    private String uuid;
    private boolean hasError = false;
    private String declarationTypes;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getDeclarationTypes() {
        return declarationTypes;
    }

    public void setDeclarationTypes(String declarationTypes) {
        this.declarationTypes = declarationTypes;
    }
}
