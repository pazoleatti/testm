package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение данных для формы "Файлы и комментарии"
 * @author lhaziev
 */
public class GetDeclarationFilesCommentsAction extends UnsecuredActionImpl<GetDeclarationFilesCommentsResult> {
    private DeclarationData declarationData;

    public DeclarationData getDeclarationData() {
        return declarationData;
    }

    public void setDeclarationData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }
}
