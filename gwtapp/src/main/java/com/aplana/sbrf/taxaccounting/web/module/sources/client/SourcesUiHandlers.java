package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.AssignDialogView;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.ButtonClickHandlers;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;
import java.util.Set;

public interface SourcesUiHandlers extends UiHandlers {
    void updateCurrentAssign(DepartmentAssign departmentFormType, CurrentAssign currentAssign, PeriodsInterval periodInterval);

    void openAssignDialog(AssignDialogView.State state, PeriodsInterval pi, ButtonClickHandlers handlers);

    void closeAssignDialog();

    void getFormsRight(Integer departmentId, DepartmentAssign selectedLeft);

    void getFormsLeft(Integer departmentId);

    void getDecsLeft(Integer departmentId);

    void getDecsRight(Integer departmentId, DepartmentAssign selectedLeft);

    void getCurrentAssigns(DepartmentAssign departmentAssign);

    TaxType getTaxType();

    void deleteCurrentAssign(DepartmentAssign departmentAssign, Set<CurrentAssign> currentAssigns);

    void prepareUpdateAssign(DepartmentAssign departmentAssign, CurrentAssign currentAssign);

    void createAssign(DepartmentAssign leftObject, Set<DepartmentAssign> rightSelectedObjects, PeriodsInterval periodsInterval, List<Integer> leftDepartment, List<Integer> rightDepartment);
}
