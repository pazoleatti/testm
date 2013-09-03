package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.PdfPage;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
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
	private ReportPeriodService reportPeriodService;

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
		result.setDocDate(declarationDataService.getXmlDataDocDate(
				action.getId(), userInfo));
		
		result.setCanAccept(permittedEvents.contains(FormDataEvent.MOVE_CREATED_TO_ACCEPTED));
		result.setCanReject(permittedEvents.contains(FormDataEvent.MOVE_ACCEPTED_TO_CREATED));
		result.setCanDownload(permittedEvents.contains(FormDataEvent.GET_LEVEL1));
		result.setCanDelete(permittedEvents.contains(FormDataEvent.DELETE));
		
		result.setTaxType(declarationTemplateService
				.get(declaration.getDeclarationTemplateId())
				.getDeclarationType().getTaxType());
		result.setDeclarationType(declarationTemplateService
				.get(declaration.getDeclarationTemplateId())
				.getDeclarationType().getName());
		result.setDepartment(departmentService.getDepartment(
				declaration.getDepartmentId()).getName());
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(
                declaration.getReportPeriodId());
		result.setReportPeriod(reportPeriod.getName());

        Date reportPeriodStartDate = reportPeriod.getTaxPeriod().getStartDate();
        String year = new SimpleDateFormat("yyyy").format(reportPeriodStartDate);
        result.setReportPeriodYear(Integer.valueOf(year));

		result.setPdf(generatePdfViewerModel(action, userInfo));

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
									   TAUserInfo userInfo) {
		Pdf pdf = new Pdf();
		pdf.setTitle("Список листов декларации");
		List<PdfPage> pdfPages = new ArrayList<PdfPage>();
		InputStream pdfData = new ByteArrayInputStream(
				declarationDataService.getPdfData(action.getId(), userInfo));
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
		return pdf;
	}

	@Override
	public void undo(GetDeclarationDataAction action,
			GetDeclarationDataResult result, ExecutionContext context)
			throws ActionException {
		// Nothing!
	}
}
