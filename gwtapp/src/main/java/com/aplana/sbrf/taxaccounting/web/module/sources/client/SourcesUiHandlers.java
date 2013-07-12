package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface SourcesUiHandlers extends UiHandlers {
	void updateFormSources(DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds);
	void setFormSources(int departmentId, TaxType taxType);
	void setFormReceivers(int departmentId, TaxType taxType);
	void setFormReceiverSources(DepartmentFormType departmentFormType);
}
