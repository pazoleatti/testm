package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.userlist.client.UserListTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formdataimport.client.FormDataImportPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

@Component
public class GetMainMenuActionHandler extends
		AbstractActionHandler<GetMainMenuAction, GetMainMenuResult> {

	private static final String CLEAR_CACHE_LINK = "cache/clear-cache";
	private static final String NUMBER_SIGN = "#";

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
				menu.getSubMenu().add(new MenuItem("Налоговые формы", NUMBER_SIGN + FormDataListNameTokens.FORM_DATA_LIST));
				if (!securityService.currentUser().hasRole(TARole.ROLE_OPERATOR)) {
					menu.getSubMenu().add(new MenuItem("Декларации", NUMBER_SIGN + DeclarationListNameTokens.DECLARATION_LIST));
				}
			}

			MenuItem settingMenuItem = new MenuItem("Настройки", "");
			settingMenuItem.getSubMenu().add(new MenuItem("Движение документов", "Empty"));
			settingMenuItem.getSubMenu().add(new MenuItem("Тест РНУ 26",  
					new StringBuilder(NUMBER_SIGN)
					.append(FormDataImportPresenter.FDIMPORT)
					.append(";").append(FormDataImportPresenter.DEPARTMENT_ID).append("=4")
					.append(";").append(FormDataImportPresenter.FORM_DATA_TEMPLATE_ID).append("=325")
					.append(";").append(FormDataImportPresenter.FORM_DATA_RPERIOD_ID).append("=101")
					.append(";").append(FormDataImportPresenter.FORM_DATA_KIND_ID).append("=1")
					.toString()
					));
			menuItems.add(settingMenuItem);

		}
		if (securityService.currentUser().hasRole(TARole.ROLE_CONF)) {
			MenuItem settingMenuItem = new MenuItem("Настройки", "");
			settingMenuItem.getSubMenu().add(new MenuItem("Шаблоны налоговых форм", NUMBER_SIGN + AdminConstants.NameTokens.adminPage));
			settingMenuItem.getSubMenu().add(new MenuItem("Шаблоны деклараций", NUMBER_SIGN + DeclarationTemplateTokens.declarationTemplateList));
			settingMenuItem.getSubMenu().add(new MenuItem("Сбросить кэш", CLEAR_CACHE_LINK));

			menuItems.add(settingMenuItem);
		}
        if (securityService.currentUser().hasRole(TARole.ROLE_ADMIN)) {
            MenuItem settingMenuItem = new MenuItem("Настройки", "");
            settingMenuItem.getSubMenu().add(new MenuItem("Пользователи системы", NUMBER_SIGN + UserListTokens.secuserPage));
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
