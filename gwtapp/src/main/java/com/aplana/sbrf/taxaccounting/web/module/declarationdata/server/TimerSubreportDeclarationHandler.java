package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerSubreportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerSubreportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
    @Autowired
    private BlobDataService blobDataService;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };

    public TimerSubreportDeclarationHandler() {
        super(TimerSubreportAction.class);
    }

    @Override
    @Transactional
    public TimerSubreportResult execute(TimerSubreportAction action, ExecutionContext executionContext) throws ActionException {
        TimerSubreportResult result = new TimerSubreportResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationDataId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationDataId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();

        Map<String, TimerSubreportResult.Status> mapExistReport = new HashMap<String, TimerSubreportResult.Status>();
        DeclarationData declaration = declarationDataService.get(action.getDeclarationDataId(), userInfo);
        List<DeclarationSubreport> subreports = declarationTemplateService.get(declaration.getDeclarationTemplateId()).getSubreports();
        for(DeclarationSubreport subreport: subreports) {
            final DeclarationDataReportType ddReportType = new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, subreport);
            TimerSubreportResult.Status status = getStatus(userInfo, action.getDeclarationDataId(), ddReportType);
            mapExistReport.put(subreport.getAlias(), status);
        }
        result.setMapExistReport(mapExistReport);
        return result;
    }

    private TimerSubreportResult.Status getStatus(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType ddReportType) {
        String key = declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType);
        if (!lockDataService.isLockExists(key, false)) {
            String uuid = reportService.getDec(userInfo, declarationDataId, ddReportType);
            if (uuid == null) {
                return TimerSubreportResult.STATUS_NOT_EXIST;
            } else {
                BlobData blobData = blobDataService.get(uuid);
                return new TimerSubreportResult.Status(TimerSubreportResult.StatusReport.EXIST, sdf.get().format(blobData.getCreationDate()));
            }
        }
        return TimerSubreportResult.STATUS_LOCKED;
    }

    @Override
    public void undo(TimerSubreportAction action, TimerSubreportResult result, ExecutionContext executionContext) throws ActionException {

    }

}
