package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
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

	    
	    RefBookDataProvider refBookDataProvider = refBookFactory
			    .getDataProvider(DICT_ID);
	    PagingResult<Map<String, RefBookValue>> result = refBookDataProvider.getRecords(new Date(), null,
			    action.getTaxType().getCode()+"=1", null);
	    res.setDictionaryTaxPeriods(convert(result));
	    // По умолчанию отчетный период не выбран
	    res.setCurrentReportPeriod(null);
	    
	    TaxPeriod lastTaxType = reportPeriodService.getLastTaxPeriod(action.getTaxType());

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

	    if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT) || (taxType == TaxType.DEAL)) {
		    res.setSelectedDepartment(departmentService.getDepartmentBySbrfCode("99006200").getId()); //УНП
		    res.setEnableDepartmentPicker(false);
	    } else if ((taxType == TaxType.TRANSPORT) || (taxType == TaxType.PROPERTY)) {
			res.setSelectedDepartment(userInfo.getUser().getDepartmentId());
		    res.setEnableDepartmentPicker(true);
	    }
	    Calendar current = Calendar.getInstance();
	    res.setCurrentYear(current.get(Calendar.YEAR));

	    // Только чтение для НЕ контролера УНП для федеральных налогов
	    if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP) && (taxType == TaxType.INCOME) || (taxType == TaxType.VAT) || (taxType == TaxType.DEAL)){
	    	res.setReadOnly(true);
	    } else {
	    	res.setReadOnly(false);
    	}
    
        return res;
    }

    @Override
    public void undo(PeriodsGetFilterData getFilterData, PeriodsGetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
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
