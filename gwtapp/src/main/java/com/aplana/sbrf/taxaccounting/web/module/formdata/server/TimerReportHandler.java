package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerReportAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerReportResult;
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
        String key = LockData.LOCK_OBJECTS.FORM_DATA.name() + "_" + action.getFormDataId() + "_" + action.getType().getName() + "_isShowChecked_" + action.isShowChecked() + "_manual_" + action.isManual() + "_saved_" + action.isSaved();
        if (!lockDataService.isLockExists(key)) {
            String uuid = reportService.get(userInfo, action.getFormDataId(), action.getType(), action.isShowChecked(), action.isManual(), action.isSaved());
            if (uuid == null) {
                result.setExistReport(TimerReportResult.StatusReport.NOT_EXIST);
            } else {
                result.setExistReport(TimerReportResult.StatusReport.EXIST);
            }
        } else {
            result.setExistReport(TimerReportResult.StatusReport.LOCKED);
        }

        return result;
    }

    @Override
    public void undo(TimerReportAction searchAction, TimerReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
