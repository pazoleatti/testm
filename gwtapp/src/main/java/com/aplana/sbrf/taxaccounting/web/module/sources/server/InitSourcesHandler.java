package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.InitSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.InitSourcesResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class InitSourcesHandler extends AbstractActionHandler<InitSourcesAction, InitSourcesResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DepartmentService departmentService;

    @Autowired
    private RefBookFactory rbFactory;

    private static final Long PERIOD_CODE_REFBOOK = RefBook.Id.PERIOD_CODE.getId();

	public InitSourcesHandler() {
		super(InitSourcesAction.class);
	}

	@Override
	public InitSourcesResult execute(InitSourcesAction action, ExecutionContext context) throws ActionException {
		InitSourcesResult result = new InitSourcesResult();
		TAUserInfo userInfo = securityService.currentUserInfo();
        // http://conf.aplana.com/pages/viewpage.action?pageId=11380675
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            result.setControlUNP(true);
			result.setDepartments(departmentService.listDepartments());
            Set<Integer> availableDepartments = new HashSet<Integer>();
            for (Department dep : result.getDepartments()) {
                availableDepartments.add(dep.getId());
            }
            result.setAvailableDepartments(availableDepartments);
        } else if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
            result.setControlUNP(false);
			Set<Integer> availableDepartments = new HashSet<Integer>();
            for (Department dep : departmentService.getBADepartments(userInfo.getUser())) {
                availableDepartments.add(dep.getId());
            }
			result.setDepartments(new ArrayList<Department>(departmentService
					.getRequiredForTreeDepartments(availableDepartments)
					.values()));
			result.setAvailableDepartments(availableDepartments);
		}
        result.setDefaultDepartment(userInfo.getUser().getDepartmentId());

        /** Получение информации по периодам из справочника Коды, определяющие налоговый (отчётный) период*/
        RefBook refBook = rbFactory.get(PERIOD_CODE_REFBOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());

        String filter = action.getTaxType().getCode() + " = 1";
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(new Date(), null, filter ,null);
        List<PeriodInfo> periods = new ArrayList<PeriodInfo>();
        for (Map<String, RefBookValue> record : records) {
            if (record.get("NAME").getStringValue() == null
                    || record.get("END_DATE").getDateValue() == null
                    || record.get("CALENDAR_START_DATE").getDateValue() == null) {
                throw new ServiceException("Не заполнен один из обязательных атрибутов справочника \"" + refBook.getName() + "\"");
            }
            PeriodInfo period = new PeriodInfo();
            period.setName(record.get("NAME").getStringValue());
            period.setCode(record.get("CODE").getStringValue());
            period.setStartDate(record.get("CALENDAR_START_DATE").getDateValue());
            period.setEndDate(record.get("END_DATE").getDateValue());
            periods.add(period);
        }
        result.setPeriods(periods);

        result.setYear(Calendar.getInstance().get(Calendar.YEAR));
		return result;
	}

	@Override
	public void undo(InitSourcesAction action, InitSourcesResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
