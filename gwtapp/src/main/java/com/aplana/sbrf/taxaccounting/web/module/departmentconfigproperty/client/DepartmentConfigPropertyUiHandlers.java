package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;

public interface DepartmentConfigPropertyUiHandlers extends AplanaUiHandlers {
    void onSave();
    void onFind();
    void onDelete();
    void onCancel();
    void reloadDepartments(TaxType taxType, Integer currentDepartmentId);
    void createTableColumns();

    void getRefBookPeriod(Integer currentReportPeriodId, Integer currentDepartmentId);
}
