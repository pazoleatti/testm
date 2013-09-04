package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
@Service
public class GetPeriodDataHandler extends AbstractActionHandler<GetPeriodDataAction, GetPeriodDataResult> {

	@Autowired
	private PeriodService reportPeriodService;


	@Autowired
	DepartmentService departmentService;

	public GetPeriodDataHandler() {
		super(GetPeriodDataAction.class);
	}

	@Override
	public GetPeriodDataResult execute(GetPeriodDataAction action, ExecutionContext executionContext) throws ActionException {
		GetPeriodDataResult res = new GetPeriodDataResult();
		//TODO Перенести фильтрацию по датам в сервисы
		GregorianCalendar from = new GregorianCalendar(action.getFrom(), Calendar.JANUARY, 1);
		GregorianCalendar to = new GregorianCalendar(action.getTo(), Calendar.DECEMBER, 31);
		List<DepartmentReportPeriod> reportPeriods = reportPeriodService.listByDepartmentId(action.getDepartmentId());
		Map<Integer, List<TableRow>> per = new TreeMap<Integer, List<TableRow>>();
		for (DepartmentReportPeriod period : reportPeriods) {
			TaxPeriod taxPeriod = period.getReportPeriod().getTaxPeriod();
			if ((taxPeriod.getStartDate().after(from.getTime()) || taxPeriod.getStartDate().equals(from.getTime()))
					&& (taxPeriod.getStartDate().before(to.getTime()) || taxPeriod.getStartDate().equals(to.getTime()))
					&& (taxPeriod.getTaxType() == action.getTaxType())) {
				if (per.get(taxPeriod.getStartDate().getYear()) == null) {
					List<TableRow> tableRows = new ArrayList<TableRow>();
					per.put(taxPeriod.getStartDate().getYear(), tableRows);
				}
				TableRow row = new TableRow();
				row.setPeriodName(period.getReportPeriod().getName());
				row.setPeriodCondition(period.isActive());
				row.setDepartmentId(action.getDepartmentId());
				row.setReportPeriodId(period.getReportPeriod().getId());
				row.setSubHeader(false);
				per.get(taxPeriod.getStartDate().getYear()).add(row);
			}
		}
		List<TableRow> resultRows = new ArrayList<TableRow>();
		for (Map.Entry<Integer, List<TableRow>> rec : per.entrySet()) {
			TableRow header = new TableRow();
			header.setPeriodName("Календарный год " + (rec.getKey()+1900));
			header.setSubHeader(true);
			resultRows.add(header);
			resultRows.addAll(rec.getValue());
		}
		res.setRows(resultRows);

		return res;
	}

	@Override
	public void undo(GetPeriodDataAction getPeriodDataAction, GetPeriodDataResult getPeriodDataResult, ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
