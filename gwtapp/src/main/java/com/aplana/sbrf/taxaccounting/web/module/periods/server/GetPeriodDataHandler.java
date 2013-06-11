package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


//@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
@Service
public class GetPeriodDataHandler extends AbstractActionHandler<GetPeriodDataAction, GetPeriodDataResult> {

	@Autowired
	private ReportPeriodService reportPeriodService;

	@Autowired
	TaxPeriodDao taxPeriodDao;

	public GetPeriodDataHandler() {
		super(GetPeriodDataAction.class);
	}

	@Override
	public GetPeriodDataResult execute(GetPeriodDataAction action, ExecutionContext executionContext) throws ActionException {
		GetPeriodDataResult res = new GetPeriodDataResult();
		GregorianCalendar from = new GregorianCalendar(action.getFrom(), 1, 1);
		GregorianCalendar to = new GregorianCalendar(action.getTo(), 1, 1);
		List<TaxPeriod> taxPeriods = taxPeriodDao.listByTaxTypeAndDate(action.getTaxType(), from.getTime(), to.getTime());
		Map<TaxPeriod, List<ReportPeriod>> periods = new HashMap<TaxPeriod, List<ReportPeriod>>();
		for (TaxPeriod taxPeriod : taxPeriods) {
			periods.put(taxPeriod,
					reportPeriodService.listByTaxPeriod(taxPeriod.getId()));
		}
		List<TableRow> rows = new ArrayList<TableRow>();
		for(TaxPeriod taxPeriod : periods.keySet()) {
			TableRow rowDate = new TableRow();
			rowDate.setPeriodName(taxPeriod.getStartDate().toString());
			rowDate.setPeriodCondition(true);
			rowDate.setPeriodKind("Налоговый"); //TODO
			rows.add(rowDate);
			for (ReportPeriod reportPeriod : periods.get(taxPeriod)) {
				TableRow row = new TableRow();
				row.setId(reportPeriod.getId());
				row.setPeriodName(reportPeriod.getName());
				row.setPeriodCondition(true);
				row.setPeriodKind("Отчетный"); //TODO
				rows.add(row);
			}
		}
		res.setRows(rows);

		return res;
	}

	@Override
	public void undo(GetPeriodDataAction getPeriodDataAction, GetPeriodDataResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
