package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CreateReportResult;
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
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private TAUserService userService;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    public CreateRefBookReportHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(final CreateReportAction action, ExecutionContext context) throws ActionException {
        final AsyncTaskType reportType;
        final String reportName = action.getReportName();
        if (AsyncTaskType.CSV_REF_BOOK.getName().equals(reportName)) {
            reportType = AsyncTaskType.CSV_REF_BOOK;
        } else if (AsyncTaskType.EXCEL_REF_BOOK.getName().equals(reportName)) {
            reportType = AsyncTaskType.EXCEL_REF_BOOK;
        } else {
            reportType = AsyncTaskType.SPECIFIC_REPORT_REF_BOOK;
        }
        CreateReportResult result = new CreateReportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();

        RefBook refBook = refBookFactory.get(action.getRefBookId());

        LockData lockData = lockDataService.getLock(refBookFactory.generateTaskKey(refBook.getId()));
        if (lockData == null) {
            String filter = null;
            String searchPattern = action.getSearchPattern();
            if (searchPattern != null && !searchPattern.isEmpty()) {
                filter = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId(), action.isExactSearch());
            }

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("refBookId", action.getRefBookId());
            params.put("version", action.getVersion());
            params.put("searchPattern", action.getSearchPattern());
            params.put("filter", filter != null ? filter : "");
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
            if (reportType.equals(AsyncTaskType.SPECIFIC_REPORT_REF_BOOK))
                params.put("specificReportType", reportName);

            String keyTask = String.format("%s_%s_refBookId_%d_version_%s_filter_%s_%s_%s_%s",
                    LockData.LockObjects.REF_BOOK.name(), reportType.getName(), action.getRefBookId(), SDF_DD_MM_YYYY.get().format(action.getVersion()), action.getSearchPattern(),
                    (sortAttribute != null ? sortAttribute.getAlias() : null), action.isAscSorting(), UUID.randomUUID());
            asyncManager.executeTask(keyTask, reportType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
        } else {
            logger.info(refBookFactory.getRefBookLockDescription(lockData, refBook.getId()));
            result.setErrorMsg("Для текущего справочника запущена операция, при которой формирование отчета невозможно");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction action, CreateReportResult result, ExecutionContext context) throws ActionException {

    }
}
