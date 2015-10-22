package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
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

        TaxType taxType = action.getTaxType();
	    List<Department> departments = new ArrayList<Department>();
        Set<Integer> ad = new HashSet<Integer>();
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


                    Collections.sort(departments, new Comparator<Department>() {
                        @Override
                        public int compare(Department o1, Department o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    for (Department dep : departments) {
                        if (dep.getType() != DepartmentType.ROOT_BANK) {
                            ad.add(dep.getId());
                        }
                    }
                    res.setAvalDepartments(ad);
                    Department userDepartmentTB = departmentService.getTBUserDepartments(userInfo.getUser()).get(0);
                    res.setSelectedDepartment(new DepartmentPair(userDepartmentTB.getId(), userDepartmentTB.getParentId(), userDepartmentTB.getName()));
                    break;
                case INCOME:
                case DEAL:
                case VAT:
                case ETR:
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
                case ETR: //TODO
			        res.setCanEdit(false);
			        break;
	        }
        }
	    if (ad.isEmpty()) { // default all available
            for (Department dep : res.getDepartments()) {
                ad.add(dep.getId());
            }
        }
	    res.setAvalDepartments(ad);
        // TODO Левыкин: указанный метод всегда возвращает null!
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
