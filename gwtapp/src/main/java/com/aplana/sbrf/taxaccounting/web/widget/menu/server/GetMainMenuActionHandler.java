package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.AuditToken;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.BookerStatementsTokens;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client.DepartmentConfigTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.members.client.MembersTokens;
import com.aplana.sbrf.taxaccounting.web.module.migration.client.MigrationTokens;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.PeriodsTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.client.SchedulerTokens;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesTokens;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.TaxFormNominationToken;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, реализующий логику определения доступности пунктов меню навигации
 */
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

        TAUser currentUser = securityService.currentUserInfo().getUser();

        // НАЛОГИ
		if (currentUser.hasRole(TARole.ROLE_OPER)
				|| currentUser.hasRole(TARole.ROLE_CONTROL)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
				|| currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {

			// тут важен порядок, поэтому мы не можем просто пробежаться по значениям
			MenuItem taxMenu = new MenuItem("Налоги");
			taxMenu.getSubMenu().add(new MenuItem(TaxType.INCOME.getName(), "", TaxType.INCOME.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.VAT.getName(), "", TaxType.VAT.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.PROPERTY.getName(), "", TaxType.PROPERTY.name()));
			taxMenu.getSubMenu().add(new MenuItem(TaxType.TRANSPORT.getName(), "", TaxType.TRANSPORT.name()));
			taxMenu.getSubMenu().add(new MenuItem("Учет КС", "", TaxType.DEAL.name()));

			for (MenuItem menu : taxMenu.getSubMenu()) {
                boolean isDeal = menu.getMeta().equals(TaxType.DEAL.name());
                String formItemName = isDeal ? "Список форм" : "Налоговые формы";
                String declarationItemName = isDeal ? "Уведомления" : "Декларации";

				menu.getSubMenu().add(new MenuItem(formItemName, NUMBER_SIGN + FormDataListNameTokens.FORM_DATA_LIST
					+ ";" + TYPE + "=" + menu.getMeta()));
				if (currentUser.hasRole(TARole.ROLE_CONTROL)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {

					menu.getSubMenu().add(new MenuItem(declarationItemName, NUMBER_SIGN
                            + DeclarationListNameTokens.DECLARATION_LIST + ";"
                            + TYPE + "=" + menu.getMeta()));

                    if (!currentUser.hasRole(TARole.ROLE_CONTROL) ) {
                        menu.getSubMenu().add(new MenuItem("Ведение периодов", NUMBER_SIGN + PeriodsTokens.PERIODS
                                + ";" + TYPE + "=" + menu.getMeta()));

                        if (currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                            menu.getSubMenu().add(
                                    new MenuItem("Назначение форм и деклараций",
                                            NUMBER_SIGN + TaxFormNominationToken.taxFormNomination + ";"
                                                    + TYPE + "=" + menu.getMeta() + ";"
                                                    + TaxFormNominationToken.isForm + "=" + true));
                            menu.getSubMenu().add(
                                    new MenuItem("Назначение источников-приёмников",
                                            NUMBER_SIGN + SourcesTokens.SOURCES + ";"
                                                    + TYPE + "=" + menu.getMeta() + ";"
                                                    + SourcesTokens.FORM_FLAG + "=" + true));
                        }
                    }
				}
			}
			menuItems.add(taxMenu);
		}

        // НСИ
        MenuItem nsiMenuItem = new MenuItem("НСИ");
        nsiMenuItem.getSubMenu().add(new MenuItem("Справочники", NUMBER_SIGN + RefBookListTokens.REFBOOK_LIST));

        if (currentUser.hasRole(TARole.ROLE_CONTROL)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            nsiMenuItem.getSubMenu().add(new MenuItem("Настройки подразделений", NUMBER_SIGN
                    + DepartmentConfigTokens.departamentConfig));
        }
        if (currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            nsiMenuItem.getSubMenu().add(new MenuItem("Бухгалтерская отчётность", NUMBER_SIGN
                    + BookerStatementsTokens.bookerStatements));
        }
        menuItems.add(nsiMenuItem);

        // АДМИНИСТРИРОВАНИЕ
        if (currentUser.hasRole(TARole.ROLE_ADMIN)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            MenuItem adminMenuItem = new MenuItem("Администрирование");
            adminMenuItem.getSubMenu().add(new MenuItem("Журнал аудита", NUMBER_SIGN + AuditToken.AUDIT));
            adminMenuItem.getSubMenu().add(new MenuItem("Список пользователей Системы", NUMBER_SIGN
                    + MembersTokens.MEMBERS));

            if (currentUser.hasRole(TARole.ROLE_ADMIN)) {
                adminMenuItem.getSubMenu().add(new MenuItem("Конфигурационные параметры", NUMBER_SIGN
                        + ConfigurationPresenter.TOKEN));
                adminMenuItem.getSubMenu().add(new MenuItem("Импорт данных", NUMBER_SIGN
                        + MigrationTokens.migration));
                adminMenuItem.getSubMenu().add(new MenuItem("Планировщик задач", NUMBER_SIGN
                        + SchedulerTokens.taskList));
            }
            menuItems.add(adminMenuItem);
        }

        // НАСТРОЙКА МАКЕТОВ
		if (currentUser.hasRole(TARole.ROLE_CONF)) {
			MenuItem configMenuItem = new MenuItem("Настройка макетов");
			configMenuItem.getSubMenu().add(new MenuItem("Макеты налоговых форм", NUMBER_SIGN
                    + AdminConstants.NameTokens.adminPage));
			configMenuItem.getSubMenu().add(new MenuItem("Макеты деклараций", NUMBER_SIGN
                    + DeclarationTemplateTokens.declarationTemplateList));
			configMenuItem.getSubMenu().add(new MenuItem("Сбросить кэш", CLEAR_CACHE_LINK));
			menuItems.add(configMenuItem);
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
