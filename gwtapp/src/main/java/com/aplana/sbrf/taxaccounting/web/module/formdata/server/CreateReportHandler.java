package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CreateReportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhaziev
 *
 */
@Service
public class CreateReportHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    public CreateReportHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        CreateReportResult result = new CreateReportResult();
        Map<String, Object> params = new HashMap<String, Object>();
        String key = LockData.LOCK_OBJECTS.FORM_DATA.name() + "_" + action.getFormDataId() + "_" + action.getType().getName() + "_isShowChecked_" + action.isShowChecked() + "_manual_" + action.isManual();
        TAUserInfo userInfo = securityService.currentUserInfo();
        params.put("formDataId", action.getFormDataId());
        params.put("isShowChecked", action.isShowChecked());
        params.put("manual", action.isManual());
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        Logger logger = new Logger();
        if (lockDataService.lock(key, userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 4) == null) {
            try {
                String uuid = reportService.get(userInfo, action.getFormDataId(), action.getType(), action.isShowChecked(), action.isManual(), false);
                if (uuid == null) {
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    asyncManager.executeAsync(3L, params, BalancingVariants.SHORT);
                    logger.info(String.format("%s отчет текущей налоговой формы(%s) поставлен в очередь на формирование.", action.getType().getName(), action.isManual()?"версия ручного ввода":"автоматическая версия"));
                } else {
                    result.setExistReport(true);
                    lockDataService.unlock(key, userInfo.getUser().getId());
                }
            } catch (Exception e) {
                lockDataService.unlock(key, userInfo.getUser().getId());
                throw new ActionException(e);
            }
        } else {
            logger.info(String.format("%s отчет текущей налоговой формы(%s) поставлен в очередь на формирование.", action.getType().getName(), action.isManual()?"версия ручного ввода":"автоматическая версия"));
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
