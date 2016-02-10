package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
        TimerReportResult.StatusReport status = getStatus(userInfo, action.getDeclarationDataId(), action.getType());
        result.setExistReport(status);
        if (TimerReportResult.StatusReport.EXIST.equals(status) && ReportType.PDF_DEC.equals(action.getType())) {
        } else if (!TimerReportResult.StatusReport.LOCKED.equals(status) && ReportType.PDF_DEC.equals(action.getType())) {
            TimerReportResult.StatusReport statusXML = getStatus(userInfo, action.getDeclarationDataId(), ReportType.XML_DEC);
            if (TimerReportResult.StatusReport.LOCKED.equals(statusXML) ||
                    TimerReportResult.StatusReport.NOT_EXIST.equals(statusXML)) {
                result.setExistXMLReport(statusXML);
            }
        }
        return result;
    }

    private TimerReportResult.StatusReport getStatus(TAUserInfo userInfo, long declarationDataId, ReportType reportType) {
        String key = declarationDataService.generateAsyncTaskKey(declarationDataId, reportType);
        if (!lockDataService.isLockExists(key, false)) {
            if (ReportType.ACCEPT_DEC.equals(reportType)) {
                return TimerReportResult.StatusReport.EXIST;
            } else if (reportService.getDec(userInfo, declarationDataId, reportType) == null) {
                Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationDataId, reportType);
                Long limit = declarationDataService.getTaskLimit(reportType);
                if (value != null && limit != 0 && limit < value) {
                    return TimerReportResult.StatusReport.LIMIT;
                } else {
                    return TimerReportResult.StatusReport.NOT_EXIST;
                }
            } else {
                return TimerReportResult.StatusReport.EXIST;
            }
        }
        return TimerReportResult.StatusReport.LOCKED;
    }

    @Override
    public void undo(TimerReportAction searchAction, TimerReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
