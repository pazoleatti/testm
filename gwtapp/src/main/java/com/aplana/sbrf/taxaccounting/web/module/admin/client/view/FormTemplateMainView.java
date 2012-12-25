package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;


import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateMainPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.BaseTab;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.SimpleTabPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;


public class FormTemplateMainView extends ViewWithUiHandlers<FormTemplateMainUiHandlers> implements FormTemplateMainPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateMainView> { }

	private final Widget widget;
	private int formId;

	@UiField
	SimpleTabPanel tabPanel;

	@UiField
	Label titleLabel;

	@UiField
	Button saveButton;

	@UiField
	Button resetButton;

	@UiField
	Button cancelButton;

	@Inject
	public FormTemplateMainView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Tab addTab(TabData tabData, String historyToken) {
		if(formId !=0) {
			return tabPanel.addTab(tabData, historyToken + ";" + AdminNameTokens.formTemplateId + "=" + formId);
		}
		return tabPanel.addTab(tabData, historyToken + ";" + AdminNameTokens.formTemplateId + "=");
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void removeTab(Tab tab) {
		tabPanel.removeTab(tab);
	}

	@Override
	public void removeTabs() {
		tabPanel.removeTabs();
	}

	@Override
	public void setActiveTab(Tab tab) {
		tabPanel.setActiveTab(tab);
	}

	@Override
	public void changeTab(Tab tab, TabData tabData, String historyToken) {
		tabPanel.changeTab(tab, tabData, historyToken + ";" + AdminNameTokens.formTemplateId + "=" + formId);
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == FormTemplateMainPresenter.TYPE_SetTabContent) {
			tabPanel.setPanelContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().save();
		}
	}

	@UiHandler("resetButton")
	public void onReset(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().reset();
		}
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().close();
	}

	@Override
	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	@Override
	public void setFormId(int formId) {
		List<BaseTab> tabList = tabPanel.getTabList();

		if (this.formId != 0) {
			for (BaseTab tab : tabList) {
				tab.setTargetHistoryToken(tab.getTargetHistoryToken()
						.substring(0, tab.getTargetHistoryToken().length() - String.valueOf(this.formId).length()));
			}
		}

		for (BaseTab tab : tabList) {
			tab.setTargetHistoryToken(tab.getTargetHistoryToken() + String.valueOf(formId));
		}

		this.formId = formId;
	}
}