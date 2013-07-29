package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.dao.DictionaryTaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class PeriodsGetFilterDataHandler extends AbstractActionHandler<PeriodsGetFilterData, PeriodsGetFilterDataResult> {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDataSearchService formDataSearchService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
	@Autowired
	TaxPeriodDao taxPeriodDao;
	@Autowired
	DictionaryTaxPeriodDao dictionaryTaxPeriodDao;
	@Autowired
	TAUserService userService;

    public PeriodsGetFilterDataHandler() {
        super(PeriodsGetFilterData.class);
    }

    @Override
    public PeriodsGetFilterDataResult execute(PeriodsGetFilterData action, ExecutionContext executionContext) throws ActionException {
	    PeriodsGetFilterDataResult res = new PeriodsGetFilterDataResult();
	    TAUserInfo userInfo = securityService.currentUserInfo();
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(userInfo, action.getTaxType());
	    if(filterValues.getDepartmentIds() == null) {
		    //Контролер УНП
		    res.setDepartments(departmentService.listAll());
	    } else {
		    //Контролер или Оператор
		    res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(filterValues
				    .getDepartmentIds()).values()));
	    }
	    res.setFilterValues(filterValues);
	    res.setDictionaryTaxPeriods(dictionaryTaxPeriodDao.getByTaxType(action.getTaxType()));
	    res.setCurrentReportPeriod(getCurrentReportPeriod(action.getTaxType()));
	    TaxPeriod lastTaxType = taxPeriodDao.getLast(action.getTaxType());

		if (lastTaxType == null) {
			Calendar current = Calendar.getInstance();
			res.setYearFrom(current.get(Calendar.YEAR) - 1900);
			res.setYearTo(current.get(Calendar.YEAR) - 1900);
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lastTaxType.getStartDate());
			res.setYearFrom(calendar.get(Calendar.YEAR));
			calendar.setTime(lastTaxType.getEndDate());
			res.setYearTo(calendar.get(Calendar.YEAR));
		}
	    TaxType taxType = action.getTaxType();

	    if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT)) {
		    res.setSelectedDepartment(departmentService.getDepartmentBySbrfCode("99006200")); //УНП
	    } else if ((taxType == TaxType.TRANSPORT) || (taxType == TaxType.PROPERTY)) {
			res.setSelectedDepartment(departmentService.getDepartment(userInfo.getUser().getDepartmentId()));
	    }
	    Calendar current = Calendar.getInstance();
	    res.setCurrentYear(current.get(Calendar.YEAR));

        return res;
    }

    @Override
    public void undo(PeriodsGetFilterData getFilterData, PeriodsGetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }

	private ReportPeriod getCurrentReportPeriod(TaxType taxType){
		try {
			ReportPeriod rp = reportPeriodDao.getCurrentPeriod(taxType);
			if (rp != null) {
				return rp;
			}
		} catch (DaoException e) {
			logger.warn("Failed to find current report period for taxType = " + taxType + ", message is: " + e.getMessage());
		}
		return null;
	}
}
