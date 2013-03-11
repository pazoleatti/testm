package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Component
public class GetMainMenuActionHandler extends
		AbstractActionHandler<GetMainMenuAction, GetMainMenuResult> {

	public GetMainMenuActionHandler() {
		super(GetMainMenuAction.class);
	}

	@Autowired
	private SecurityService securityService;

	@Override
	public GetMainMenuResult execute(GetMainMenuAction action,
			ExecutionContext context) throws ActionException {

		List<MenuItem> menuItems = new ArrayList<MenuItem>();

		if (securityService.currentUser().hasRole(TARole.ROLE_OPERATOR)
				|| securityService.currentUser().hasRole(TARole.ROLE_CONTROL)
				|| securityService.currentUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
			/*
			 *	тут важен порядок, поэтому мы не можем просто пробежаться по значениям
			 */
			menuItems.add(new MenuItem(TaxType.INCOME.getName(), TaxType.INCOME.name()));
			menuItems.add(new MenuItem(TaxType.VAT.getName(), TaxType.VAT.name()));
			menuItems.add(new MenuItem(TaxType.PROPERTY.getName(), TaxType.PROPERTY.name()));
			menuItems.add(new MenuItem(TaxType.TRANSPORT.getName(), TaxType.TRANSPORT.name()));

			for (MenuItem menu : menuItems) {
				menu.getSubMenu().add(new MenuItem("Налоговые формы", FormDataListNameTokens.FORM_DATA_LIST));
				if (!securityService.currentUser().hasRole(TARole.ROLE_OPERATOR)) {
					menu.getSubMenu().add(new MenuItem("Декларации", DeclarationListNameTokens.DECLARATION_LIST));
				}
			}

			MenuItem settingMenuItem = new MenuItem("Настройки", "");
			settingMenuItem.getSubMenu().add(new MenuItem("Движение документов", "Empty"));
			menuItems.add(settingMenuItem);

		}
		if (securityService.currentUser().hasRole(TARole.ROLE_CONF)) {
			MenuItem settingMenuItem = new MenuItem("Настройки", "");
			settingMenuItem.getSubMenu().add(new MenuItem("Шаблоны налоговых форм", AdminConstants.NameTokens.adminPage));
			settingMenuItem.getSubMenu().add(new MenuItem("Шаблоны деклараций", DeclarationTemplateTokens.declarationTemplateList));
			menuItems.add(settingMenuItem);
		}

		GetMainMenuResult result = new GetMainMenuResult();
		result.setMenuItems(menuItems);

		return result;

	}

	@Override
	public void undo(GetMainMenuAction action, GetMainMenuResult result,
			ExecutionContext context) throws ActionException {
	}

}
