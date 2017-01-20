package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.DeleteConfigPropertyAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.DeleteConfigPropertyResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.TableCell;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteConfigPropertyHandler extends AbstractActionHandler<DeleteConfigPropertyAction, DeleteConfigPropertyResult> {

    public DeleteConfigPropertyHandler() {
        super(DeleteConfigPropertyAction.class);
    }

    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    PeriodService reportService;
    @Autowired
    SecurityService securityService;
    @Autowired
    LogEntryService logEntryService;

    private static final String SUCCESS_INFO = "Настройки подразделения в период с %s по %s были удалены";
    private static final String SUCCESS_INFO_SHORT = "Настройки подразделения в период с %s были удалены";

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public DeleteConfigPropertyResult execute(DeleteConfigPropertyAction action, ExecutionContext executionContext) throws ActionException {
        DeleteConfigPropertyResult result = new DeleteConfigPropertyResult();
        Map<String, TableCell> notTableParams = action.getNotTableParams();

        if (notTableParams != null
                && action.getReportPeriodId() != null) {


            RefBookDataProvider provider = rbFactory.getDataProvider(action.getRefBookId());
            ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
            Logger logger = new Logger();
            logger.setTaUserInfo(securityService.currentUserInfo());

            RefBookRecordVersion recordVersion = provider.getRecordVersionInfo(action.getRecordId());
            List<Long> deleteList = new ArrayList<Long>();
            deleteList.add(recordVersion.getRecordId());

            RefBookDataProvider providerSlave = rbFactory.getDataProvider(action.getSlaveRefBookId());
            String filterSlave = "REF_BOOK_NDFL_ID = " + recordVersion.getRecordId();
            PagingResult<Map<String, RefBookValue>> paramsSlave = providerSlave.getRecords(period.getCalendarStartDate(), null, filterSlave, null);

            for (Map<String, RefBookValue> r : paramsSlave) {
                deleteList.add(r.get("record_id").getNumberValue().longValue());
            }

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
            result.setUuid(logEntryService.save(logger.getEntries()));
            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setHasError(true);
            }
        }
        return result;
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    @Override
    public void undo(DeleteConfigPropertyAction deleteConfigPropertyAction, DeleteConfigPropertyResult deleteConfigPropertyResult, ExecutionContext executionContext) throws ActionException {

    }
}
