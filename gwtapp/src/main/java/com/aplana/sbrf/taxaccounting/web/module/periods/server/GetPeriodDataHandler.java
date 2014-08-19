package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Component
public class GetPeriodDataHandler extends AbstractActionHandler<GetPeriodDataAction, GetPeriodDataResult> {

	@Autowired
	private	PeriodService periodService;

	@Autowired
	private DepartmentService departmentService;

    @Autowired
    private NotificationService notificationService;

	public GetPeriodDataHandler() {
		super(GetPeriodDataAction.class);
	}

	@Override
	public GetPeriodDataResult execute(GetPeriodDataAction action, ExecutionContext executionContext) throws ActionException {
		GetPeriodDataResult res = new GetPeriodDataResult();


		Map<Integer, List<TableRow>> per = new TreeMap<Integer, List<TableRow>>();
		List<DepartmentReportPeriod> drp = periodService.listByDepartmentIdAndTaxType(action.getDepartmentId(), action.getTaxType());
		List<Integer> depIds = new ArrayList<Integer>();
		for (DepartmentReportPeriod d : drp) {
			depIds.add(d.getDepartmentId().intValue());
		}
		Map<Integer, Department> departmentMap = departmentService.getDepartments(depIds);

		for (DepartmentReportPeriod period : drp) {
			int year = period.getReportPeriod().getTaxPeriod().getYear();
			if ((action.getFrom() <= year) && (year <= action.getTo())) {
				if (per.get(year) == null) {
					per.put(year, new ArrayList<TableRow>());
				}
				TableRow row = new TableRow();
				row.setPeriodName(period.getReportPeriod().getName());
				row.setReportPeriodId(period.getReportPeriod().getId());
                row.setDictTaxPeriodId(period.getReportPeriod().getDictTaxPeriodId());
				row.setDepartmentId(period.getDepartmentId());
				row.setPeriodCondition(period.isActive());
				row.setBalance(period.isBalance());
				row.setYear(year);
                row.setOrd(period.getReportPeriod().getOrder());
				row.setCorrectPeriod(period.getCorrectPeriod());
				Department dep = departmentMap.get(period.getDepartmentId().intValue());
				Notification notification = notificationService.get(period.getReportPeriod().getId(), dep.getId(), dep.getParentId());
				row.setDeadline(notification != null ? notification.getDeadline() : null);
				per.get(year).add(row);
			}
		}
		List<TableRow> rows = new ArrayList<TableRow>();
		for (Map.Entry<Integer, List<TableRow>> rec : per.entrySet()) {
			TableRow header = new TableRow();
			header.setPeriodName("Календарный год " + (rec.getKey()));
			header.setSubHeader(true);
			rows.add(header);

            List<TableRow> sortedRows = rec.getValue();
            Collections.sort(rec.getValue(), new RowComparator());
            rows.addAll(sortedRows);
		}
		res.setRows(rows);

		return res;
	}

    class RowComparator implements Comparator<TableRow> {
        @Override
        public int compare(TableRow a, TableRow b) {
            if ((a.getCorrectPeriod() == null) && (b.getCorrectPeriod() == null)) {
                return (a.getOrd() > b.getOrd() ? 1 : -1);
            }
            if (a.getCorrectPeriod() == null) {
                return -1;
            }
            if (b.getCorrectPeriod() == null) {
                return 1;
            }
            return a.getCorrectPeriod().compareTo(b.getCorrectPeriod());
        }
    }

	@Override
	public void undo(GetPeriodDataAction getPeriodDataAction, GetPeriodDataResult getPeriodDataResult,
                     ExecutionContext executionContext) throws ActionException {
		//ничего не делаем
	}
}
