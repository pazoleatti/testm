package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
@Component
public class GetPeriodDataHandler extends AbstractActionHandler<GetPeriodDataAction, GetPeriodDataResult> {

	@Autowired
	private	PeriodService periodService;

	@Autowired
	private DepartmentService departmentService;

	public GetPeriodDataHandler() {
		super(GetPeriodDataAction.class);
	}

	@Override
	public GetPeriodDataResult execute(GetPeriodDataAction action, ExecutionContext executionContext) throws ActionException {
		GetPeriodDataResult res = new GetPeriodDataResult();
		//TODO Перенести фильтрацию по датам в сервисы
		List<DepartmentReportPeriod> reportPeriods = periodService.listByDepartmentId(action.getDepartmentId());
		Collections.sort(reportPeriods, new Comparator<DepartmentReportPeriod>(){
			@Override
			public int compare(DepartmentReportPeriod o1,
					DepartmentReportPeriod o2) {
                return o1.getReportPeriod().getOrder() - o2.getReportPeriod().getOrder();
            }
		});
		Map<Integer, List<TableRow>> per = new TreeMap<Integer, List<TableRow>>();
		for (DepartmentReportPeriod period : reportPeriods) {
			int periodYear = period.getReportPeriod().getYear(); 
			TaxType periodTaxType = period.getReportPeriod().getTaxType();
			if (periodYear >= action.getFrom() && periodYear <= action.getTo() && periodTaxType == action.getTaxType()) {
				if (per.get(periodYear) == null) {
					List<TableRow> tableRows = new ArrayList<TableRow>();
					per.put(periodYear, tableRows);
				}
				TableRow row = new TableRow();
				row.setPeriodName(period.getReportPeriod().getName());
				row.setPeriodCondition(period.isActive());
				row.setDepartmentId(action.getDepartmentId());
				row.setReportPeriodId(period.getReportPeriod().getId());
				row.setSubHeader(false);
                row.setBalance(period.isBalance());
				per.get(periodYear).add(row);
			}
		}
		List<TableRow> resultRows = new ArrayList<TableRow>();
		for (Map.Entry<Integer, List<TableRow>> rec : per.entrySet()) {
			TableRow header = new TableRow();
			header.setPeriodName("Календарный год " + (rec.getKey()));
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
