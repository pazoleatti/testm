package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Получает активные виды налоговых форм. ПО идее срабатывает после выбора определенных полей.
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class BookerStatementsFieldsHandler extends AbstractActionHandler<BookerStatementsFieldsAction, BookerStatementsFieldsResult> {
    public BookerStatementsFieldsHandler() {
        super(BookerStatementsFieldsAction.class);
    }

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    FormDataAccessService dataAccessService;

    @Override
    public BookerStatementsFieldsResult execute(BookerStatementsFieldsAction action, ExecutionContext executionContext) throws ActionException {
        BookerStatementsFieldsResult result = new BookerStatementsFieldsResult();
        switch (action.getFieldsNum()){
            case FIRST:
	            List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
	            periodList.addAll(periodService.getOpenForUser(securityService.currentUserInfo().getUser(), TaxType.INCOME));
                result.setReportPeriods(periodList);
                break;
            case SECOND:
                List<Integer> departments =
                        departmentService.getOpenPeriodDepartments(securityService.currentUserInfo().getUser(), asList(TaxType.INCOME), action.getFieldId());
                if (departments.isEmpty()){
                    result.setDepartments(new ArrayList<Department>());
                    result.setDepartmentIds(new HashSet<Integer>());
                } else {
	                Set<Integer> departmentIds = new HashSet<Integer>(departments);
                    result.setDepartments(new ArrayList<Department>(
                            departmentService.getRequiredForTreeDepartments(departmentIds).values()));
                    result.setDepartmentIds(departmentIds);
                }
                break;
        }

        return result;
    }

    @Override
    public void undo(BookerStatementsFieldsAction action, BookerStatementsFieldsResult result, ExecutionContext executionContext) throws ActionException {

    }
}
