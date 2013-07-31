package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.dao.DictionaryTaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;
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

	public OpenPeriodHandler() {
		super(OpenPeriodAction.class);
	}

	@Override
	public OpenPeriodResult execute(OpenPeriodAction action, ExecutionContext executionContext) throws ActionException {
		OpenPeriodResult result = new OpenPeriodResult();
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, action.getYear());
		date.set(Calendar.MONTH, Calendar.JANUARY);
		date.set(Calendar.DAY_OF_MONTH, 1);
		List<TaxPeriod> taxPeriodList = taxPeriodService.listByTaxTypeAndDate(action.getTaxType(), date.getTime(), date.getTime());
		List<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>();
		for (TaxPeriod taxPeriod : taxPeriodList) {
			reportPeriods.addAll(reportPeriodService.listByTaxPeriod(taxPeriod.getId()));
		}
		List<DictionaryTaxPeriod> dictionaryTaxPeriods = new ArrayList<DictionaryTaxPeriod>();
		dictionaryTaxPeriods.add(dictionaryTaxPeriodDao.get(action.getDictionaryTaxPeriodId()));
		if (taxPeriodList.isEmpty() && reportPeriods.isEmpty()) {
			// Нет такого налогового периода
			TaxPeriod newTaxPeriod = new TaxPeriod();
			newTaxPeriod.setStartDate(date.getTime());
			newTaxPeriod.setTaxType(action.getTaxType());
			newTaxPeriod.setEndDate(action.getEndDate());

			newTaxPeriod.setDictionaryTaxPeriod(dictionaryTaxPeriods);
			int newTaxPeriodId = taxPeriodService.add(newTaxPeriod);

			ReportPeriod newReportPeriod = new ReportPeriod();
			newReportPeriod.setBalancePeriod(action.isBalancePeriod());
			newReportPeriod.setDepartmentId(action.getDepartmentId());
			newReportPeriod.setName(date.get(Calendar.YEAR) + " - " + dictionaryTaxPeriods.get(0).getName());
			newReportPeriod.setTaxPeriodId(newTaxPeriodId);
			newReportPeriod.setActive(action.isActive());
			newReportPeriod.setMonths(action.getMonths());
			newReportPeriod.setOrder(1);
			newReportPeriod.setDictTaxPeriodId(action.getDictionaryTaxPeriodId());
			reportPeriodService.add(newReportPeriod);

		} else {
			if (!reportPeriods.isEmpty() && (reportPeriods.size()>0) && (reportPeriods.size()<=4) &&
					((findPeriodInListByPeriodDict(reportPeriods, action.getDictionaryTaxPeriodId()) == null))) {
				if (reportPeriods.get(reportPeriods.size()-1).isActive()) {
					throw new OpenException(OpenException.ErrorCode.PREVIOUS_ACTIVE, "Отчетный период не может быть открыт, так как еще не сформирован предыдущий отчетный период");
				} else {
					ReportPeriod newReportPeriod = new ReportPeriod();
					newReportPeriod.setBalancePeriod(action.isBalancePeriod());
					newReportPeriod.setDepartmentId(action.getDepartmentId());
					newReportPeriod.setName(date.get(Calendar.YEAR) + " - " + dictionaryTaxPeriods.get(0).getName());
					newReportPeriod.setTaxPeriodId(taxPeriodList.get(taxPeriodList.size()-1).getId());
					newReportPeriod.setActive(action.isActive());
					newReportPeriod.setMonths(action.getMonths());
//					newReportPeriod.setOrder(taxPeriodList.get(taxPeriodList.size()-1).get);
					newReportPeriod.setDictTaxPeriodId(action.getDictionaryTaxPeriodId());
					reportPeriodService.add(newReportPeriod);

				}
			} else if (!reportPeriods.isEmpty() && (findPeriodInListByPeriodDict(reportPeriods, action.getDictionaryTaxPeriodId()).isActive())) {//7a
				throw new OpenException(OpenException.ErrorCode.EXIST_OPEN, "Указанный период уже открыт!");
			} else if (!reportPeriods.isEmpty()) {
				ReportPeriod existPeriod = findPeriodInListByPeriodDict(reportPeriods, action.getDictionaryTaxPeriodId());
				if (!(existPeriod.isActive())) {
					OpenException exception = new OpenException(OpenException.ErrorCode.EXIST_CLOSED, "Указанный период уже заведён в Системе и находится в состоянии \"Закрыт\"! Переоткрыть указанный период?");
					exception.setReportPeriodId(existPeriod.getId());
					throw exception;
				}
			}

		}
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
