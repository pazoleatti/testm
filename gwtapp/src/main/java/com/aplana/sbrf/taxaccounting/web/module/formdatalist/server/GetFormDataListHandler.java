package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, GetFormDataListResult> {
    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

	@Autowired
	private FormDataSearchService formDataSearchService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    public GetFormDataListHandler() {
		super(GetFormDataList.class);
	}

	@Override
    public GetFormDataListResult execute(GetFormDataList action, ExecutionContext context) throws ActionException {
        if (action == null || action.getFormDataFilter() == null) {
            return null;
        }
        GetFormDataListResult res = new GetFormDataListResult();

        if (action.getFormDataFilter().getFormDataId() != null) {
            Long rowNum = formDataSearchService
                    .getRowNumByFilter(securityService.currentUserInfo(), action.getFormDataFilter());

            if (rowNum != null) {
                rowNum = rowNum - 1;
                int countOfRecords = action.getFormDataFilter().getCountOfRecords();
                int startIndex = action.getFormDataFilter().getStartIndex();
                res.setPage((int)(rowNum/countOfRecords));
                if ((startIndex /countOfRecords) != res.getPage()) {
                    return res;
                }
            }
        }
        PagingResult<FormDataSearchResultItem> resultPage = formDataSearchService
                .findDataByUserIdAndFilter(securityService.currentUserInfo(), action.getFormDataFilter());
        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        for(FormDataSearchResultItem item: resultPage) {
            if (departmentFullNames.get(item.getDepartmentId()) == null) departmentFullNames.put(item.getDepartmentId(), item.getHierarchicalDepName());

            if (item.isAccruing()) {
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(item.getDepartmentReportPeriodId());
                item.setReportPeriodName(departmentReportPeriod.getReportPeriod().getAccName());
            }

            if (item.getCorrectionDate() != null) {
                item.setReportPeriodName(item.getReportPeriodName() + ", корр. (" + DATE_TIME_FORMAT.get().format(item.getCorrectionDate()) + ")");
            }

            if (item.getComparativePeriodId() != null) {
                DepartmentReportPeriod compPeriod = departmentReportPeriodService.get(item.getComparativePeriodId());
                item.setComparativPeriodName(
                        compPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " +
                                (item.isAccruing() ? compPeriod.getReportPeriod().getAccName() : compPeriod.getReportPeriod().getName()));
                if (compPeriod.getCorrectionDate() != null) {
                    item.setComparativPeriodName(item.getComparativPeriodName() + ", корр. (" + DATE_TIME_FORMAT.get().format(compPeriod.getCorrectionDate()) + ")");
                }
            }
        }
        res.setTotalCountOfRecords(resultPage.getTotalCount());
        res.setRecords(resultPage);
        res.setDepartmentFullNames(departmentFullNames);
        return res;
    }

	@Override
	public void undo(GetFormDataList action, GetFormDataListResult result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
}
