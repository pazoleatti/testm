package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
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
		//menuItems.add(new MenuItem("Домашняя", HomeNameTokens.homePage));
		menuItems.add(new MenuItem("Транспортный налог",FormDataListNameTokens.FORM_DATA_LIST + ";nType=" + TaxType.TRANSPORT));
		menuItems.add(new MenuItem("Налог на прибыль", FormDataListNameTokens.FORM_DATA_LIST + ";nType=" + TaxType.INCOME));
		menuItems.add(new MenuItem("Налог на имущество", FormDataListNameTokens.FORM_DATA_LIST + ";nType=" + TaxType.PROPERTY));
		menuItems.add(new MenuItem("НДС", FormDataListNameTokens.FORM_DATA_LIST + ";nType=" + TaxType.VAT));
		if (securityService.currentUser().hasRole("ROLE_CONF")) {
	        menuItems.add(new MenuItem("Шаблоны налоговых форм", AdminConstants.NameTokens.adminPage));
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
