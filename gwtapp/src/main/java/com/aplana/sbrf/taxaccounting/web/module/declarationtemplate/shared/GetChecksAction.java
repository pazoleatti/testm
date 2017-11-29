package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetChecksAction extends UnsecuredActionImpl<GetChecksResult> implements ActionName{

	private int declarationTypeId;
	private Integer declarationTemplateId;

	public int getDeclarationTypeId() {
		return declarationTypeId;
	}

	public void setDeclarationTypeId(int declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
	}

	public Integer getDeclarationTemplateId() {
		return declarationTemplateId;
	}

	public void setDeclarationTemplateId(Integer declarationTemplateId) {
		this.declarationTemplateId = declarationTemplateId;
	}

	@Override
	public String getName() {
		return "Получение фатальности проверок";
	}
}
