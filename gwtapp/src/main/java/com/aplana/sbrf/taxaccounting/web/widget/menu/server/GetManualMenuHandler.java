package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetManualMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetManualMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetManualMenuHandler extends AbstractActionHandler<GetManualMenuAction, GetManualMenuResult> {

    public static final String MANUAL_FOR_USERS = "Руководство для бизнес-пользователей";
    public static final String MANUAL_FOR_CONF = "Руководство для Настройщика макетов";

    public GetManualMenuHandler() {
        super(GetManualMenuAction.class);
    }

    @Autowired
    private SecurityService securityService;

    @Override
    public GetManualMenuResult execute(GetManualMenuAction action, ExecutionContext context) throws ActionException {

        List<MenuItem> menuItems = new ArrayList<MenuItem>();

        GetManualMenuResult result = new GetManualMenuResult();

        TAUser currentUser = securityService.currentUserInfo().getUser();

        // Руководство пользователя
        if (currentUser.hasRoles(TARole.ROLE_ADMIN,
                TARole.N_ROLE_OPER, TARole.F_ROLE_OPER,
                TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.N_ROLE_CONF, TARole.F_ROLE_CONF)) {

            MenuItem manualMenu = new MenuItem("");

            if (currentUser.hasRoles(TARole.ROLE_ADMIN,
                    TARole.N_ROLE_OPER, TARole.F_ROLE_OPER,
                    TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                    TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
                manualMenu.getSubMenu().add(new MenuItem("Руководство пользователя", "resources/help_ndfl.pdf"));
            }

            if (currentUser.hasRoles(TARole.N_ROLE_CONF, TARole.F_ROLE_CONF)) {
                manualMenu.getSubMenu().add(new MenuItem("Руководство настройщика макетов", "resources/help_conf.pdf"));
            }

            menuItems.add(manualMenu);
        }

        result.setCanShowNotification(true);
        result.setMenuItems(menuItems);

        return result;
    }

    @Override
    public void undo(GetManualMenuAction action, GetManualMenuResult result, ExecutionContext context) throws ActionException {

    }
}
