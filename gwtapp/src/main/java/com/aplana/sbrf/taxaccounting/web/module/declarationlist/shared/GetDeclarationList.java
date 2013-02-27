package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationList extends UnsecuredActionImpl<GetDeclarationListResult> {

	private DeclarationDataFilter declarationFilter;

	public GetDeclarationList() {

	}

	public DeclarationDataFilter getDeclarationFilter() {
		return declarationFilter;
	}

	public void setDeclarationFilter(DeclarationDataFilter declarationFilter) {
		this.declarationFilter = declarationFilter;
	}
}
