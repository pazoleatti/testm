package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DeleteDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DeleteDepartmentCombinedResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author vpetrov
*/
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteDepartmentCombinedHandler extends AbstractActionHandler<DeleteDepartmentCombinedAction,
        DeleteDepartmentCombinedResult> {

    @Autowired
    private PeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    private static final String SUCCESS_INFO = "Настройки подразделения в период %s - %s были удалены";
    private static final String SUCCESS_INFO_SHORT = "Настройки подразделения в период %s были удалены";

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    public DeleteDepartmentCombinedHandler() {
        super(DeleteDepartmentCombinedAction.class);
    }

    @Override
    public DeleteDepartmentCombinedResult execute(DeleteDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {
        DeleteDepartmentCombinedResult result = new DeleteDepartmentCombinedResult();
        DepartmentCombined depCombined = action.getDepartmentCombined();

        if (depCombined != null
                && depCombined.getDepartmentId() != null
                && !depCombined.getDepartmentId().isEmpty()
                && action.getTaxType() != null
                && action.getReportPeriodId() != null) {

            Long refBookId = null;
            switch (action.getTaxType()) {
                case NDFL:
                    refBookId = RefBook.Id.NDFL.getId();
                    break;
                case PFR:
                    refBookId = RefBook.Id.FOND.getId();
                    break;
            }
            RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);
            ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
            Logger logger = new Logger();
            logger.setTaUserInfo(securityService.currentUserInfo());

            RefBookRecordVersion recordVersion = provider.getRecordVersionInfo(depCombined.getRecordId());
            List<Long> deleteList = new ArrayList<Long>();
            deleteList.add(recordVersion.getRecordId());

            if (period.getCalendarStartDate().equals(recordVersion.getVersionStart())) {
                provider.deleteRecordVersions(logger, deleteList, false);
            } else {
                provider.updateRecordsVersionEnd(logger, addDayToDate(period.getCalendarStartDate(),-2), deleteList);
            }

            if (!logger.containsLevel(LogLevel.ERROR)) {
                if (recordVersion.getVersionEnd()==null)
                    logger.info(String.format(SUCCESS_INFO_SHORT, sdf.get().format(period.getCalendarStartDate())));
                else
                    logger.info(String.format(SUCCESS_INFO, sdf.get().format(period.getCalendarStartDate()), sdf.get().format(recordVersion.getVersionEnd())));
            }

            if (action.getOldUUID() == null) {
                result.setUuid(logEntryService.save(logger.getEntries()));
            } else {
                result.setUuid(logEntryService.update(logger.getEntries(), action.getOldUUID()));
            }
            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setHasError(true);
            }
        }
        return result;
    }

    @Override
    public void undo(DeleteDepartmentCombinedAction deleteDepartmentCombinedAction, DeleteDepartmentCombinedResult deleteDepartmentCombinedResult, ExecutionContext executionContext) throws ActionException {
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}
