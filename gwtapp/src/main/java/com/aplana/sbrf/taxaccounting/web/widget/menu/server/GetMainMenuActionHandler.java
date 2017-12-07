package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.client.CommonParameterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client.DepartmentConfigPropertyTokens;
import com.aplana.sbrf.taxaccounting.web.module.members.client.MembersTokens;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.PeriodsTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.client.SchedulerTokens;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.client.ScriptsImportTokens;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.TaxFormNominationToken;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс, реализующий логику определения доступности пунктов меню навигации
 * http://conf.aplana.com/pages/viewpage.action?pageId=11382816
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetMainMenuActionHandler extends
		AbstractActionHandler<GetMainMenuAction, GetMainMenuResult> {

	private static final String CLEAR_CACHE_LINK = "controller/actions/cache/clear-cache";
	private static final String NUMBER_SIGN = "#";
    private static final String DOWNLOAD_ALL_TEMPLATES = "controller/actions/declarationTemplate/downloadAll";
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

        securityService.updateUserRoles();
        TAUser currentUser = securityService.currentUserInfo().getUser();

        // НАЛОГИ
        if (currentUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS,
                TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.ROLE_ADMIN)) {
            // тут важен порядок, поэтому мы не можем просто пробежаться по значениям
            MenuItem taxMenu = new MenuItem("Налоги");
            if (currentUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS,
                    TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
                for (TaxType taxType : Arrays.asList(TaxType.NDFL)) {
                    if (currentUser.hasRoles(taxType, TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP,
                            TARole.F_ROLE_OPER, TARole.F_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_UNP)) {
                        MenuItem menu = new MenuItem(taxType.getName(), "", taxType.name());
                        taxMenu.getSubMenu().add(menu);

                        // формы
                        menu.getSubMenu().add(new MenuItem("Формы", "index.html#/taxes/ndflJournal"));

                        // ведение периодов
                        menu.getSubMenu().add(new MenuItem("Ведение периодов", NUMBER_SIGN + PeriodsTokens.PERIODS
                                + ";" + TYPE + "=" + menu.getMeta()));

                        // настройки подразделений
                        // настройки форм и подразделений, назначение источников-приемников
                        menu.getSubMenu().add(new MenuItem("Настройки подразделений", NUMBER_SIGN
                                + DepartmentConfigPropertyTokens.departamentConfig + ";" + TYPE + "=" + menu.getMeta()));
                        menu.getSubMenu().add(
                                new MenuItem("Назначение форм",
                                        NUMBER_SIGN + TaxFormNominationToken.taxFormNomination + ";"
                                                + TYPE + "=" + menu.getMeta() + ";"
                                                + TaxFormNominationToken.isForm + "=" + false));
                        if (currentUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                                TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
                            if (taxType.equals(TaxType.NDFL)) {
                                menu.getSubMenu().add(new MenuItem("Отчетность", "index.html#/taxes/ndflReportJournal"));
                            }
                        }
                    }
                }
            }

            MenuItem menuItem = new MenuItem("Сервис", "", "Сервис");
            menuItem.getSubMenu().add(new MenuItem("Загрузить файлы", "index.html#/taxes/service/uploadTransportData"));
            taxMenu.getSubMenu().add(menuItem);

            if (currentUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS,
                    TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
                taxMenu.getSubMenu().add(new MenuItem("Общие параметры", "index.html#/taxes/commonParams/editCommonParams"));
            }
            menuItems.add(taxMenu);
        }

        // НСИ
        if (currentUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            MenuItem nsiMenuItem = new MenuItem("НСИ");
            nsiMenuItem.getSubMenu().add(new MenuItem("Справочники", NUMBER_SIGN + RefBookListTokens.REFBOOK_LIST));

            menuItems.add(nsiMenuItem);
        }

        // АДМИНИСТРИРОВАНИЕ
        if (currentUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP, TARole.N_ROLE_CONF, TARole.F_ROLE_CONF, TARole.ROLE_ADMIN)) {

            MenuItem adminMenuItem = new MenuItem("Администрирование");
            if (currentUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                    TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP, TARole.ROLE_ADMIN)) {
                adminMenuItem.getSubMenu().add(new MenuItem("Список блокировок", "index.html#/administration/locks"));
                adminMenuItem.getSubMenu().add(new MenuItem("Асинхронные задачи", "index.html#/administration/async"));
            }

            if (currentUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP, TARole.ROLE_ADMIN)) {
                adminMenuItem.getSubMenu().add(new MenuItem("Список пользователей", NUMBER_SIGN
                        + MembersTokens.MEMBERS));
            }

            if (currentUser.hasRole(TARole.ROLE_ADMIN)) {
                adminMenuItem.getSubMenu().add(new MenuItem("Конфигурационные параметры", NUMBER_SIGN
                        + ConfigurationPresenter.TOKEN));
                adminMenuItem.getSubMenu().add(new MenuItem("Планировщик задач", "index.html#/administration/schedulerTaskList"));
            }

            if (currentUser.hasRoles(TARole.N_ROLE_CONF, TARole.F_ROLE_CONF)) {
                MenuItem templateMenu = new MenuItem("Настройки", "", null);
                adminMenuItem.getSubMenu().add(templateMenu);
                templateMenu.getSubMenu().add(new MenuItem("Макеты налоговых форм", NUMBER_SIGN
                        + DeclarationTemplateTokens.declarationTemplateList));
                templateMenu.getSubMenu().add(new MenuItem("Справочники", NUMBER_SIGN
                        + RefBookListTokens.REFBOOK_LIST_ADMIN));
                templateMenu.getSubMenu().add(new MenuItem("Сбросить кэш", CLEAR_CACHE_LINK, "", "_blank"));
                templateMenu.getSubMenu().add(new MenuItem("Экспорт макетов", DOWNLOAD_ALL_TEMPLATES));
                templateMenu.getSubMenu().add(new MenuItem("Импорт скриптов", NUMBER_SIGN
                        + ScriptsImportTokens.SCRIPTS_IMPORT));
            }
            if (!adminMenuItem.getSubMenu().isEmpty()) {
                menuItems.add(adminMenuItem);
            }
        }

		GetMainMenuResult result = new GetMainMenuResult();
		result.setMenuItems(menuItems);

        result.setRolesHashCode(currentUser.getRoles().hashCode());

		return result;
	}

	@Override
	public void undo(GetMainMenuAction action, GetMainMenuResult result,
			ExecutionContext context) throws ActionException {
	}
}
