package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.dao.DictionaryTaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenException;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class OpenPeriodHandler extends AbstractActionHandler<OpenPeriodAction, OpenPeriodResult> {

	@Autowired
	private ReportPeriodService reportPeriodService;
	@Autowired
	private TaxPeriodService taxPeriodService;
	@Autowired
	private DictionaryTaxPeriodDao dictionaryTaxPeriodDao;
	@Autowired
	private SecurityService securityService;

	public OpenPeriodHandler() {
		super(OpenPeriodAction.class);
	}

	@Override
	public OpenPeriodResult execute(OpenPeriodAction action, ExecutionContext executionContext) throws ActionException {
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setMonths(action.getMonths());
		newReportPeriod.setOrder(1);
		newReportPeriod.setDictTaxPeriodId((int) action.getDictionaryTaxPeriodId());

		reportPeriodService.open(newReportPeriod, action.getYear(), (int) action.getDictionaryTaxPeriodId(),
				action.getTaxType(), securityService.currentUserInfo(), action.getDepartmentId());
		OpenPeriodResult result = new OpenPeriodResult();
		return result;
	}

	@Override
	public void undo(OpenPeriodAction getPeriodDataAction, OpenPeriodResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}

	private ReportPeriod findPeriodInListByPeriodDict(List<ReportPeriod> reportPeriods, int dictionaryTaxPeriodId) {
		for (ReportPeriod reportPeriod : reportPeriods) {
			if (reportPeriod.getDictTaxPeriodId() == dictionaryTaxPeriodId) {
				return reportPeriod;
			}
		}
		return null;
	}
}
