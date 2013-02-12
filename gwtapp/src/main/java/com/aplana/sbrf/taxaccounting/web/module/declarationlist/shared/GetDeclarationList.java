package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationList extends UnsecuredActionImpl<GetDeclarationListResult> {

	private DeclarationFilter declarationFilter;

	public GetDeclarationList() {

	}

	public DeclarationFilter getDeclarationFilter() {
		return declarationFilter;
	}

	public void setDeclarationFilter(DeclarationFilter declarationFilter) {
		this.declarationFilter = declarationFilter;
	}
}
