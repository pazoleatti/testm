package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDeclarationFilterDataHandler extends AbstractActionHandler<GetDeclarationFilterData, GetDeclarationFilterDataResult> {

	public GetDeclarationFilterDataHandler() {
		super(GetDeclarationFilterData.class);
	}

	@Autowired
	private SecurityService securityService;

	@Autowired
	private PeriodService periodService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private DeclarationDataSearchService declarationDataSearchService;

	@Override
	public GetDeclarationFilterDataResult execute(GetDeclarationFilterData action, ExecutionContext executionContext)
            throws ActionException {
        // Данные для панели фильтрации
		GetDeclarationFilterDataResult res = new GetDeclarationFilterDataResult();

		TAUserInfo currentUser = securityService.currentUserInfo();

        DeclarationDataFilterAvailableValues declarationFilterValues =
                declarationDataSearchService.getFilterAvailableValues(currentUser, action.getTaxType());

        res.setDataKinds(getAvailableDeclarationFormKind(action.getTaxType(), action.isReports(), currentUser.getUser()));

        // Доступные подразделения
		res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(
                declarationFilterValues.getDepartmentIds()).values()));

		res.setFilterValues(declarationFilterValues);

        // Периоды, связанные с доступными подразделениями
		res.setPeriods(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(),
                new ArrayList<Integer>(declarationFilterValues.getDepartmentIds())));

		List<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>();
		reportPeriods.addAll(periodService.getOpenForUser(currentUser.getUser(), action.getTaxType()));
        DeclarationDataFilter dataFilter = new DeclarationDataFilter();
        dataFilter.setTaxType(action.getTaxType());
        res.setDefaultDecFilterData(dataFilter);
        res.setUserDepartmentId(currentUser.getUser().getDepartmentId());
        res.setAsnuIds(currentUser.getUser().getAsnuIds());

		return res;
	}

    public static List<DeclarationFormKind> getAvailableDeclarationFormKind(TaxType taxType, boolean isReports, TAUser taUser) {
        if (taxType.equals(TaxType.PFR)) {
            if (taUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                return Arrays.asList(DeclarationFormKind.PRIMARY, DeclarationFormKind.CONSOLIDATED, DeclarationFormKind.REPORTS);
            } else if (taUser.hasRole(taxType, TARole.F_ROLE_OPER)) {
                return Arrays.asList(DeclarationFormKind.PRIMARY);
            } else {
                return new ArrayList<DeclarationFormKind>();
            }
        } else if (taxType.equals(TaxType.NDFL)) {
            if (isReports) {
                if (taUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                    return Arrays.asList(DeclarationFormKind.REPORTS);
                } else {
                    return new ArrayList<DeclarationFormKind>();
                }
            } else {
                if (taUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                    return Arrays.asList(DeclarationFormKind.PRIMARY, DeclarationFormKind.CONSOLIDATED);
                } else if (taUser.hasRole(taxType, TARole.N_ROLE_OPER)) {
                    return Arrays.asList(DeclarationFormKind.PRIMARY);
                } else {
                    return new ArrayList<DeclarationFormKind>();
                }
            }
        }
        return new ArrayList<DeclarationFormKind>();
    }

    @Override
	public void undo(GetDeclarationFilterData getDeclarationFilterData,
                     GetDeclarationFilterDataResult getDeclarationFilterDataResult,
                     ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}
}
