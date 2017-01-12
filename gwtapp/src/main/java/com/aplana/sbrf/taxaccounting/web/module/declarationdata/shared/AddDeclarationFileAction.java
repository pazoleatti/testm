package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Добавление сфайла в НФ
 * @author Lhaziev
 */
public class AddDeclarationFileAction extends UnsecuredActionImpl<AddDeclarationFileResult> {
    private DeclarationData declarationData;
    private String uuid;

    public DeclarationData getDeclarationData() {
        return declarationData;
    }

    public void setDeclarationData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
