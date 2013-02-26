package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationDataHandler extends AbstractActionHandler<GetDeclarationAction, GetDeclarationResult> {
    @Autowired
	@Qualifier("declarationService")
	private DeclarationService declarationService;

	@Autowired
	@Qualifier("departmentService")
	private DepartmentService departmentService;

	@Autowired
	private DeclarationAccessService declarationAccessService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private SecurityService securityService;

    public GetDeclarationDataHandler() {
        super(GetDeclarationAction.class);
    }

    @Override
    public GetDeclarationResult execute(GetDeclarationAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		GetDeclarationResult result = new GetDeclarationResult();
		Declaration declaration = declarationService.get(action.getId(), userId);
		result.setDeclaration(declaration);
		result.setCanRead(declarationAccessService.canRead(userId, action.getId()));
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
    public void undo(GetDeclarationAction action, GetDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
