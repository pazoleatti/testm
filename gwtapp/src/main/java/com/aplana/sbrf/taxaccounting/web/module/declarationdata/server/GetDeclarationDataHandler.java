package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.PdfPage;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDeclarationDataHandler
		extends
		AbstractActionHandler<GetDeclarationDataAction, GetDeclarationDataResult> {
	
	public static final int DEFAULT_IMAGE_RESOLUTION = 150;
	
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private DeclarationDataAccessService declarationAccessService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

	public GetDeclarationDataHandler() {
		super(GetDeclarationDataAction.class);
	}

	@Override
	public GetDeclarationDataResult execute(GetDeclarationDataAction action,
			ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();

		GetDeclarationDataResult result = new GetDeclarationDataResult();
		Set<FormDataEvent> permittedEvents  = declarationAccessService.getPermittedEvents(userInfo, action.getId());
		
		DeclarationData declaration = declarationDataService.get(
				action.getId(), userInfo);
        Date docDate = declarationDataService.getXmlDataDocDate(action.getId(), userInfo);
		result.setDocDate(docDate != null ? docDate : new Date());

        result.setAccepted(declaration.isAccepted());

		result.setCanAccept(permittedEvents.contains(FormDataEvent.MOVE_CREATED_TO_ACCEPTED));
		result.setCanReject(permittedEvents.contains(FormDataEvent.MOVE_ACCEPTED_TO_CREATED));
		result.setCanDelete(permittedEvents.contains(FormDataEvent.DELETE));

        TaxType taxType = declarationTemplateService
                .get(declaration.getDeclarationTemplateId())
                .getType().getTaxType();
		result.setTaxType(taxType);

		result.setDeclarationType(declarationTemplateService
				.get(declaration.getDeclarationTemplateId())
				.getType().getName());
		result.setDepartment(departmentService.getParentsHierarchy(
				declaration.getDepartmentId()));
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                declaration.getDepartmentReportPeriodId());

		result.setReportPeriod(departmentReportPeriod.getReportPeriod().getName());

        result.setReportPeriodYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());

        result.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        result.setTaxOrganCode(declaration.getTaxOrganCode());
        result.setKpp(declaration.getKpp());

		return result;
	}

	@Override
	public void undo(GetDeclarationDataAction action,
			GetDeclarationDataResult result, ExecutionContext context)
			throws ActionException {
		// Nothing!
	}
}
