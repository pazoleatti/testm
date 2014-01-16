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
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.HistoryBusinessToken;
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
        TAUser currentUser =  securityService.currentUserInfo().getUser();

        // Налоги
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
			taxMenu.getSubMenu().add(new MenuItem("Учёт КС", "", TaxType.DEAL.name()));

			for (MenuItem menu : taxMenu.getSubMenu()) {
                boolean isDeal = menu.getMeta().equals(TaxType.DEAL.name());
                String formItemName = isDeal ? "Список форм" : "Налоговые формы";
                String declarationItemName = isDeal ? "Уведомления" : "Декларации";
                String nominationItemName = isDeal ? "Назначение форм и уведомлений" : "Назначение форм и деклараций";

                // добавить налоговые формы
				menu.getSubMenu().add(new MenuItem(formItemName, NUMBER_SIGN + FormDataListNameTokens.FORM_DATA_LIST
					+ ";" + TYPE + "=" + menu.getMeta()));

                // добавить "декларации / уведомления"
				if (currentUser.hasRole(TARole.ROLE_CONTROL)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
					menu.getSubMenu().add(new MenuItem(declarationItemName, NUMBER_SIGN + DeclarationListNameTokens.DECLARATION_LIST
						+ ";" + TYPE + "=" + menu.getMeta()));
                }

                if (currentUser.hasRole(TARole.ROLE_CONTROL)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                    // добавить "ведение периодов"
                    menu.getSubMenu().add(new MenuItem("Ведение периодов", NUMBER_SIGN + PeriodsTokens.PERIODS
                            + ";" + TYPE + "=" + menu.getMeta()));

                    // добавить "Назначение форм и деклараций"
                    menu.getSubMenu().add(new MenuItem(nominationItemName, NUMBER_SIGN + TaxFormNominationToken.taxFormNomination
                            + ";" + TaxFormNominationToken.isForm + "=" + true + ";" + TYPE + "=" + menu.getMeta()));

                    // добавить "Назначение источников-приёмников"
                    menu.getSubMenu().add(new MenuItem("Назначение источников-приёмников", NUMBER_SIGN + SourcesTokens.SOURCES
                            + ";" + SourcesTokens.FORM_FLAG + "=" + true + ";" + TYPE + "=" + menu.getMeta()));
				}
			}
			menuItems.add(taxMenu);

			/*settingMenuItem.getSubMenu().add(new MenuItem("Тест РНУ 26",
					new StringBuilder(NUMBER_SIGN)
					.append(FormDataImportPresenter.FDIMPORT)
					.append(";").append(FormDataImportPresenter.DEPARTMENT_ID).append("=4")
					.append(";").append(FormDataImportPresenter.FORM_DATA_TEMPLATE_ID).append("=325")
					.append(";").append(FormDataImportPresenter.FORM_DATA_RPERIOD_ID).append("=101")
					.append(";").append(FormDataImportPresenter.FORM_DATA_KIND_ID).append("=1")
					.toString()
					));*/
		}
		
		
        // нси
        MenuItem nsiMenuItem = new MenuItem("НСИ");
        // добавить "Справочники"
        nsiMenuItem.getSubMenu().add(new MenuItem("Справочники", NUMBER_SIGN + RefBookListTokens.REFBOOK_LIST));
        if (currentUser.hasRole(TARole.ROLE_CONTROL)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {

            // добавить "Настройка подразделений"
            nsiMenuItem.getSubMenu().add(new MenuItem("Настройка подразделений", NUMBER_SIGN + DepartmentConfigTokens.departamentConfig));

            // добавить "Бухгалтерская отчётность"
	        if (currentUser.hasRole(TARole.ROLE_CONTROL_NS) ||
                    currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                nsiMenuItem.getSubMenu().add(new MenuItem("Бухгалтерская отчётность", NUMBER_SIGN + BookerStatementsTokens.bookerStatements));
	        }
        }
        menuItems.add(nsiMenuItem);

        // Администрирование
        if (currentUser.hasRole(TARole.ROLE_ADMIN)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            MenuItem settingMenuItem = new MenuItem("Администрирование");

            // добавить "Журнал аудита"
            if (currentUser.hasRole(TARole.ROLE_ADMIN)) {
                settingMenuItem.getSubMenu().add(new MenuItem("Журнал аудита", NUMBER_SIGN + AuditToken.AUDIT));
            } else {
                settingMenuItem.getSubMenu().add(new MenuItem("Журнал истории изменений налоговых форм / декларации",
                        NUMBER_SIGN + HistoryBusinessToken.HISTORY_BUSINESS));
            }

            // добавить "Список пользователей"
            settingMenuItem.getSubMenu().add(new MenuItem("Список пользователей", NUMBER_SIGN + MembersTokens.MEMBERS));

            // добавить "Конфигурационные параметры"
            if (currentUser.hasRole(TARole.ROLE_ADMIN)) {
                settingMenuItem.getSubMenu().add(new MenuItem("Конфигурационные параметры",	NUMBER_SIGN + ConfigurationPresenter.TOKEN));
                settingMenuItem.getSubMenu().add(new MenuItem("Миграция данных", NUMBER_SIGN + MigrationTokens.migration));
                settingMenuItem.getSubMenu().add(new MenuItem("Планировщик задач", NUMBER_SIGN + SchedulerTokens.taskList));
            }
            menuItems.add(settingMenuItem);
        }

        // Настройка макетов
        if (currentUser.hasRole(TARole.ROLE_CONF)) {
            MenuItem confMenuItem = new MenuItem("Настройка макетов");
            confMenuItem.getSubMenu().add(new MenuItem("Макеты налоговых форм", NUMBER_SIGN + AdminConstants.NameTokens.adminPage));
            confMenuItem.getSubMenu().add(new MenuItem("Макеты деклараций", NUMBER_SIGN + DeclarationTemplateTokens.declarationTemplateList));
            confMenuItem.getSubMenu().add(new MenuItem("Сбросить кэш", CLEAR_CACHE_LINK));
            menuItems.add(confMenuItem);
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
