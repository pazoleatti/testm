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

    static final Comparator<FormTypeKind> comparator =  new Comparator<FormTypeKind>() {
        /**
         * Порядок сортировки записей при отображении:
         * - Подразделение
         * - Тип налоговой формы
         * - Вид налоговой формы
         *
         * @param o1
         * @param o2
         * @return
         */
        public int compare(FormTypeKind o1, FormTypeKind o2) {
            int result = o1.getDepartment().getName().compareTo(o2.getDepartment().getName());
            if (result == 0) {
                result = o1.getKind().getName().compareTo(o2.getKind().getName());
                if (result == 0) {
                    result = o1.getName().compareTo(o2.getName());
                }
            }

            return result;
        }
    };

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        char taxType = action.getTaxType();
        List<FormTypeKind> data = new ArrayList<FormTypeKind>();
        if (action.isForm()){
            for (Integer depoId: action.getDepartmentsIds()){
                data.addAll(departmentFormTypeService.getFormAssigned(depoId.longValue(), taxType));
            }

            // sort
            Collections.sort(data, comparator);
        }
        else {
            for (Integer depoId: action.getDepartmentsIds()){
                data.addAll(departmentFormTypeService.getDeclarationAssigned(depoId.longValue(), taxType));
            }
        }
        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        for(FormTypeKind item: data) {
            if (departmentFullNames.get(item.getDepartment().getId()) == null) departmentFullNames.put(item.getDepartment().getId(), departmentService.getParentsHierarchy(item.getDepartment().getId()));
            if (item.getPerformer() != null && departmentFullNames.get(item.getPerformer().getId()) == null) departmentFullNames.put(item.getPerformer().getId(), departmentService.getParentsHierarchy(item.getPerformer().getId()));
        }
        result.setDepartmentFullNames(departmentFullNames);

        if (action.getCount() != 0){
            int toIndex = action.getStartIndex()+action.getCount() > data.size() ?
                        data.size()-1 : action.getStartIndex()+action.getCount();
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
