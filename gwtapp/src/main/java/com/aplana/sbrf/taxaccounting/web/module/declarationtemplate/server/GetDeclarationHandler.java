package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetDeclarationHandler extends AbstractActionHandler<GetDeclarationAction, GetDeclarationResult> {
    @Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private TAUserService taUserService;

    public GetDeclarationHandler() {
        super(GetDeclarationAction.class);
    }

    @Override
    public GetDeclarationResult execute(GetDeclarationAction action, ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();

        GetDeclarationResult result = new GetDeclarationResult();
        fillLockData(action, userInfo, result);
		DeclarationTemplate declarationTemplate = declarationTemplateService.get(action.getId());
        declarationTemplate.setCreateScript(declarationTemplateService.getDeclarationTemplateScript(action.getId()));
		result.setDeclarationTemplate(declarationTemplate);
        result.setEndDate(declarationTemplateService.getDTEndDate(declarationTemplate.getId()));
		return result;
    }

    /**
     * Блокирует макет при необходимости, заполняет состояние блокировки
     *
     * @param action
     * @param userInfo
     * @param result
     */
    private void fillLockData(GetDeclarationAction action, TAUserInfo userInfo,
                              GetDeclarationResult result) {
        LockData lockInformation = declarationTemplateService.getObjectLock(action
                .getId(), securityService.currentUserInfo());
        if (lockInformation != null) {
            // Если данная форма уже заблокирована
            result.setLockedByUser(taUserService.getUser(lockInformation.getUserId()).getName());
            result.setLockDate(getFormedDate(lockInformation.getDateLock()));
            if (lockInformation.getUserId() != userInfo.getUser().getId()) {
                result.setLockedByAnotherUser(true);
            }
        }
        if (!result.isLockedByAnotherUser()) {
            declarationTemplateService.lock(action.getId(), userInfo);
        }
    }

    private static String getFormedDate(Date dateToForm) {
        // Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        formatter.format(dateToForm);
        return (formatter.format(dateToForm));
    }

    @Override
    public void undo(GetDeclarationAction action, GetDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
