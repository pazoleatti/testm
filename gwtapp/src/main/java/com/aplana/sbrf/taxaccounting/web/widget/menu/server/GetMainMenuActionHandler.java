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
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client.DepartmentConfigPropertyTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.client.IfrsTokens;
import com.aplana.sbrf.taxaccounting.web.module.lock.client.LockTokens;
import com.aplana.sbrf.taxaccounting.web.module.members.client.MembersTokens;
import com.aplana.sbrf.taxaccounting.web.module.migration.client.MigrationTokens;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.PeriodsTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.client.SchedulerTokens;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.client.ScriptsImportTokens;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesTokens;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.TaxFormNominationToken;
import com.aplana.sbrf.taxaccounting.web.module.testpage.client.TestPageTokens;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.UploadTransportDataTokens;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Класс, реализующий логику определения доступности пунктов меню навигации
 * http://conf.aplana.com/pages/viewpage.action?pageId=11382816
 */
@Component
public class GetMainMenuActionHandler extends
		AbstractActionHandler<GetMainMenuAction, GetMainMenuResult> {

	private static final String CLEAR_CACHE_LINK = "cache/clear-cache";
	private static final String NUMBER_SIGN = "#";
    private static final String DOWNLOAD_ALL_TEMPLATES = "download/formTemplate/downloadAll";
	private static final String TYPE = "nType";

	public GetMainMenuActionHandler() {
		super(GetMainMenuAction.class);
	}

	@Autowired
	private SecurityService securityService;

    @Autowired
    @Qualifier("versionInfoProperties")
    private Properties versionInfoProperties;

	@Override
	public GetMainMenuResult execute(GetMainMenuAction action,
			ExecutionContext context) throws ActionException {

		List<MenuItem> menuItems = new ArrayList<MenuItem>();

        TAUser currentUser = securityService.currentUserInfo().getUser();

        // НАЛОГИ
        if (currentUser.hasRole(TARole.ROLE_OPER)
                || currentUser.hasRole(TARole.ROLE_CONTROL)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                || currentUser.hasRole(TARole.ROLE_GARANT)) {

            // тут важен порядок, поэтому мы не можем просто пробежаться по значениям
            MenuItem taxMenu = new MenuItem("Налоги");

            if (currentUser.hasRole(TARole.ROLE_OPER)
                    || currentUser.hasRole(TARole.ROLE_CONTROL)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                taxMenu.getSubMenu().add(new MenuItem(TaxType.NDFL.getName(), "", TaxType.NDFL.name()));
                taxMenu.getSubMenu().add(new MenuItem(TaxType.PFR.getName(), "", TaxType.PFR.name()));

                for (MenuItem menu : taxMenu.getSubMenu()) {
                    // декларации
                    if (currentUser.hasRole(TARole.ROLE_CONTROL)
                            || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                            || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                        menu.getSubMenu().add(new MenuItem("Формы", NUMBER_SIGN
                                + DeclarationListNameTokens.DECLARATION_LIST + ";"
                                + TYPE + "=" + menu.getMeta()));
                    }

                    // ведение периодов
                    if (currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                            || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                        menu.getSubMenu().add(new MenuItem("Ведение периодов", NUMBER_SIGN + PeriodsTokens.PERIODS
                                + ";" + TYPE + "=" + menu.getMeta()));
                    }

                    // настройки подразделений
                    if (currentUser.hasRole(TARole.ROLE_CONTROL)
                            || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                            || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                        menu.getSubMenu().add(new MenuItem("Настройки подразделений", NUMBER_SIGN
                            + DepartmentConfigPropertyTokens.departamentConfig + ";" + TYPE + "=" + menu.getMeta()));
                    }

                    // настройки форм и подразделений, назначение источников-приемников
                    if (currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                            || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                        menu.getSubMenu().add(
                                new MenuItem("Назначение форм",
                                        NUMBER_SIGN + TaxFormNominationToken.taxFormNomination + ";"
                                                + TYPE + "=" + menu.getMeta() + ";"
                                                + TaxFormNominationToken.isForm + "=" + true));

                        menu.getSubMenu().add(
                                new MenuItem("Назначение источников-приёмников",
                                        NUMBER_SIGN + SourcesTokens.SOURCES + ";"
                                                + TYPE + "=" + menu.getMeta()));
                    }
                }
            }

            if (currentUser.hasRole(TARole.ROLE_OPER)
                    || currentUser.hasRole(TARole.ROLE_CONTROL)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                MenuItem menuItem = new MenuItem("Сервис", "", "Сервис");
                menuItem.getSubMenu().add(new MenuItem("Загрузить ТФ", NUMBER_SIGN + UploadTransportDataTokens.uploadTransportData));
                taxMenu.getSubMenu().add(menuItem);
            }

            menuItems.add(taxMenu);
        }

        // НСИ
        if (currentUser.hasRole(TARole.ROLE_CONTROL_UNP) || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_OPER) || currentUser.hasRole(TARole.ROLE_CONTROL)){
            MenuItem nsiMenuItem = new MenuItem("НСИ");
            nsiMenuItem.getSubMenu().add(new MenuItem("Справочники", NUMBER_SIGN + RefBookListTokens.REFBOOK_LIST));

            if (currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                nsiMenuItem.getSubMenu().add(new MenuItem("Бухгалтерская отчётность", NUMBER_SIGN
                        + BookerStatementsTokens.bookerStatements));
            }
            menuItems.add(nsiMenuItem);
        }

        // АДМИНИСТРИРОВАНИЕ
        if (currentUser.hasRole(TARole.ROLE_OPER)
                || currentUser.hasRole(TARole.ROLE_CONTROL)
                || currentUser.hasRole(TARole.ROLE_ADMIN)
                || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                || currentUser.hasRole(TARole.ROLE_CONF)) {

            MenuItem adminMenuItem = new MenuItem("Администрирование");
            if (currentUser.hasRole(TARole.ROLE_ADMIN) || currentUser.hasRole(TARole.ROLE_OPER) ||
                    currentUser.hasRole(TARole.ROLE_CONTROL) || currentUser.hasRole(TARole.ROLE_CONTROL_NS) ||
                    currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                adminMenuItem.getSubMenu().add(new MenuItem("Список блокировок", NUMBER_SIGN
                        + LockTokens.lockList));
            }

            if (currentUser.hasRole(TARole.ROLE_ADMIN)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                    || currentUser.hasRole(TARole.ROLE_OPER)
                    || currentUser.hasRole(TARole.ROLE_CONTROL)) {
                adminMenuItem.getSubMenu().add(new MenuItem("Журнал аудита", NUMBER_SIGN + AuditToken.AUDIT));
            }

            if (currentUser.hasRole(TARole.ROLE_ADMIN)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                    || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)
                    || currentUser.hasRole(TARole.ROLE_CONF)) {

                if (currentUser.hasRole(TARole.ROLE_ADMIN)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_NS)
                        || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                    adminMenuItem.getSubMenu().add(new MenuItem("Список пользователей", NUMBER_SIGN
                            + MembersTokens.MEMBERS));
                }

                if (currentUser.hasRole(TARole.ROLE_ADMIN)) {
                    adminMenuItem.getSubMenu().add(new MenuItem("Конфигурационные параметры", NUMBER_SIGN
                            + ConfigurationPresenter.TOKEN));
                    adminMenuItem.getSubMenu().add(new MenuItem("Миграция данных", NUMBER_SIGN
                            + MigrationTokens.migration));
                    adminMenuItem.getSubMenu().add(new MenuItem("Планировщик задач", NUMBER_SIGN
                            + SchedulerTokens.taskList));
                }

                if (currentUser.hasRole(TARole.ROLE_CONF)) {
                    MenuItem templateMenu = new MenuItem("Настройки", "", null);
                    adminMenuItem.getSubMenu().add(templateMenu);
                    templateMenu.getSubMenu().add(new MenuItem("Макеты налоговых форм", NUMBER_SIGN
                            + AdminConstants.NameTokens.adminPage));
                    templateMenu.getSubMenu().add(new MenuItem("Макеты деклараций", NUMBER_SIGN
                            + DeclarationTemplateTokens.declarationTemplateList));
                    templateMenu.getSubMenu().add(new MenuItem("Справочники", NUMBER_SIGN
                            + RefBookListTokens.REFBOOK_LIST_ADMIN));
                    templateMenu.getSubMenu().add(new MenuItem("Сбросить кэш", CLEAR_CACHE_LINK, "", "_blank"));
                    templateMenu.getSubMenu().add(new MenuItem("Экспорт макетов", DOWNLOAD_ALL_TEMPLATES));
                    templateMenu.getSubMenu().add(new MenuItem("Импорт скриптов", NUMBER_SIGN
                            + ScriptsImportTokens.SCRIPTS_IMPORT));
                }

                // в банке все равно такого пользователя не будет, если надо убрать скажите мне aivanov
                if("god".equals(currentUser.getLogin())){
                    adminMenuItem.getSubMenu().add(new MenuItem("Тестовая страница", NUMBER_SIGN
                            + TestPageTokens.TEST_PAGE));
                }

            }
            if (!adminMenuItem.getSubMenu().isEmpty())
                menuItems.add(adminMenuItem);

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
