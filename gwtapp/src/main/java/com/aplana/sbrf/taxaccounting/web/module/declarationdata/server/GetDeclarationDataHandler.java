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
	private PeriodService reportPeriodService;

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
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(
                declaration.getReportPeriodId());
		result.setReportPeriod(reportPeriod.getName());

        result.setReportPeriodYear(reportPeriod.getTaxPeriod().getYear());

		result.setPdf(generatePdfViewerModel(action, userInfo, taxType));

		return result;
	}

	/**
	 * Формирует модель для PDFViewer
	 * 
	 * @param action
	 * @param userInfo
	 * @return
	 */
	private Pdf generatePdfViewerModel(GetDeclarationDataAction action,
									   TAUserInfo userInfo, TaxType taxType) {
		Pdf pdf = new Pdf();
		pdf.setTitle(!taxType.equals(TaxType.DEAL) ? "Список листов декларации" : "Список листов уведомления");
		List<PdfPage> pdfPages = new ArrayList<PdfPage>();
        byte buf[] = declarationDataService.getPdfData(action.getId(), userInfo);
        if (buf != null) {
    		InputStream pdfData = new ByteArrayInputStream(buf);
            int pageNumber = PDFImageUtils.getPageNumber(pdfData);
            String randomUUID = UUID.randomUUID().toString().toLowerCase(); // добавлено чтобы браузер не кешировал данные
            for (int i = 0; i < pageNumber; i++) {
                PdfPage pdfPage = new PdfPage();
                pdfPage.setTitle("Лист " + (i + 1));

                pdfPage.setSrc(String.format("download/declarationData/pageImage/%d/%d/%s",
                        action.getId(), i, randomUUID));
                pdfPages.add(pdfPage);
            }
            pdf.setPdfPages(pdfPages);
        }
		return pdf;
	}

	@Override
	public void undo(GetDeclarationDataAction action,
			GetDeclarationDataResult result, ExecutionContext context)
			throws ActionException {
		// Nothing!
	}
}
