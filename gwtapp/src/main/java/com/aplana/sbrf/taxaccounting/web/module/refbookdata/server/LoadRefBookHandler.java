package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.LoadRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.LoadRefBookResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
@Component
public class LoadRefBookHandler extends AbstractActionHandler<LoadRefBookAction, LoadRefBookResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private TAUserService userService;

    public LoadRefBookHandler() {
        super(LoadRefBookAction.class);
    }

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    @Override
    public LoadRefBookResult execute(final LoadRefBookAction action, ExecutionContext arg1) throws ActionException {
        final ReportType reportType = ReportType.IMPORT_REF_BOOK;

        TAUserInfo userInfo = securityService.currentUserInfo();
        LoadRefBookResult result = new LoadRefBookResult();
        Logger logger = new Logger();
        RefBook refBook = refBookFactory.get(action.getRefBookId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", action.getRefBookId());
        params.put("uuid", action.getUuid());
        if (refBook.isVersioned()) {
            params.put("dateFrom", action.getDateFrom());
            if (action.getDateTo() != null) {
                params.put("dateTo", action.getDateTo());
            }
        }

        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(refBook.getId());
        if (lockType == null || lockType.getFirst().equals(reportType)) {
            String keyTask = refBookFactory.generateTaskKey(refBook.getId(), reportType);
            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, refBookFactory.getTaskName(reportType, action.getRefBookId(), null), userInfo, action.isForce(), logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.LOCKED);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.CREATE);
            } else {
                result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.CREATE);
                asyncTaskManagerService.createTask(keyTask, reportType, params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                    @Override
                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                refBookFactory.getTaskFullName(reportType, action.getRefBookId(), null, null, null),
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
                        return refBookFactory.getTaskName(reportType, action.getRefBookId(), null);
                    }
                });
            }
        } else {
            logger.error(LockData.LOCK_CURRENT,
                    sdf.get().format(lockType.getSecond().getDateLock()),
                    userService.getUser(lockType.getSecond().getUserId()).getName(),
                    refBookFactory.getTaskName(lockType.getFirst(), action.getRefBookId(), null));
            throw new ServiceLoggerException("Выполнение операции \"%s\" невозможно",
                    logEntryService.save(logger.getEntries()),
                    refBookFactory.getTaskName(ReportType.IMPORT_REF_BOOK, action.getRefBookId(), null));
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(LoadRefBookAction action, LoadRefBookResult result,
                     ExecutionContext arg2) throws ActionException {
        // Auto-generated method stub
    }
}
