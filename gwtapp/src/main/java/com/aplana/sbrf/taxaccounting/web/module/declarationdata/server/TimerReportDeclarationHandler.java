package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportResult;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.PdfPage;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author lhaziev
 *
 */
@Service
public class TimerReportDeclarationHandler extends AbstractActionHandler<TimerReportAction, TimerReportResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public TimerReportDeclarationHandler() {
        super(TimerReportAction.class);
    }

    @Override
    public TimerReportResult execute(TimerReportAction action, ExecutionContext executionContext) throws ActionException {
        TimerReportResult result = new TimerReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationDataId(), action.getType());
        if (!lockDataService.isLockExists(key)) {
            String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), action.getType());
            if (uuid == null) {
                result.setExistReport(TimerReportResult.StatusReport.NOT_EXIST);
            } else {
                result.setExistReport(TimerReportResult.StatusReport.EXIST);
                if (ReportType.XML_DEC.equals(action.getType())) {
                    result.setPdf(generatePdfViewerModel(action.getDeclarationDataId(), userInfo));
                }
            }
        } else {
            result.setExistReport(TimerReportResult.StatusReport.LOCKED);
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
        byte buf[] = declarationDataService.getPdfData(declarationDataId, userInfo);
        if (buf != null) {
            InputStream pdfData = new ByteArrayInputStream(buf);
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
        }
        return pdf;
    }

    @Override
    public void undo(TimerReportAction searchAction, TimerReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
