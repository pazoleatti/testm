package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.FormDataReportType;
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

import java.util.*;

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
        Map<String, TimerReportResult.StatusReport> mapExistReport = new HashMap<String, TimerReportResult.StatusReport>();
        action.getSpecificReportTypes().add(FormDataReportType.EXCEL.getReportName());
        action.getSpecificReportTypes().add(FormDataReportType.CSV.getReportName());
        for(String type: action.getSpecificReportTypes()) {
            TimerReportResult.StatusReport statusReport;
            final FormDataReportType fdReportType = FormDataReportType.getFDReportTypeByName(type);
            String key = formDataService.generateReportKey(action.getFormDataId(), fdReportType, action.isShowChecked(), action.isManual(), action.isSaved());
            if (!lockDataService.isLockExists(key, false)) {
                String uuid = reportService.get(userInfo, action.getFormDataId(), fdReportType, action.isShowChecked(), action.isManual(), action.isSaved());
                if (uuid == null) {
                    statusReport = TimerReportResult.StatusReport.NOT_EXIST;
                } else {
                    statusReport = TimerReportResult.StatusReport.EXIST;
                }
            } else {
                statusReport = TimerReportResult.StatusReport.LOCKED;
            }
            mapExistReport.put(type, statusReport);
        }
        result.setMapExistReport(mapExistReport);
        return result;
    }

    @Override
    public void undo(TimerReportAction searchAction, TimerReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
