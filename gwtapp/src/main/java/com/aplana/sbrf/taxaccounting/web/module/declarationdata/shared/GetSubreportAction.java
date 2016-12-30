package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение параметров спец. отчета
 *
 * @author lhaziev
 */
public class GetSubreportAction extends UnsecuredActionImpl<GetSubreportResult> implements ActionName {

    private long declarationId;
    private long declarationSubreportId;

    public long getDeclarationId() {
        return declarationId;
    }

    public void setDeclarationId(long declarationId) {
        this.declarationId = declarationId;
    }

    public long getDeclarationSubreportId() {
        return declarationSubreportId;
    }

    public void setDeclarationSubreportId(long declarationSubreportId) {
        this.declarationSubreportId = declarationSubreportId;
    }

    @Override
	public String getName() {
		return "Получение параметров спец отчета";
	}
}
