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
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(action.getType());
        TimerReportResult result = new TimerReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        TimerReportResult.StatusReport status = getStatus(userInfo, action.getDeclarationDataId(), ddReportType);
        result.setExistReport(status);
        if (TimerReportResult.StatusReport.EXIST.equals(status) && ReportType.PDF_DEC.equals(ddReportType.getReportType())) {
        } else if (!TimerReportResult.StatusReport.LOCKED.equals(status) && ReportType.PDF_DEC.equals(ddReportType.getReportType())) {
            TimerReportResult.StatusReport statusXML = getStatus(userInfo, action.getDeclarationDataId(), DeclarationDataReportType.XML_DEC);
            if (TimerReportResult.StatusReport.LOCKED.equals(statusXML) ||
                    TimerReportResult.StatusReport.NOT_EXIST.equals(statusXML)) {
                result.setExistXMLReport(statusXML);
            }
        }
        return result;
    }

    private TimerReportResult.StatusReport getStatus(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType ddReportType) {
        String key = declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType);
        if (!lockDataService.isLockExists(key, false)) {
            if (DeclarationDataReportType.ACCEPT_DEC.equals(ddReportType)) {
                return TimerReportResult.StatusReport.EXIST;
            } else if (reportService.getDec(userInfo, declarationDataId, ddReportType) == null) {
                Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationDataId, ddReportType.getReportType());
                Long limit = declarationDataService.getTaskLimit(ddReportType.getReportType());
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
