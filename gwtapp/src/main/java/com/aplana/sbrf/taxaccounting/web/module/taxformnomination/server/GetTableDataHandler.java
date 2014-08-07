package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxNominationFilter;
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
        TaxNominationFilter filter = new TaxNominationFilter();
        filter.setSortColumn(action.getSortColumn());
        filter.setAscSorting(action.isAsc());

        List<FormTypeKind> data = new ArrayList<FormTypeKind>();
        // загрузка данных
        if (action.isForm()) {
            data.addAll(departmentFormTypeService.getAllFormAssigned(departmentsIds, taxType, filter));
        } else {
            data.addAll(departmentFormTypeService.getAllDeclarationAssigned(departmentsIds, taxType, filter));
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

        // обрезание по пейджингу
        if (action.getCount() != 0) {
            int toIndex = action.getStartIndex() +
                    action.getCount() > data.size() ? data.size() : action.getStartIndex() + action.getCount();
            result.setTableData(new ArrayList<FormTypeKind>(data.subList(action.getStartIndex(), toIndex)));
        } else {
            result.setTableData(data);
        }
        result.setTotalCount(data.size());

        return result;
    }

    @Override
    public void undo(GetTableDataAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
