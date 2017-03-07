package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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

import java.util.*;

import static java.util.Arrays.asList;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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

    @Autowired
    private RefBookFactory rbFactory;

	@Override
	public GetDeclarationListResult execute(GetDeclarationList action, ExecutionContext executionContext) throws ActionException {
		if (action == null || action.getDeclarationFilter() == null){
			return null;
		}

        boolean wasEmpty = false;
        TaxType taxType = action.getDeclarationFilter().getTaxType();

        TAUser currentUser = securityService.currentUserInfo().getUser();

        if (action.getDeclarationFilter().getDepartmentIds() == null || action.getDeclarationFilter().getDepartmentIds().isEmpty()) {
            action.getDeclarationFilter().setDepartmentIds(departmentService.getTaxFormDepartments(
                    currentUser, action.getDeclarationFilter().getTaxType(), null, null));
            wasEmpty = true;
        }

        List<Long> availableDeclarationFormKindIds = new ArrayList<Long>();
        for(DeclarationFormKind declarationFormKind: GetDeclarationFilterDataHandler.getAvailableDeclarationFormKind(taxType, action.isReports(), currentUser)) {
            availableDeclarationFormKindIds.add(declarationFormKind.getId());
        }
        if (action.getDeclarationFilter().getFormKindIds() != null && !action.getDeclarationFilter().getFormKindIds().isEmpty()) {
            availableDeclarationFormKindIds.retainAll(action.getDeclarationFilter().getFormKindIds());
        }
        action.getDeclarationFilter().setFormKindIds(availableDeclarationFormKindIds);

        // Для всех пользователей, кроме пользователей с ролью "Контролер УНП" происходит принудительная фильтрация
        // деклараций по подразделениям
        if (!currentUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)
                && !wasEmpty) {
            // Список доступных подразделений
            List<Integer> availableList = departmentService.getTaxFormDepartments(
                    currentUser, action.getDeclarationFilter().getTaxType(), null, null);

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
                if ((startIndex/countOfRecords) != result.getPage()) {
                    return result;
                }
            }
        }

        action.getDeclarationFilter().setAsnuIds(currentUser.getAsnuIds());

        if (taxType.equals(TaxType.NDFL) && !currentUser.hasRole(TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRole(TARole.N_ROLE_OPER) ||
                taxType.equals(TaxType.PFR) && !currentUser.hasRole(TARole.F_ROLE_CONTROL_UNP) && currentUser.hasRole(TARole.F_ROLE_OPER)) {
            action.getDeclarationFilter().setUserDepartmentId(currentUser.getDepartmentId());
        }

		PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(action.getDeclarationFilter());
        Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();
        Map<Long, String> asnuNames = new HashMap<Long, String>();
        RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
        for(DeclarationDataSearchResultItem item: page) {
            if (departmentFullNames.get(item.getDepartmentId()) == null) {
                departmentFullNames.put(item.getDepartmentId(), departmentService.getParentsHierarchyShortNames(item.getDepartmentId()));
            }
            if (item.getAsnuId() != null && !asnuNames.containsKey(item.getAsnuId())) {
                asnuNames.put(item.getAsnuId(), asnuProvider.getRecordData(item.getAsnuId()).get("NAME").getStringValue());
            }
        }
		result.setRecords(page);
        result.setDepartmentFullNames(departmentFullNames);
        result.setAsnuNames(asnuNames);
        result.setTotalCountOfRecords(page.getTotalCount());
		return result;
	}

	@Override
	public void undo(GetDeclarationList getDeclarationList, GetDeclarationListResult getDeclarationListResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
