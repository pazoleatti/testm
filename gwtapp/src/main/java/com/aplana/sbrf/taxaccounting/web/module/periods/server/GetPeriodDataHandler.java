package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class GetPeriodDataHandler extends AbstractActionHandler<GetPeriodDataAction, GetPeriodDataResult> {

	@Autowired
	private DepartmentService departmentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

	public GetPeriodDataHandler() {
		super(GetPeriodDataAction.class);
	}

	@Override
	public GetPeriodDataResult execute(GetPeriodDataAction action, ExecutionContext executionContext) throws ActionException {
		GetPeriodDataResult res = new GetPeriodDataResult();

		Map<Integer, List<TableRow>> per = new TreeMap<Integer, List<TableRow>>();
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(action.getDepartmentId()));
        filter.setTaxTypeList(Arrays.asList(action.getTaxType()));
        filter.setYearStart(action.getFrom());
        filter.setYearEnd(action.getTo());

		List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
		List<Integer> depIds = new ArrayList<Integer>();
		for (DepartmentReportPeriod d : drpList) {
			depIds.add(d.getDepartmentId());
		}
		Map<Integer, Department> departmentMap = departmentService.getDepartments(depIds);

		for (DepartmentReportPeriod drp : drpList) {
			int year = drp.getReportPeriod().getTaxPeriod().getYear();
            if (per.get(year) == null) {
                per.put(year, new ArrayList<TableRow>());
            }
            TableRow row = new TableRow();
            row.setPeriodName(drp.getReportPeriod().getName());
            row.setReportPeriodId(drp.getReportPeriod().getId());
            row.setDictTaxPeriodId(drp.getReportPeriod().getDictTaxPeriodId());
            row.setDepartmentId(drp.getDepartmentId());
            row.setPeriodCondition(drp.isActive());
            row.setBalance(drp.isBalance());
            row.setYear(year);
            row.setOrd(drp.getReportPeriod().getOrder());
            row.setCorrectPeriod(drp.getCorrectionDate());
            Department dep = departmentMap.get(drp.getDepartmentId());
            Notification notification = notificationService.get(drp.getReportPeriod().getId(), null, dep.getId());
            row.setDeadline(notification != null ? notification.getDeadline() : null);
            row.setDepartmentReportPeriodId(drp.getId());
            per.get(year).add(row);
		}
		List<TableRow> rows = new ArrayList<TableRow>();
        //TODO avanteev: Подумать, может имеет смысл перенести в запрос
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
