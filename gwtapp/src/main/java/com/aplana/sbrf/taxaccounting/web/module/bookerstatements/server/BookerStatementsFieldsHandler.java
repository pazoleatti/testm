package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.BookerStatementsFieldsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.BookerStatementsFieldsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * Получает активные виды налоговых форм. По идее срабатывает после выбора определенных полей.
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class BookerStatementsFieldsHandler extends AbstractActionHandler<BookerStatementsFieldsAction, BookerStatementsFieldsResult> {
    public BookerStatementsFieldsHandler() {
        super(BookerStatementsFieldsAction.class);
    }

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    FormDataAccessService dataAccessService;

    @Override
    public BookerStatementsFieldsResult execute(BookerStatementsFieldsAction action, ExecutionContext executionContext) throws ActionException {
        BookerStatementsFieldsResult result = new BookerStatementsFieldsResult();

        // TODO дб так, но не работает
               /* Set<Integer> availableDepartmentSet = new HashSet<Integer>();
                List<Department> departments = departmentService.getBADepartments(securityService.currentUserInfo().getUser());
                for (Department d: departments){
                    availableDepartmentSet.add(d.getId());
                }

                if (departments.isEmpty()){
                    result.setDepartments(new ArrayList<Department>());
                    result.setDepartmentIds(new HashSet<Integer>());
                } else {
                    result.setDepartments(new ArrayList<Department>(
                            departmentService.getRequiredForTreeDepartments(availableDepartmentSet).values()));
                    result.setDepartmentIds(availableDepartmentSet);
                }
                */
        Set<Integer> availableDepartmentSet = new HashSet<Integer>();
        result.setDepartments(departmentService.listAll());
        for (Department dep : result.getDepartments()) {
            availableDepartmentSet.add(dep.getId());
        }
        result.setDepartmentIds(availableDepartmentSet);
        result.setYear(Calendar.getInstance().get(Calendar.YEAR));

        return result;
    }

    @Override
    public void undo(BookerStatementsFieldsAction action, BookerStatementsFieldsResult result, ExecutionContext executionContext) throws ActionException {

    }
}
