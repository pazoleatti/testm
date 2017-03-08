package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationList extends UnsecuredActionImpl<GetDeclarationListResult> {

	private DeclarationDataFilter declarationFilter;
    private boolean isReports;


	public DeclarationDataFilter getDeclarationFilter() {
		return declarationFilter;
	}

	public void setDeclarationFilter(DeclarationDataFilter declarationFilter) {
		this.declarationFilter = declarationFilter;
	}

    public boolean isReports() {
        return isReports;
    }

    public void setReports(boolean isReports) {
        this.isReports = isReports;
    }
}
