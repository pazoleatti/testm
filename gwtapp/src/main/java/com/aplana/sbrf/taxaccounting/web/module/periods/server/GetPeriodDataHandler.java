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
		GregorianCalendar from = new GregorianCalendar(action.getFrom(), Calendar.JANUARY, 1);
		GregorianCalendar to = new GregorianCalendar(action.getTo(), Calendar.DECEMBER, 31);
		List<TaxPeriod> taxPeriods = taxPeriodDao.listByTaxTypeAndDate(action.getTaxType(), from.getTime(), to.getTime());
		Map<TaxPeriod, List<ReportPeriod>> periods = new LinkedHashMap<TaxPeriod, List<ReportPeriod>>();
		for (TaxPeriod taxPeriod : taxPeriods) {
			if (action.getDepartmentId() == 0) {
				periods.put(taxPeriod,
						reportPeriodService.listByTaxPeriod(taxPeriod.getId()));
			} else {
				periods.put(taxPeriod,
						reportPeriodService.listByTaxPeriodAndDepartment(taxPeriod.getId(), action.getDepartmentId()));
			}
		}
		List<TableRow> rows = new ArrayList<TableRow>();
		for(TaxPeriod taxPeriod : periods.keySet()) {
			List<ReportPeriod> reportPeriods = periods.get(taxPeriod);
			if (reportPeriods.isEmpty()) {
				continue;
			}
			TableRow rowDate = new TableRow();
			rowDate.setPeriodName(""+(1900 + taxPeriod.getStartDate().getYear())); //TODO
			rowDate.setPeriodCondition(null);
			rowDate.setSubHeader(true);
			rows.add(rowDate);
			for (ReportPeriod reportPeriod : reportPeriods) {
				TableRow row = new TableRow();
				row.setId(reportPeriod.getId());
				row.setPeriodName(reportPeriod.getName());
				row.setPeriodCondition(reportPeriod.isActive());
				row.setSubHeader(false);
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
