package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.dao.DictionaryTaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
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

    public PeriodsGetFilterDataHandler() {
        super(PeriodsGetFilterData.class);
    }

    @Override
    public PeriodsGetFilterDataResult execute(PeriodsGetFilterData action, ExecutionContext executionContext) throws ActionException {
	    PeriodsGetFilterDataResult res = new PeriodsGetFilterDataResult();
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(securityService
			    .currentUserInfo(), action.getTaxType());

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
