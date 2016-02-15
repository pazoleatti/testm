package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CreateReportResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PreAuthorize("isAuthenticated()")
public class CreateRefBookReportHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {

    private static final Log LOG = LogFactory.getLog(CreateRefBookReportHandler.class);

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentService departmentService;

    private static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");

    public CreateRefBookReportHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(final CreateReportAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType;
        final String reportName = action.getReportName();
        if (ReportType.CSV_REF_BOOK.getName().equals(reportName)) {
            reportType = ReportType.CSV_REF_BOOK;
        } else if (ReportType.EXCEL_REF_BOOK.getName().equals(reportName)) {
            reportType = ReportType.EXCEL_REF_BOOK;
        } else {
            reportType = ReportType.SPECIFIC_REPORT_REF_BOOK;
        }
        CreateReportResult result = new CreateReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();

        RefBook refBook = refBookFactory.get(action.getRefBookId());
        TAUser currentUser = securityService.currentUserInfo().getUser();

        String filter = null;
        if (refBook.getRegionAttribute() != null && !currentUser.hasRole("ROLE_CONTROL_UNP")) {
            List<Department> deps = departmentService.getBADepartments(currentUser);
            filter = RefBookPickerUtils.buildRegionFilterForUser(deps, refBook);
            if (filter != null && filter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
                //Среди подразделений пользователя нет относящихся к какому то региону и нет смысла получать записи
                // справочника - ни одна не должна быть ему доступна
                logger.error("Нет доступных записей для выгрузки");
                result.setUuid(logEntryService.save(logger.getEntries()));
                return result;
            }
        }

        String searchPattern = action.getSearchPattern();
        if (searchPattern != null && !searchPattern.isEmpty()) {
            if (filter != null && !filter.isEmpty()) {
                filter += " and (" + refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId()) + ")";
            } else {
                filter = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId());
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", action.getRefBookId());
        params.put("version", action.getVersion());
        params.put("searchPattern", action.getSearchPattern());
        params.put("filter", filter!=null?filter:"");
        RefBookAttribute sortAttribute = null;
        if (refBook.isHierarchic()) {
            try {
                sortAttribute = refBook.getAttribute("NAME");
                params.put("sortAttribute", sortAttribute.getId());
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            if (action.getSortColumnIndex() < 0) {
                action.setSortColumnIndex(0);
            }
            List<RefBookAttribute> refBookAttributeList = new LinkedList<RefBookAttribute>();
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (attribute.isVisible()) {
                    refBookAttributeList.add(attribute);
                }
            }
            sortAttribute = refBookAttributeList.get(action.getSortColumnIndex());
            params.put("sortAttribute", sortAttribute.getId());
        }
        params.put("isSortAscending", action.isAscSorting());
        if (reportType.equals(ReportType.SPECIFIC_REPORT_REF_BOOK))
            params.put("specificReportType", reportName);

        String keyTask = String.format("%s_%s_refBookId_%d_version_%s_filter_%s_%s_%s_%s",
                LockData.LockObjects.REF_BOOK.name(), reportType.getName(), action.getRefBookId(), SDF_DD_MM_YYYY.format(action.getVersion()) , action.getSearchPattern(),
                (sortAttribute!=null?sortAttribute.getAlias():null), action.isAscSorting(), UUID.randomUUID());
        asyncTaskManagerService.createTask(keyTask, reportType, params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
            @Override
            public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                        refBookFactory.getTaskFullName(reportType, action.getRefBookId(), action.getVersion(), action.getSearchPattern(), reportName),
                        LockData.State.IN_QUEUE.getText());
            }

            @Override
            public void executePostCheck() {
            }

            @Override
            public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                return false;
            }

            @Override
            public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
            }

            @Override
            public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                return refBookFactory.getTaskName(reportType, action.getRefBookId(), action.getReportName());
            }
        });
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction action, CreateReportResult result, ExecutionContext context) throws ActionException {

    }
}
