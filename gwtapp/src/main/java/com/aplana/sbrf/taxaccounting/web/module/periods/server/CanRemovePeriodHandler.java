package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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

import java.util.*;

@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CanRemovePeriodHandler extends AbstractActionHandler<CanRemovePeriodAction, CanRemovePeriodResult> {

	public CanRemovePeriodHandler() {
		super(CanRemovePeriodAction.class);
	}

	@Autowired
	private RefBookFactory rbFactory;
	@Autowired
	private PeriodService periodService;
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

	static final long REF_BOOK_101 = 50L;
	static final long REF_BOOK_102 = 52L;

	@Override
	public CanRemovePeriodResult execute(CanRemovePeriodAction action, ExecutionContext executionContext) throws ActionException {
		CanRemovePeriodResult result = new CanRemovePeriodResult();
        TAUserInfo user = securityService.currentUserInfo();
        List<Department> departments = departmentService.getBADepartments(user.getUser());
        List<Integer> departmentIds = new ArrayList<Integer>();
        List<LogEntry> logs = new ArrayList<LogEntry>();
        for (Department dep : departments) {
            departmentIds.add(dep.getId());
        }
        String depFilter = buildFilter(departmentIds, action.getReportPeriodId());

        //Check INCOME_101
		RefBookDataProvider dataProvider = rbFactory.getDataProvider(REF_BOOK_101);

		Date endDate = periodService.getEndDate(action.getReportPeriodId()).getTime();
		PagingResult<Map<String, RefBookValue>> result101 =  dataProvider.getRecords(endDate, null, depFilter, null);

        Set<Integer> depInc101Ids = new HashSet<Integer>();
        for (Map<String, RefBookValue> e : result101) {
            depInc101Ids.add(e.get("DEPARTMENT_ID").getReferenceValue().intValue());
        }

        for (Integer id : depInc101Ids) {
            logs.add(new LogEntry(LogLevel.ERROR, "Форма 101 (бухгалтерская отчётность) в подразделении \"" +
                    departmentService.getDepartment(id).getName() +
                    "\" находится в удаляемом периоде!"));
        }

        //Check INCOME_102
		dataProvider = rbFactory.getDataProvider(REF_BOOK_102);
		PagingResult<Map<String, RefBookValue>> result102 =  dataProvider.getRecords(endDate, null, depFilter, null);
        Set<Integer> depInc102Ids = new HashSet<Integer>();
        for (Map<String, RefBookValue> e : result102) {
            depInc102Ids.add(e.get("DEPARTMENT_ID").getReferenceValue().intValue());
        }

        for (Integer id : depInc102Ids) {
            logs.add(new LogEntry(LogLevel.ERROR, "Форма 102 (бухгалтерская отчётность) в подразделении \"" +
                    departmentService.getDepartment(id).getName() +
                    "\" находится в удаляемом периоде!"));
        }

        //Check forms
        List<FormData> formDatas = formDataService.find(departmentIds, action.getReportPeriodId());
        for (FormData fd : formDatas) {
            logs.add(new LogEntry(LogLevel.ERROR, "Форма " + fd.getFormType().getName() + " " + fd.getKind().getName() +
                    " в подразделении " + departmentService.getDepartment(fd.getDepartmentId()).getName() + " находится в удаляемом периоде!"));
        }


        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentIds);
        filter.setReportPeriodIds(Collections.singletonList(action.getReportPeriodId()));
        List<Long> declarations = declarationDataSearchService.getDeclarationIds(filter, DeclarationDataSearchOrdering.ID, true);
        for (Long id : declarations) {
            DeclarationData dd = declarationDataService.get(id, user);
            DeclarationTemplate dt = declarationService.get(dd.getDeclarationTemplateId());
            logs.add(new LogEntry(LogLevel.ERROR, dt.getType().getName() + " в подразделении " +
                    departmentService.getDepartment(dd.getDepartmentId()).getName() + " находится в удаляемом периоде!"));
        }
		result.setCanRemove(result101.isEmpty() && result102.isEmpty() && formDatas.isEmpty() && declarations.isEmpty());
        result.setUuid(logEntryService.save(logs));
		return result;

	}

    private String buildFilter(List<Integer> departments, long reportPeriodId) {
        if ((departments == null) || departments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("(");
        for (Integer dep : departments) {
            sb.append("DEPARTMENT_ID=" + dep + " or ");
        }

        sb.delete(sb.length() - 4, sb.length() - 1);
        sb.append(")");
        sb.append(" and REPORT_PERIOD_ID=" + reportPeriodId);
        return sb.toString();
    }

	@Override
	public void undo(CanRemovePeriodAction canRemovePeriodAction, CanRemovePeriodResult canRemovePeriodResult, ExecutionContext executionContext) throws ActionException {
	}
}
