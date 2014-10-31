package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CreateIfrsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CreateIfrsDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class CreateIfrsDataHandler extends AbstractActionHandler<CreateIfrsDataAction, CreateIfrsDataResult> {

    @Autowired
    private IfrsDataService ifrsDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    private LockDataService lockDataService;
    public CreateIfrsDataHandler() {
        super(CreateIfrsDataAction.class);
    }

    @Override
    public CreateIfrsDataResult execute(CreateIfrsDataAction action, ExecutionContext executionContext) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();

        CreateIfrsDataResult result = new CreateIfrsDataResult();
        String key = ifrsDataService.generateTaskKey(action.getReportPeriodId());
        LockData lockData = null;//lockDataService.lock(key, userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 24); //ставим такую блокировку т.к. стандартная на 1 час
        if (lockData == null) {
            try {
                ifrsDataService.create(action.getReportPeriodId());
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("reportPeriodId", action.getReportPeriodId());
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE_END.name(), lockDataService.getLock(key).getDateBefore());

            } catch (Exception e) {

            }
        }
        return result;
    }

    @Override
    public void undo(CreateIfrsDataAction action, CreateIfrsDataResult result, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
