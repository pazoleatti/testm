package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.TimerReportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;


/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
    private BlobDataService blobDataService;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };

    public TimerReportDeclarationHandler() {
        super(TimerReportAction.class);
    }

    @Override
    public TimerReportResult execute(TimerReportAction action, ExecutionContext executionContext) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(action.getType());
        if (ddReportType.isSubreport())
            throw new ActionException("Неправильный тип отчета");
        TimerReportResult result = new TimerReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        TimerReportResult.Status status = getStatus(userInfo, action.getDeclarationDataId(), ddReportType);
        result.setExistReport(status);
        if (TimerReportResult.StatusReport.EXIST.equals(status.getStatusReport()) && ReportType.PDF_DEC.equals(ddReportType.getReportType())) {
        } else if (!TimerReportResult.StatusReport.LOCKED.equals(status.getStatusReport()) && ReportType.PDF_DEC.equals(ddReportType.getReportType())) {
            TimerReportResult.Status statusXML = getStatus(userInfo, action.getDeclarationDataId(), DeclarationDataReportType.XML_DEC);
            if (TimerReportResult.StatusReport.LOCKED.equals(statusXML.getStatusReport()) ||
                    TimerReportResult.StatusReport.NOT_EXIST.equals(statusXML.getStatusReport())) {
                result.setExistXMLReport(statusXML);
            }
        }
        return result;
    }

    private TimerReportResult.Status getStatus(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType ddReportType) {
        String key = declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType);
        if (!lockDataService.isLockExists(key, false)) {
            String uuid;
            if (DeclarationDataReportType.ACCEPT_DEC.equals(ddReportType)) {
                return new TimerReportResult.Status(TimerReportResult.StatusReport.EXIST);
            } else if ((uuid = reportService.getDec(userInfo, declarationDataId, ddReportType)) == null) {
                Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationDataId, DeclarationDataReportType.getDDReportTypeByReportType(ddReportType.getReportType()));
                Long limit = declarationDataService.getTaskLimit(ddReportType.getReportType());
                if (value != null && limit != 0 && limit < value) {
                    return TimerReportResult.STATUS_LIMIT;
                } else {
                    return TimerReportResult.STATUS_NOT_EXIST;
                }
            } else {
                BlobData blobData = blobDataService.get(uuid);
                return new TimerReportResult.Status(TimerReportResult.StatusReport.EXIST, sdf.get().format(blobData.getCreationDate())) ;
            }
        }
        return TimerReportResult.STATUS_LOCKED;
    }

    @Override
    public void undo(TimerReportAction searchAction, TimerReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
