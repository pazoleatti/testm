package com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;
import java.util.List;

public interface DeadlineDialogUiHandlers extends UiHandlers {
    void setDepartments(List<Department> departments, List<DepartmentPair> selectedDepartments);
    void setTitle(String periodName, int year);
    void setDeadLine(Date date);
    void setSelectedPeriod(TableRow selectedPeriod);
    void setDepartmentDeadline(int senderDepartmentId, Integer receiverDepartmentId);
    void updateDepartmentDeadline(List<DepartmentPair> departments, Date deadline, Boolean isNeedUpdateChildren);
    void setTaxType(TaxType taxType);
}
