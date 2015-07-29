package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CheckJrxmlAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CheckJrxmlResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class CheckJrxmlHandler extends AbstractActionHandler<CheckJrxmlAction, CheckJrxmlResult> {

    private static final String DEC_DATA_EXIST_IN_TASK =
            "%s в подразделении \"%s\" в периоде \"%s %d%s\", налоговый орган %s, КПП %s";
    private static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    DeclarationTemplateService declarationTemplateService;
    @Autowired
    DeclarationDataService declarationDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    ReportService reportService;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    PeriodService periodService;
    @Autowired
    LockDataService lockDataService;
    @Autowired
    LogEntryService logEntryService;

    public CheckJrxmlHandler() {
        super(CheckJrxmlAction.class);
    }

    @Override
    public CheckJrxmlResult execute(CheckJrxmlAction action, ExecutionContext context) throws ActionException {
        DeclarationTemplate template = declarationTemplateService.get(action.getDtId());
        TAUserInfo currUser = securityService.currentUserInfo();
        Logger logger = new Logger();
        //Идентификаторы деклараций для передачи на клиента,  полследующей передачей в хендлер очистки
        HashSet<Long> dataIds = new HashSet<Long>();
        HashSet<Long> dataLockIds = new HashSet<Long>();
        ArrayList<String> existDec = new ArrayList<String>();
        ArrayList<String> existInLockDec = new ArrayList<String>();

        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())){
            DeclarationData data = declarationDataService.get(dataId, currUser);
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, ReportType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, ReportType.EXCEL_DEC);
            ReportPeriod rp = periodService.getReportPeriod(data.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.get(data.getDepartmentReportPeriodId());
            if (
                    reportService.getDec(currUser, dataId, ReportType.PDF_DEC) != null
                    ||
                    reportService.getDec(currUser, dataId, ReportType.EXCEL_DEC) != null) {
                existDec.add(String.format(
                        DEC_DATA_EXIST_IN_TASK,
                        template.getName(),
                        departmentService.getDepartment(data.getDepartmentId()).getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
                        data.getTaxOrganCode(),
                        data.getKpp()));
                dataIds.add(dataId);
            }  else if(lockDataService.isLockExists(decKeyPDF, false) || lockDataService.isLockExists(decKeyXLSM, false)){
                existInLockDec.add(String.format(
                        DEC_DATA_EXIST_IN_TASK,
                        template.getName(),
                        departmentService.getDepartment(data.getDepartmentId()).getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
                        data.getTaxOrganCode(),
                        data.getKpp()));
                dataLockIds.add(dataId);
            }
        }
        if(!existInLockDec.isEmpty()){
            logger.warn("По следующим экземплярам деклараций запущена операция формирования pdf/xlsx отчета:");
            for (String s : existInLockDec){
                logger.warn(s);
            }
        }
        if(!existDec.isEmpty()){
            logger.warn("По следующим экземплярам деклараций запущена операция формирования pdf/xlsx отчета:");
            for (String s : existDec){
                logger.warn(s);
            }
        }

        CheckJrxmlResult result = new CheckJrxmlResult();
        result.setIds(dataIds);
        result.setLockIds(dataLockIds);
        result.setCanDelete(dataIds.isEmpty() && dataLockIds.isEmpty());
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CheckJrxmlAction action, CheckJrxmlResult result, ExecutionContext context) throws ActionException {

    }
}
