package com.aplana.sbrf.taxaccounting.web.module.testpage.server;

import com.aplana.sbrf.taxaccounting.common.model.EventType;
import com.aplana.sbrf.taxaccounting.common.model.UserInfo;
import com.aplana.sbrf.taxaccounting.common.service.CommonServiceException;
import com.aplana.sbrf.taxaccounting.common.service.EventAuditService;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.AuthenticationUserDetailsServiceImpl;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.testpage.shared.DoEventAction;
import com.aplana.sbrf.taxaccounting.web.module.testpage.shared.DoEventResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author aivanov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DoEventHandler extends AbstractActionHandler<DoEventAction, DoEventResult> {

    @Autowired
    EventAuditService eventAuditService;

    @Autowired
    SecurityService userService;

    public DoEventHandler() {
        super(DoEventAction.class);
    }

    @Override
    public DoEventResult execute(DoEventAction action, ExecutionContext executionContext) throws ActionException {
        DoEventResult result = new DoEventResult();
        TAUserInfo systemUserInfo = userService.currentUserInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(systemUserInfo.getUser().getId());
        userInfo.setUserIp(systemUserInfo.getIp());
        EventType event = EventType.getByCode(Integer.valueOf(action.getCode()));
        try {
            eventAuditService.addAuditLog(event, userInfo, "Проверка АПИ событий");
        } catch (CommonServiceException e) {
            result.setMessage("Ошибка: " + e.getMessage());
            return result;
        }

        result.setMessage("Успех");
        return result;
    }


    @Override
    public void undo(DoEventAction deleteFormsSourseAction, DoEventResult deleteFormsSourceResult, ExecutionContext executionContext) throws ActionException {

    }
}
