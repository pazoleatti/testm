package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.ArrayList;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class PeriodsGetFilterDataHandler extends AbstractActionHandler<PeriodsGetFilterData, PeriodsGetFilterDataResult> {

	public static final long DICT_ID = 8L;
	
	@Autowired
	private FormDataSearchService formDataSearchService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private ReportPeriodService reportPeriodService;
	@Autowired
	TAUserService userService;
	@Autowired
	RefBookFactory refBookFactory;

    public PeriodsGetFilterDataHandler() {
        super(PeriodsGetFilterData.class);
    }

    @Override
    public PeriodsGetFilterDataResult execute(PeriodsGetFilterData action, ExecutionContext executionContext) throws ActionException {
	    PeriodsGetFilterDataResult res = new PeriodsGetFilterDataResult();
	    TAUserInfo userInfo = securityService.currentUserInfo();
	    res.setTaxType(action.getTaxType());
	    
	    // Используем сервис для инициализации фильтра форм даты (в аналитике ссылка)
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(userInfo, action.getTaxType());


	    res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(filterValues
			    .getDepartmentIds()).values()));
	    res.setAvalDepartments(filterValues.getDepartmentIds());
	    res.setSelectedDepartment(userInfo.getUser().getDepartmentId());

	    // По умолчанию отчетный период не выбран
	    res.setCurrentReportPeriod(null);
	    
	    TaxPeriod lastTaxType = reportPeriodService.getLastTaxPeriod(action.getTaxType());

		if (lastTaxType == null) {
			Calendar current = Calendar.getInstance();
			res.setYearFrom(current.get(Calendar.YEAR));
			res.setYearTo(current.get(Calendar.YEAR));
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lastTaxType.getStartDate());
			res.setYearFrom(calendar.get(Calendar.YEAR));
			res.setYearTo(calendar.get(Calendar.YEAR));
		}
	    TaxType taxType = action.getTaxType();

	    
	    
	    Calendar current = Calendar.getInstance();
	    res.setCurrentYear(current.get(Calendar.YEAR));


	    if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT) || (taxType == TaxType.DEAL)){
	    	//Если контролеру назначено подразделение УНП, то ему доступно открытие периодов для федеральных налогов
	    	res.setReadOnly(!(departmentService.getUNPDepartment().getId() == userInfo.getUser().getDepartmentId()));
	    	res.setEnableDepartmentPicker(false);
	    } else {
	    	res.setReadOnly(false);
	    	res.setEnableDepartmentPicker(true);
    	}
    
        return res;
    }

    @Override
    public void undo(PeriodsGetFilterData getFilterData, PeriodsGetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }

}
