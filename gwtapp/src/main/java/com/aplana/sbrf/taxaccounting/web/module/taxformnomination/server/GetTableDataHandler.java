package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.QueryParams;
import com.aplana.sbrf.taxaccounting.model.TaxNominationColumnEnum;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetTableDataHandler extends AbstractActionHandler<GetTableDataAction, GetTableDataResult> {

    @Autowired
    private DepartmentService departmentService;

    public GetTableDataHandler() {
        super(GetTableDataAction.class);
    }

    @Autowired
    private SourceService departmentFormTypeService;

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        char taxType = action.getTaxType();
        List<Long> departmentsIds = new ArrayList<Long>();
        for (Integer id : action.getDepartmentsIds()){
            departmentsIds.add(Long.valueOf(id));
        }
        // Фильтр для сортировки
        QueryParams queryParams = new QueryParams();
        queryParams.setSearchOrdering(action.getSortColumn() == null ? TaxNominationColumnEnum.DEPARTMENT : action.getSortColumn());
        queryParams.setAscending(action.isAsc());
        queryParams.setFrom(action.getStartIndex());
        queryParams.setCount(action.getCount());

        List<FormTypeKind> data = new ArrayList<FormTypeKind>();
        // загрузка данных
        if (action.isForm()) {
            data.addAll(departmentFormTypeService.getAllFormAssigned(departmentsIds, taxType, queryParams));
            result.setTotalCount(departmentFormTypeService.getAssignedFormsCount(departmentsIds, taxType));
        } else {
            data.addAll(departmentFormTypeService.getAllDeclarationAssigned(departmentsIds, taxType, queryParams));
            result.setTotalCount(departmentFormTypeService.getAssignedDeclarationsCount(departmentsIds, taxType));
        }
        // формирование мапы с полным названием подразделения
        // TODO - лучше получать иерархические названия подразделений одним запросом!!!
        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        for (FormTypeKind item : data) {
            int departmentId = item.getDepartment().getId();
            Integer performerId = item.getPerformer() != null ? item.getPerformer().getId() : null;
            if (departmentFullNames.get(departmentId) == null) {
                departmentFullNames.put(departmentId, departmentService.getParentsHierarchy(departmentId));
            }
            if (performerId != null && departmentFullNames.get(performerId) == null) {
                departmentFullNames.put(performerId, departmentService.getParentsHierarchy(performerId));
            }
        }
        result.setDepartmentFullNames(departmentFullNames);

        result.setTableData(data);

        return result;
    }

    @Override
    public void undo(GetTableDataAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
