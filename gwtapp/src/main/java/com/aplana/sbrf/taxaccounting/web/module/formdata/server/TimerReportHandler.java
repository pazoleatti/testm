package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
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

    @Autowired
    private FormDataService formDataService;

    public TimerReportHandler() {
        super(TimerReportAction.class);
    }

    @Override
    public TimerReportResult execute(TimerReportAction action, ExecutionContext executionContext) throws ActionException {
        TimerReportResult result = new TimerReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        String key = formDataService.generateReportKey(action.getFormDataId(), action.getType(), action.getSpecificReportType(), action.isShowChecked(), action.isManual(), action.isSaved());
        if (!lockDataService.isLockExists(key, false)) {
            String uuid;
            if (!action.getType().equals(ReportType.SPECIFIC_REPORT)) {
                uuid = reportService.get(userInfo, action.getFormDataId(), action.getType(), action.isShowChecked(), action.isManual(), action.isSaved());
            } else {
                uuid = reportService.get(userInfo, action.getFormDataId(), action.getSpecificReportType(), action.isShowChecked(), action.isManual(), action.isSaved());
            }
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
