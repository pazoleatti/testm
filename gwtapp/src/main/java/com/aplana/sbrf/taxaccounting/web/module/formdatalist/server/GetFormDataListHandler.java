package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
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

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, GetFormDataListResult> {
	@Autowired
	private FormDataSearchService formDataSearchService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

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
                if (((int)startIndex/countOfRecords) != res.getPage()) {
                    return res;
                }
            }
        }
        PagingResult<FormDataSearchResultItem> resultPage = formDataSearchService
                .findDataByUserIdAndFilter(securityService.currentUserInfo(), action.getFormDataFilter());
        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        for(FormDataSearchResultItem item: resultPage) {
            if (departmentFullNames.get(item.getDepartmentId()) == null) departmentFullNames.put(item.getDepartmentId(), departmentService.getParentsHierarchyShortNames(item.getDepartmentId()));
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
