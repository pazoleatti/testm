package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class PeriodsGetFilterDataHandler extends AbstractActionHandler<PeriodsGetFilterData, PeriodsGetFilterDataResult> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private PeriodService reportPeriodService;
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
//	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(userInfo, action.getTaxType());


//	    res.setAvalDepartments(filterValues.getDepartmentIds());


        TaxType taxType = action.getTaxType();
	    List<Department> departments = new ArrayList<Department>();
        if (userInfo.getUser().hasRole("ROLE_CONTROL_UNP")) {
	        res.setCanEdit(true);
            switch (taxType) {
                case PROPERTY:
                case TRANSPORT:
                    res.setCanChangeDepartment(true);
	                departments.addAll(departmentService.getTBDepartments(userInfo.getUser()));
	                Set<Integer> depIds = new HashSet<Integer>();
	                for (Department dep : departments) {
		                depIds.add(dep.getId());
	                }
	                res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(depIds).values()));
	                Department bank = departmentService.getBankDepartment();
	                res.setSelectedDepartment(new DepartmentPair(bank.getId(), bank.getParentId(), bank.getName()));
                    break;
                case INCOME:
                case DEAL:
                case VAT:
                    res.setCanChangeDepartment(false);
	                res.setDepartments(Arrays.asList(departmentService.getBankDepartment()));
	                res.setSelectedDepartment(
			                new DepartmentPair(res.getDepartments().get(0).getId(),
					                res.getDepartments().get(0).getParentId(),
					                res.getDepartments().get(0).getName())
	                );
                    break;
                default:
                    break;
            }
        } else { // Контролер НС
	        res.setCanChangeDepartment(false);
	        res.setDepartments(departmentService.getTBDepartments(userInfo.getUser()));
	        res.setSelectedDepartment(
			        new DepartmentPair(res.getDepartments().get(0).getId(),
					        res.getDepartments().get(0).getParentId(),
					        res.getDepartments().get(0).getName())
	        );
	        switch (taxType) {
		        case PROPERTY:
		        case TRANSPORT:
			        res.setCanEdit(true);
			        break;
		        case INCOME:
		        case DEAL:
		        case VAT:
			        res.setCanEdit(false);
			        break;
	        }
        }
	    Set<Integer> ad = new HashSet<Integer>();
	    for (Department dep : res.getDepartments()) {
		    ad.add(dep.getId());
	    }
	    res.setAvalDepartments(ad);

	    DepartmentReportPeriod rp = reportPeriodService.getLastReportPeriod(taxType, action.getDepartmentId());
	    Calendar current = Calendar.getInstance();
	    if (rp != null) {
		    res.setYearFrom(rp.getReportPeriod().getTaxPeriod().getYear());
		    res.setYearTo(rp.getReportPeriod().getTaxPeriod().getYear());
		    res.setCurrentYear(current.get(Calendar.YEAR));
	    } else {
		    res.setYearFrom(current.get(Calendar.YEAR));
		    res.setYearTo(current.get(Calendar.YEAR));
		    res.setCurrentYear(current.get(Calendar.YEAR));
	    }

        return res;
    }

    @Override
    public void undo(PeriodsGetFilterData getFilterData, PeriodsGetFilterDataResult getFilterDataResult,
                     ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
