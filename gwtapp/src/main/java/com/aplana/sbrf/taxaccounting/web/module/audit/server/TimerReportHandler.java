package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.TimerReportAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.TimerReportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service("TimerReportAuditHandler")
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class TimerReportHandler extends AbstractActionHandler<TimerReportAction, TimerReportResult> {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private LockDataService lockDataService;

    public TimerReportHandler() {
        super(TimerReportAction.class);
    }

    @Override
    public TimerReportResult execute(TimerReportAction action, ExecutionContext executionContext) throws ActionException {
        TimerReportResult result = new TimerReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        if (action.getReportType() == ReportType.ARCHIVE_AUDIT){
            if (lockDataService.isLockExists(LockData.LockObjects.LOG_SYSTEM_BACKUP.name(), false)){
                result.setLocked(true);
                return result;
            }
        }
        String uuid = reportService.getAudit(userInfo, action.getReportType());
        result.setLocked(false);
        result.setExist(uuid!=null);
        result.setUuid(uuid);

        return result;
    }

    @Override
    public void undo(TimerReportAction timerReportAction, TimerReportResult timerReportResult, ExecutionContext executionContext) throws ActionException {

    }
}