package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.PrintAction;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.PrintResult;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.SaveUserAction;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.SaveUserResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class SaveUserHandler extends AbstractActionHandler<SaveUserAction, SaveUserResult> {

	@Autowired
	private TAUserService taUserService;
    @Autowired
    private SecurityService securityService;

	public SaveUserHandler() {
		super(SaveUserAction.class);
	}

	@Override
	public SaveUserResult execute(SaveUserAction action, ExecutionContext executionContext) throws ActionException {
        SaveUserResult result = new SaveUserResult();
        List<String> fields = new ArrayList<String>();
        TAUserView user = action.getTaUserView();
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            fields.add("Логин");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            fields.add("Полное имя пользователя");
        }
        if (user.getDepId() == null) {
            fields.add("Подразделение");
        }
        if (!fields.isEmpty()) {
            throw new ActionException(String.format(fields.size()==1?"Поле %s обязательно для заполнения.":"Поля %s обязательны для заполнения.", StringUtils.join(fields.toArray(), ", ", "\"")));
        }

        if (action.getTaUserView().getId() == null) {
            taUserService.createUser(action.getTaUserView());
        } else {
            taUserService.updateUser(action.getTaUserView());
        }
        return result;
    }

	@Override
	public void undo(SaveUserAction action, SaveUserResult result, ExecutionContext executionContext) throws ActionException {}
}
