package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RecalculateDeclarationDataHandler extends AbstractActionHandler<RecalculateDeclarationDataAction, RecalculateDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    public RecalculateDeclarationDataHandler() {
        super(RecalculateDeclarationDataAction.class);
    }

    @Override
    public RecalculateDeclarationDataResult execute(RecalculateDeclarationDataAction action, ExecutionContext context) {
		TAUserInfo userInfo = securityService.currentUserInfo();
        RecalculateDeclarationDataResult result = new RecalculateDeclarationDataResult();
        declarationDataService.checkLockedMe(action.getDeclarationId(), userInfo);
        declarationDataService.lock(action.getDeclarationId(), userInfo);
        try {
            Logger logger = new Logger();
            declarationDataService.calculate(logger, action.getDeclarationId(), userInfo, action.getDocDate());
            String key = LockData.LOCK_OBJECTS.DECLARATION_DATA.name() + "_" +action.getDeclarationId() + "_" + ReportType.XML_DEC.getName();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("declarationDataId", action.getDeclarationId());
            params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
            params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
            LockData lockData;
            if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 4)) == null) {
                // отменяем задания на формирование XML
                lockDataService.unlock(key, 0, true);
            }
            try {
                declarationDataService.deleteReport(action.getDeclarationId());
                // отменяем задания на формирование XLSX
                lockDataService.unlock(LockData.LOCK_OBJECTS.DECLARATION_DATA.name() + "_" +action.getDeclarationId() + "_" + ReportType.EXCEL_DEC.getName(), 0, true);
                // ставим задачу в очередь
                lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                asyncManager.executeAsync(ReportType.XML_DEC.getAsyncTaskTypeId(false), params, BalancingVariants.LONG);
            } catch (AsyncTaskException e) {
                lockDataService.unlock(key, userInfo.getUser().getId());
                logger.error("Ошибка при постановке в очередь асинхронной задачи формирования отчета");
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
        } finally {
            declarationDataService.unlock(action.getDeclarationId(), userInfo);
        }
        return result;
    }

    @Override
    public void undo(RecalculateDeclarationDataAction action, RecalculateDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
