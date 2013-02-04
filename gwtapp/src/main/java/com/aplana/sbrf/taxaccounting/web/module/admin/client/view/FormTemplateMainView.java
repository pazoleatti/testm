package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;


import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateMainPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.BaseTab;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.SimpleTabPanel;
import com.aplana.sbrf.taxaccounting.web.widget.cell.LogEntryCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;


public class FormTemplateMainView extends ViewWithUiHandlers<FormTemplateMainUiHandlers> implements FormTemplateMainPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateMainView> { }

	private final Widget widget;
	private int formId;

	@UiField
	DockLayoutPanel dockPanel;

	@UiField
	Widget logPanel;

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

	@UiField(provided = true)
	CellList<LogEntry> loggerList = new CellList<LogEntry>(new LogEntryCell());

	@Inject
	public FormTemplateMainView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Tab addTab(TabData tabData, String historyToken) {
		if(formId !=0) {
			return tabPanel.addTab(tabData, historyToken + ";" + AdminConstants.NameTokens.formTemplateId + "=" + formId);
		}
		return tabPanel.addTab(tabData, historyToken + ";" + AdminConstants.NameTokens.formTemplateId + "=");
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
		tabPanel.changeTab(tab, tabData, historyToken + ";" + AdminConstants.NameTokens.formTemplateId + "=" + formId);
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

	@Override
	public void setLogMessages(List<LogEntry> entries) {
		dockPanel.setWidgetHidden(logPanel, (entries == null || entries.isEmpty()));
		if (entries != null && !entries.isEmpty()) {
			logPanel.setVisible(true);
			loggerList.setVisible(true);
			loggerList.setRowData(entries);
		}
		else {
			loggerList.setRowCount(0);
			loggerList.setRowData(new ArrayList<LogEntry>(0));
		}
		loggerList.redraw();
	}
}