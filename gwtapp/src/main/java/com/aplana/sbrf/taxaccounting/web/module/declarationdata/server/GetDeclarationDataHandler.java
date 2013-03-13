package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationDataHandler extends AbstractActionHandler<GetDeclarationDataAction, GetDeclarationDataResult> {
    @Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private DeclarationDataAccessService declarationAccessService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private SecurityService securityService;

    public GetDeclarationDataHandler() {
        super(GetDeclarationDataAction.class);
    }

    @Override
    public GetDeclarationDataResult execute(GetDeclarationDataAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		GetDeclarationDataResult result = new GetDeclarationDataResult();
		DeclarationData declaration = declarationDataService.get(action.getId(), userId);
		result.setCanAccept(declarationAccessService.canAccept(userId, action.getId()));
		result.setCanReject(declarationAccessService.canReject(userId, action.getId()));
		result.setCanDownload(declarationAccessService.canDownloadXml(userId, action.getId()));
		result.setCanDelete(declarationAccessService.canDelete(userId, action.getId()));
		result.setTaxType(declarationTemplateService.get(declaration.getDeclarationTemplateId()).getDeclarationType().getTaxType());
		result.setDeclarationType(declarationTemplateService.get(declaration.getDeclarationTemplateId()).getDeclarationType().getName());
		result.setDepartment(departmentService.getDepartment(declaration.getDepartmentId()).getName());
		result.setReportPeriod(reportPeriodDao.get(declaration.getReportPeriodId()).getName());

		return result;
    }

    @Override
    public void undo(GetDeclarationDataAction action, GetDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
