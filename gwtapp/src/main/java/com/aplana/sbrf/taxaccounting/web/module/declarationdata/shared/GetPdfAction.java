package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение формы предварительного просмотра
 *
 * @author lhaziev
 */
public class GetPdfAction extends UnsecuredActionImpl<GetPdfResult> implements ActionName {

    private long declarationDataId;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    @Override
	public String getName() {
		return "Получение формы предварительного просмотра";
	}
}
