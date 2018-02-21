package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.LoadRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.LoadRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
@Component
public class LoadRefBookHandler extends AbstractActionHandler<LoadRefBookAction, LoadRefBookResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private ConfigurationService configurationService;

    public LoadRefBookHandler() {
        super(LoadRefBookAction.class);
    }

    @Override
    public LoadRefBookResult execute(final LoadRefBookAction action, ExecutionContext arg1) throws ActionException {
        final LoadRefBookResult result = new LoadRefBookResult();
        Logger logger = new Logger();

        Configuration isImportEnabledConfiguration = configurationService.fetchByEnum(ConfigurationParam.ENABLE_IMPORT_PERSON);
        if (isImportEnabledConfiguration != null && "1".equals(isImportEnabledConfiguration.getValue())) {
            TAUserInfo userInfo = securityService.currentUserInfo();
            final TAUser user = userInfo.getUser();
            BlobData blobData = blobDataService.get(action.getUuid());
            String fileName = blobData.getName();
            RefBook refBook = refBookFactory.get(action.getRefBookId());

            String refBookLockKey = refBookFactory.generateTaskKey(action.getRefBookId());
            LockData refBookLockData = lockDataService.getLock(refBookLockKey);
            if (refBookLockData != null && refBookLockData.getUserId() != user.getId()) {
                logger.info(refBookFactory.getRefBookLockDescription(refBookLockData, refBook.getId()));
                throw new ServiceLoggerException("Загрузка файла \"%s\" не может быть выполнена.",
                        logEntryService.save(logger.getEntries()),
                        fileName);
            } else {
                String asyncLockKey = LockData.LockObjects.IMPORT_REF_BOOK_XML.name() + "_" + action.getRefBookId();
                Pair<Boolean, String> restartStatus = asyncManager.restartTask(asyncLockKey, userInfo, action.isForce(), logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.CREATE);
                    // в логгере будет что задача запущена и вы добавлены в список получателей оповещения
                } else {
                    result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.CREATE);
                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("refBookId", action.getRefBookId());
                        params.put("blobDataId", action.getUuid());
                        asyncManager.executeTask(asyncLockKey, AsyncTaskType.IMPORT_REF_BOOK_XML, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                            @Override
                            public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                                return lockDataService.lockAsync(keyTask, user.getId());
                            }

                            @Override
                            public void postCheckProcessing() {
                                result.setStatus(LoadRefBookResult.CreateAsyncTaskStatus.EXIST_TASK);
                            }
                        });
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        } else {
            logger.error("Загрузка файлов справочника ФЛ отключена. Обратитесь к администратору");
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(LoadRefBookAction action, LoadRefBookResult result,
                     ExecutionContext arg2) throws ActionException {
        // nothing
    }
}
