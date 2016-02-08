package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetPdfAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetPdfResult;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.PdfPage;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author lhaziev
 *
 */
@Service
public class GetPdfDeclarationHandler extends AbstractActionHandler<GetPdfAction, GetPdfResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public GetPdfDeclarationHandler() {
        super(GetPdfAction.class);
    }

    @Override
    public GetPdfResult execute(GetPdfAction action, ExecutionContext executionContext) throws ActionException {
        GetPdfResult result = new GetPdfResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        if (reportService.getDec(userInfo, action.getDeclarationDataId(), ReportType.PDF_DEC) != null) {
            result.setPdf(generatePdfViewerModel(action.getDeclarationDataId(), userInfo));
        }
        return result;
    }

    /**
     * Формирует модель для PDFViewer
     *
     * @param declarationDataId
     * @param userInfo
     * @return
     */
    private Pdf generatePdfViewerModel(long declarationDataId, TAUserInfo userInfo) {

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        TaxType taxType = declarationTemplateService
                .get(declarationData.getDeclarationTemplateId())
                .getType().getTaxType();

        Pdf pdf = new Pdf();
        pdf.setTitle(!taxType.equals(TaxType.DEAL) ? "Список листов декларации" : "Список листов уведомления");
        List<PdfPage> pdfPages = new ArrayList<PdfPage>();
        InputStream pdfData = declarationDataService.getPdfDataAsStream(declarationDataId, userInfo);
        if (pdfData != null) {
            try {
                int pageNumber = PDFImageUtils.getPageNumber(pdfData);
                String randomUUID = UUID.randomUUID().toString().toLowerCase(); // добавлено чтобы браузер не кешировал данные
                for (int i = 0; i < pageNumber; i++) {
                    PdfPage pdfPage = new PdfPage();
                    pdfPage.setTitle("Лист " + (i + 1));

                    pdfPage.setSrc(String.format("download/declarationData/pageImage/%d/%d/%s",
                            declarationDataId, i, randomUUID));
                    pdfPages.add(pdfPage);
                }
                pdf.setPdfPages(pdfPages);
            } finally {
                IOUtils.closeQuietly(pdfData);
            }
        }
        return pdf;
    }

    @Override
    public void undo(GetPdfAction action, GetPdfResult result, ExecutionContext executionContext) throws ActionException {

    }

}
