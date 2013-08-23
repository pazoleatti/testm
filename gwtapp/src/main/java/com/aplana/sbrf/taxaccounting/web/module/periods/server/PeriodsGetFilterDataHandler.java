package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.dao.DictionaryTaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class PeriodsGetFilterDataHandler extends AbstractActionHandler<PeriodsGetFilterData, PeriodsGetFilterDataResult> {

	private Log logger = LogFactory.getLog(getClass());
	public static final long DICT_ID = 8L;
	
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
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(userInfo, action.getTaxType());
	    RefBookDataProvider refBookDataProvider = refBookFactory
			    .getDataProvider(DICT_ID);
	    PagingResult<Map<String, RefBookValue>> result = refBookDataProvider.getRecords(new Date(), null,
			    action.getTaxType().getCode()+"=1", null);
	    if(filterValues.getDepartmentIds() == null) {
		    //Контролер УНП
		    res.setDepartments(departmentService.listAll());
	    } else {
		    //Контролер или Оператор
		    res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(filterValues
				    .getDepartmentIds()).values()));
	    }
	    res.setFilterValues(filterValues);
	    res.setDictionaryTaxPeriods(convert(result));
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
			res.setYearTo(calendar.get(Calendar.YEAR));
		}
	    TaxType taxType = action.getTaxType();

	    if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT)) {
		    res.setSelectedDepartment(departmentService.getDepartmentBySbrfCode("99006200")); //УНП
		    res.setEnableDepartmentPicker(false);
	    } else if ((taxType == TaxType.TRANSPORT) || (taxType == TaxType.PROPERTY)) {
			res.setSelectedDepartment(departmentService.getDepartment(userInfo.getUser().getDepartmentId()));
		    res.setEnableDepartmentPicker(true);
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

	private List<DictionaryTaxPeriod> convert(PagingResult<Map<String, RefBookValue>> values) {
		List<DictionaryTaxPeriod> result = new ArrayList<DictionaryTaxPeriod>();
		for(Map<String, RefBookValue> rec : values.getRecords()) {
			DictionaryTaxPeriod r = new DictionaryTaxPeriod();
			r.setName(rec.get("NAME").getStringValue());
			r.setCode(Integer.parseInt(rec.get("CODE").getStringValue()));
			result.add(r);
		}
		return result;
	}
}
