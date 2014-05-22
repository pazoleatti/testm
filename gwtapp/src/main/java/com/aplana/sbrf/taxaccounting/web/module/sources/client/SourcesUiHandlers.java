package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface SourcesUiHandlers extends UiHandlers {
    void updateFormSources(DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds);

    void updateDeclarationSources(final DepartmentDeclarationType departmentDeclarationType,
                                  List<Long> sourceDepartmentFormTypeIds);

    void getFormSources(Integer departmentId);

    void getFormReceivers(Integer departmentId);

    void getFormReceiverSources(DepartmentFormType departmentFormType);

    void getDeclarationReceiverSources(DepartmentDeclarationType departmentDeclarationType);

    void getDeclarationReceivers(Integer departmentId);

    TaxType getTaxType();

}
