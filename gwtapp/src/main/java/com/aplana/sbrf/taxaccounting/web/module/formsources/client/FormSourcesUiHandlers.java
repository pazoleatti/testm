package com.aplana.sbrf.taxaccounting.web.module.formsources.client;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface FormSourcesUiHandlers extends UiHandlers {
	void updateFormSources(DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds);
	void setFormSources(int departmentId, TaxType taxType);
	void setFormReceivers(int departmentId, TaxType taxType);
	void setFormReceiverSources(DepartmentFormType departmentFormType);
}
