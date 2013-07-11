package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.AuditToken;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.PeriodsTokens;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesTokens;
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

@Component
public class GetMainMenuActionHandler extends
		AbstractActionHandler<GetMainMenuAction, GetMainMenuResult> {

	private static final String CLEAR_CACHE_LINK = "cache/clear-cache";
	private static final String NUMBER_SIGN = "#";
	private static final String TYPE = "nType";

	public GetMainMenuActionHandler() {
		super(GetMainMenuAction.class);
	}

	@Autowired
	private SecurityService securityService;

	@Override
	public GetMainMenuResult execute(GetMainMenuAction action,
			ExecutionContext context) throws ActionException {

		List<MenuItem> menuItems = new ArrayList<MenuItem>();

		if (securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_OPERATOR)
				|| securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL)
				|| securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {

			// тут важен порядок, поэтому мы не можем просто пробежаться по значениям
			MenuItem taxMenu = new MenuItem("Налоги");

			taxMenu.getSubMenu().add(new MenuItem(TaxType.INCOME.getName(), "", TaxType.INCOME.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.VAT.getName(), "", TaxType.VAT.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.PROPERTY.getName(), "", TaxType.PROPERTY.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.TRANSPORT.getName(), "", TaxType.TRANSPORT.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.DEAL.getName(), "", TaxType.DEAL.name()));

			for (MenuItem menu : taxMenu.getSubMenu()) {
				menu.getSubMenu().add(new MenuItem("Налоговые формы", NUMBER_SIGN + FormDataListNameTokens.FORM_DATA_LIST
					+ ";" + TYPE + "=" + menu.getMeta()));
				if (!securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_OPERATOR)
						&& !menu.getName().equals(TaxType.DEAL.getName())) {
					menu.getSubMenu().add(new MenuItem("Декларации", NUMBER_SIGN + DeclarationListNameTokens.DECLARATION_LIST
						+ ";" + TYPE + "=" + menu.getMeta()));
				}
				menu.getSubMenu().add(new MenuItem("Ведение периодов", NUMBER_SIGN + PeriodsTokens.PERIODS
						+ ";" + TYPE + "=" + menu.getMeta()));
			}
			menuItems.add(taxMenu);

			MenuItem settingMenuItem = new MenuItem("Настройки");
			settingMenuItem.getSubMenu().add(new MenuItem("Движение документов"));
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
		if (securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONF)) {
			MenuItem settingMenuItem = new MenuItem("Настройки");
			settingMenuItem.getSubMenu().add(
					new MenuItem("Шаблоны налоговых форм", NUMBER_SIGN + AdminConstants.NameTokens.adminPage));
			settingMenuItem.getSubMenu().add(
					new MenuItem("Шаблоны деклараций", NUMBER_SIGN + DeclarationTemplateTokens.declarationTemplateList));
			settingMenuItem.getSubMenu().add(new MenuItem("Сбросить кэш", CLEAR_CACHE_LINK));

			menuItems.add(settingMenuItem);
		}
        if (securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_ADMIN)) {
            MenuItem settingMenuItem = new MenuItem("Настройки");
            settingMenuItem.getSubMenu().add(new MenuItem("Пользователи системы", NUMBER_SIGN + UserListTokens.secuserPage));
            settingMenuItem.getSubMenu().add(new MenuItem("Журнал аудита", NUMBER_SIGN + AuditToken.AUDIT));
            menuItems.add(settingMenuItem);
        }

		if (securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_ADMIN)
				|| securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL)
				|| securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
			MenuItem settingMenuItem = new MenuItem("Администрирование");
			settingMenuItem.getSubMenu().add(
					new MenuItem("Назначение форм и деклараций", NUMBER_SIGN + "!destination"));
			settingMenuItem.getSubMenu().add(
					new MenuItem("Указание форм-источников", NUMBER_SIGN + SourcesTokens.sources));
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
