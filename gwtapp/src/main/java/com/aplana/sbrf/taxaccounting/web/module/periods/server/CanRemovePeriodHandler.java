package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CanRemovePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CanRemovePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CanRemovePeriodHandler extends AbstractActionHandler<CanRemovePeriodAction, CanRemovePeriodResult> {

	public CanRemovePeriodHandler() {
		super(CanRemovePeriodAction.class);
	}

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DeclarationTemplateService declarationService;
    @Autowired
    private DeclarationDataService declarationDataService;

	@Override
	public CanRemovePeriodResult execute(CanRemovePeriodAction action, ExecutionContext executionContext) throws ActionException {
		CanRemovePeriodResult result = new CanRemovePeriodResult();
        TAUserInfo user = securityService.currentUserInfo();
        List<Integer> departmentIds = departmentService.getBADepartmentIds(user.getUser());
        List<LogEntry> logs = new ArrayList<LogEntry>();

        //Check forms
        List<FormData> formDatas = formDataService.find(departmentIds, action.getReportPeriodId());
        for (FormData fd : formDatas) {
            logs.add(new LogEntry(LogLevel.ERROR, "Форма \"" + fd.getFormType().getName() + "\" \"" + fd.getKind().getTitle() +
                    "\" в подразделении \"" + departmentService.getDepartment(fd.getDepartmentId()).getName() + "\" находится в " +
                    action.getOperationName() +
                    " периоде!"));
        }

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentIds);
        filter.setReportPeriodIds(Collections.singletonList(action.getReportPeriodId()));
        List<Long> declarations = declarationDataSearchService.getDeclarationIds(filter, DeclarationDataSearchOrdering.ID, true);
        for (Long id : declarations) {
            DeclarationData dd = declarationDataService.get(id, user);
            DeclarationTemplate dt = declarationService.get(dd.getDeclarationTemplateId());
            logs.add(new LogEntry(LogLevel.ERROR, "\""+dt.getType().getName() + "\" в подразделении \"" +
                    departmentService.getDepartment(dd.getDepartmentId()).getName() + "\" находится в " +
                    action.getOperationName() +
                    " периоде!"));
        }
		result.setCanRemove(formDatas.isEmpty() && declarations.isEmpty());
        result.setUuid(logEntryService.save(logs));
		return result;
	}

	@Override
	public void undo(CanRemovePeriodAction canRemovePeriodAction, CanRemovePeriodResult canRemovePeriodResult, ExecutionContext executionContext) throws ActionException {
	}
}
