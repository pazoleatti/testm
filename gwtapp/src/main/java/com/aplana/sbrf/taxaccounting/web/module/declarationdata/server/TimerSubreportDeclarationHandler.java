package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerSubreportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerSubreportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lhaziev
 *
 */
@Service
public class TimerSubreportDeclarationHandler extends AbstractActionHandler<TimerSubreportAction, TimerSubreportResult> {

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

    public TimerSubreportDeclarationHandler() {
        super(TimerSubreportAction.class);
    }

    @Override
    @Transactional
    public TimerSubreportResult execute(TimerSubreportAction action, ExecutionContext executionContext) throws ActionException {
        TimerSubreportResult result = new TimerSubreportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();

        Map<String, TimerSubreportResult.StatusReport> mapExistReport = new HashMap<String, TimerSubreportResult.StatusReport>();
        DeclarationData declaration = declarationDataService.get(action.getDeclarationDataId(), userInfo);
        List<DeclarationSubreport> subreports = declarationTemplateService.get(declaration.getDeclarationTemplateId()).getSubreports();
        for(DeclarationSubreport subreport: subreports) {
            final DeclarationDataReportType ddReportType = new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, subreport);
            TimerSubreportResult.StatusReport status = getStatus(userInfo, action.getDeclarationDataId(), ddReportType);
            mapExistReport.put(subreport.getAlias(), status);
        }
        result.setMapExistReport(mapExistReport);
        return result;
    }

    private TimerSubreportResult.StatusReport getStatus(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType ddReportType) {
        String key = declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType);
        if (!lockDataService.isLockExists(key, false)) {
            if (reportService.getDec(userInfo, declarationDataId, ddReportType) == null) {
                return TimerSubreportResult.StatusReport.NOT_EXIST;
            } else {
                return TimerSubreportResult.StatusReport.EXIST;
            }
        }
        return TimerSubreportResult.StatusReport.LOCKED;
    }

    @Override
    public void undo(TimerSubreportAction action, TimerSubreportResult result, ExecutionContext executionContext) throws ActionException {

    }

}
