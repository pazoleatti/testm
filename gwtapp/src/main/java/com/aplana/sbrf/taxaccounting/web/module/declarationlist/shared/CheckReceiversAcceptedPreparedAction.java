package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckReceiversAcceptedPreparedAction extends UnsecuredActionImpl<CheckReceiversAcceptedPreparedResult> implements ActionName {

    Long declarationDataId;

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    @Override
    public String getName() {
        return "Проверить приемники";
    }
}
