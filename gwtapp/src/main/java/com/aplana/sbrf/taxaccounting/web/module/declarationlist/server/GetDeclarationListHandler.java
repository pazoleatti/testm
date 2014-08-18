package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationList;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDeclarationListHandler extends AbstractActionHandler<GetDeclarationList, GetDeclarationListResult> {

	public GetDeclarationListHandler() {
		super(GetDeclarationList.class);
	}

	@Autowired
	private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

	@Override
	public GetDeclarationListResult execute(GetDeclarationList action, ExecutionContext executionContext) throws ActionException {
		if(action == null || action.getDeclarationFilter() == null){
			return null;
		}

        boolean wasEmpty = false;

        if (action.getDeclarationFilter().getDepartmentIds() == null || action.getDeclarationFilter().getDepartmentIds().isEmpty()) {
            action.getDeclarationFilter().setDepartmentIds(departmentService.getTaxFormDepartments(
                    securityService.currentUserInfo().getUser(), asList(action.getDeclarationFilter().getTaxType()), null, null));
            wasEmpty = true;
        }

        // Для всех пользователей, кроме пользователей с ролью "Контролер УНП" происходит принудительная фильтрация
        // деклараций по подразделениям
        if (!securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP) && !wasEmpty) {
            // Список доступных подразделений
            List<Integer> availableList = departmentService.getTaxFormDepartments(
                    securityService.currentUserInfo().getUser(), asList(action.getDeclarationFilter().getTaxType()), null, null);

            // Если пользовательская фильтрация не задана, то выбираем по всем доступным подразделениям
            // Если пользовательская фильтрация задана, то выбираем по всем доступным подразделениям,
            // которые указал пользователь
            if (action.getDeclarationFilter().getDepartmentIds() != null
                    && !action.getDeclarationFilter().getDepartmentIds().isEmpty()) {
                availableList.retainAll(action.getDeclarationFilter().getDepartmentIds());
            }
            action.getDeclarationFilter().setDepartmentIds(availableList);
        }

        GetDeclarationListResult result = new GetDeclarationListResult();

        if (action.getDeclarationFilter().getDeclarationDataId() != null) {
            Long rowNum = declarationDataSearchService
                    .getRowNumByFilter(action.getDeclarationFilter());

            if (rowNum != null) {
                rowNum = rowNum - 1;
                int countOfRecords = action.getDeclarationFilter().getCountOfRecords();
                int startIndex = action.getDeclarationFilter().getStartIndex();
                result.setPage((int)(rowNum/countOfRecords));
                if (((int)startIndex/countOfRecords) != result.getPage()) {
                    return result;
                }
            }
        }

		PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(action.getDeclarationFilter());
        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        for(DeclarationDataSearchResultItem item: page) {
            if (departmentFullNames.get(item.getDepartmentId()) == null) departmentFullNames.put(item.getDepartmentId(), departmentService.getParentsHierarchyShortNames(item.getDepartmentId()));
        }
		result.setRecords(page);
        result.setDepartmentFullNames(departmentFullNames);
		result.setTotalCountOfRecords(page.getTotalCount());
		return result;
	}

	@Override
	public void undo(GetDeclarationList getDeclarationList, GetDeclarationListResult getDeclarationListResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
