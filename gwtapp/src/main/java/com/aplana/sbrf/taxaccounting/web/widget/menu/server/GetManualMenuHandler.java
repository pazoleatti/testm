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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
@Component
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
        if (currentUser.hasRole(TARole.ROLE_ADMIN)
                || currentUser.hasRole(TARole.ROLE_OPER)
                || currentUser.hasRole(TARole.ROLE_CONTROL)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                || currentUser.hasRole(TARole.ROLE_CONF)) {

            MenuItem manualMenu = new MenuItem("");

            MenuItem menuItem = new MenuItem("Учет налогов");

            menuItem.getSubMenu().add(new MenuItem("Руководство для бизнес-пользователей", "resources/help_un.pdf"));
            if (currentUser.hasRole(TARole.ROLE_CONF)) {
                menuItem.getSubMenu().add(new MenuItem("Руководство для Настройщика макетов", "resources/help_conf.pdf"));
            }

            manualMenu.getSubMenu().add(menuItem);
            manualMenu.getSubMenu().add(new MenuItem("Учет КС", "resources/help_uks.pdf", "Учет КС"));
            menuItems.add(manualMenu);
        }

        result.setMenuItems(menuItems);

        return result;
    }

    @Override
    public void undo(GetManualMenuAction action, GetManualMenuResult result, ExecutionContext context) throws ActionException {

    }
}
