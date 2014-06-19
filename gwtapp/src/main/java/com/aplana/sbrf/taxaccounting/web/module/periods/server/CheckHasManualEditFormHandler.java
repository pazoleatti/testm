package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasManualEditFormAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasManualEditFormResult;
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

@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CheckHasManualEditFormHandler extends AbstractActionHandler<CheckHasManualEditFormAction, CheckHasManualEditFormResult> {

    public CheckHasManualEditFormHandler() {
        super(CheckHasManualEditFormAction.class);
    }

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private LogEntryService logEntryService;

    @Override
    public CheckHasManualEditFormResult execute(CheckHasManualEditFormAction action, ExecutionContext executionContext) throws ActionException {
        List<Integer> departmentsForClose =
                periodService.getAvailableDepartmentsForClose(action.getTaxType(), securityService.currentUserInfo().getUser(), action.getDepartmentId());


        List<FormData> manualInputForms =
                formDataService.getManualInputForms(departmentsForClose, action.getReportPeriodId(), action.getTaxType(), action.getKind());
        List<LogEntry> logs = new ArrayList<LogEntry>();
        Set<Long> uniqId = new HashSet<Long>();
        for (FormData formData : manualInputForms) {
            if (!uniqId.contains(formData.getId())) {
                logs.add(new LogEntry(LogLevel.ERROR,
                        "Для формы " + formData.getFormType().getName() + " " + formData.getKind().getName() +
                                " в подразделении " + departmentService.getDepartment(formData.getDepartmentId()).getName() + " существует версия ручного ввода"));
            }
            uniqId.add(formData.getId());
        }
        CheckHasManualEditFormResult result = new CheckHasManualEditFormResult();
        result.setHasManualInputForms(!logs.isEmpty());
        result.setUuid(logEntryService.save(logs));

        return result;
    }

    @Override
    public void undo(CheckHasManualEditFormAction checkHasManualEditFormAction, CheckHasManualEditFormResult checkHasManualEditFormResult, ExecutionContext executionContext) throws ActionException {

    }
}
