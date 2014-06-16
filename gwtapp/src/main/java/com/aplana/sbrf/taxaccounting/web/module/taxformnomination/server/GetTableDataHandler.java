package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
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

import java.util.*;

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

    static final TaxNominationDataComparator comparator = new TaxNominationDataComparator();

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        char taxType = action.getTaxType();
        List<FormTypeKind> data = new ArrayList<FormTypeKind>();
        // загрузка данных
        if (action.isForm()) {
            for (Integer depoId : action.getDepartmentsIds()) {
                data.addAll(departmentFormTypeService.getFormAssigned(depoId.longValue(), taxType));
            }
        } else {
            for (Integer depoId : action.getDepartmentsIds()) {
                data.addAll(departmentFormTypeService.getDeclarationAssigned(depoId.longValue(), taxType));
            }
        }
        // формирование мапы с полным названием подразделения
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

        //сортировка
        comparator.setup(action.getSortColumn(), action.isAsc(), action.isForm(), departmentFullNames);
        Collections.sort(data, comparator);

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
