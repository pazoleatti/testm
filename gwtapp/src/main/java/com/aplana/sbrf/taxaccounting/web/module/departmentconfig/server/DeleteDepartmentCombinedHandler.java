package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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

    private static final String SUCCESS_INFO = "Настройки для \"%s\" в периоде с %s по %s успешно удалены.";

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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
                case INCOME:
                    refBookId = RefBook.DEPARTMENT_CONFIG_INCOME;
                    break;
                case TRANSPORT:
                    refBookId = RefBook.DEPARTMENT_CONFIG_TRANSPORT;
                    break;
                case DEAL:
                    refBookId = RefBook.DEPARTMENT_CONFIG_DEAL;
                    break;
                case VAT:
                    refBookId = RefBook.DEPARTMENT_CONFIG_VAT;
                    break;
                case PROPERTY:
                    refBookId = RefBook.DEPARTMENT_CONFIG_PROPERTY;
                    break;
            }
            RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);
            Logger logger = new Logger();
            List<Long> deleteList = new ArrayList<Long>();
            deleteList.add(depCombined.getRecordId());
            provider.deleteAllRecords(logger, deleteList);

            if (!logger.containsLevel(LogLevel.ERROR)) {
                ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
                String strEndDate = sdf.format(period.getEndDate());
                String departmentName = departmentService.getDepartment(action.getDepartment()).getName();
                logger.info(String.format(SUCCESS_INFO, departmentName, sdf.format(period.getCalendarStartDate()), strEndDate));
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setHasError(true);
            }
        }
        return result;
    }

    @Override
    public void undo(DeleteDepartmentCombinedAction deleteDepartmentCombinedAction, DeleteDepartmentCombinedResult deleteDepartmentCombinedResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
