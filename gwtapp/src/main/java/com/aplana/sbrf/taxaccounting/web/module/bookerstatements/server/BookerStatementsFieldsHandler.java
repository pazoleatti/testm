package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.BookerStatementsFieldsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.BookerStatementsFieldsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

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

    @Autowired
    private SecurityService securityService;

    @Override
    public BookerStatementsFieldsResult execute(BookerStatementsFieldsAction action, ExecutionContext executionContext) throws ActionException {
        BookerStatementsFieldsResult result = new BookerStatementsFieldsResult();
        TAUser currUser = securityService.currentUserInfo().getUser();

        Set<Integer> availableDepartmentSet = new HashSet<Integer>();
        availableDepartmentSet.addAll(departmentService.getBADepartmentIds(currUser));
        // Необходимые для дерева подразделения
        result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(availableDepartmentSet).values()));
        result.setDepartmentIds(availableDepartmentSet);
        result.setYear(Calendar.getInstance().get(Calendar.YEAR));

        return result;
    }

    @Override
    public void undo(BookerStatementsFieldsAction action, BookerStatementsFieldsResult result, ExecutionContext executionContext) throws ActionException {
    }
}
