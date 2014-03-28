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
    private String declarationTypes;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDeclarationTypes() {
        return declarationTypes;
    }

    public void setDeclarationTypes(String declarationTypes) {
        this.declarationTypes = declarationTypes;
    }
}
