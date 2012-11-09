package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeNameTokens;
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

	@Override
	public GetMainMenuResult execute(GetMainMenuAction action,
			ExecutionContext context) throws ActionException {

		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(new MenuItem("Домашняя", HomeNameTokens.homePage));
		menuItems.add(new MenuItem("Список форм",
				FormDataListNameTokens.formDataListPage));
        menuItems.add(new MenuItem("Администрирование", AdminNameTokens.adminPage));
		menuItems.add(new MenuItem("О системе", AboutNameTokens.aboutPage));
		menuItems.add(new MenuItem("Контакты", AboutNameTokens.contactPage));

		GetMainMenuResult result = new GetMainMenuResult();
		result.setMenuItems(menuItems);

		return result;

	}

	@Override
	public void undo(GetMainMenuAction action, GetMainMenuResult result,
			ExecutionContext context) throws ActionException {
	}

}
