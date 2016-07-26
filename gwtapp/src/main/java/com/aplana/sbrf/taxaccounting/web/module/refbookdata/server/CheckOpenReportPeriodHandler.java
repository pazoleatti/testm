package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckOpenReportPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckOpenReportPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Проверка существует ли хотя бы один отчетный период для региональных налогов (транспорт, имущество) для подразделения.
 * 4А.1 Система проверяет, существует ли хотя бы один отчетный период для региональных налогов (транспорт, имущество) для подразделения.
 * http://conf.aplana.com/pages/viewpage.action?pageId=11378355
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CheckOpenReportPeriodHandler extends AbstractActionHandler<CheckOpenReportPeriodAction, CheckOpenReportPeriodResult> {

    @Autowired
    PeriodService periodService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    LogEntryService logEntryService;

    public CheckOpenReportPeriodHandler() {
        super(CheckOpenReportPeriodAction.class);
    }

    @Override
    public CheckOpenReportPeriodResult execute(CheckOpenReportPeriodAction action, ExecutionContext context) throws ActionException {
        CheckOpenReportPeriodResult result = new CheckOpenReportPeriodResult();
        Logger logger = new Logger();

        List<ReportPeriod> openReportPeriods = new ArrayList<ReportPeriod>(0);
        openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.TRANSPORT, Arrays.asList(action.getDepId()), true, true));
        openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.PROPERTY, Arrays.asList(action.getDepId()), true, true));
        openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.LAND, Arrays.asList(action.getDepId()), true, true));
        if (!openReportPeriods.isEmpty()){
            for (ReportPeriod period : openReportPeriods) {
                logger.error(
                        "Для подразделения %s для налога %s уже открыт период %s для %d",
                        departmentService.getDepartment(action.getDepId()).getName(),
                        period.getTaxPeriod().getTaxType().getName(),
                        period.getName(),
                        period.getTaxPeriod().getYear());
            }
            if (!logger.getEntries().isEmpty())
                result.setUuid(logEntryService.save(logger.getEntries()));
            /*throw new ServiceLoggerException(
                    "Подразделение не может быть отредактировано, так как для него нельзя изменить тип \"ТБ\", если для него существует период!",
                    logEntryService.save(logger.getEntries()));*/
        }
        result.setHaveOpenPeriod(!openReportPeriods.isEmpty());

        return result;
    }

    @Override
    public void undo(CheckOpenReportPeriodAction action, CheckOpenReportPeriodResult result, ExecutionContext context) throws ActionException {

    }
}
