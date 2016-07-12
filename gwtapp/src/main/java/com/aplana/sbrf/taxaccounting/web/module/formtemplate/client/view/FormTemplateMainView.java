package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;


import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateMainPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.BaseTab;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.SimpleTabPanel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;


public class FormTemplateMainView extends ViewWithUiHandlers<FormTemplateMainUiHandlers> implements FormTemplateMainPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateMainView> { }

    private final static int LOCK_INFO_BLOCK_HEIGHT = 25;

    private int formId;

	@UiField
	Label title;

	@UiField
	SimpleTabPanel tabPanel;

	@UiField
	Button saveButton;

	@UiField
	Button resetButton;

	@UiField
	Button cancelButton;

    @UiField
    Button activateVersion;

    @UiField
    Anchor returnAnchor;

    @UiField
    Label lockInformation;

	@Inject
	public FormTemplateMainView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public Tab addTab(TabData tabData, String historyToken) {
		if(formId !=0) {
			return tabPanel.addTab(tabData, historyToken + ";" + AdminConstants.NameTokens.formTemplateId + "=" + formId);
		}
		return tabPanel.addTab(tabData, historyToken + ";" + AdminConstants.NameTokens.formTemplateId + "=");
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
	public void setInSlot(Object slot, IsWidget content) {
		if (slot == FormTemplateMainPresenter.TYPE_SetTabContent) {
			tabPanel.setPanelContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().setOnLeaveConfirmation(null);
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
		if (getUiHandlers() == null)
			return;

		getUiHandlers().setOnLeaveConfirmation(null);
        Dialog.confirmMessage(formId != 0 ?"Редактирование версии макета" : "Создание версии макета", "Сохранить изменения?",
                new DialogHandler() {
                    @Override
                    public void no() {
                        getUiHandlers().close();
                    }

                    @Override
                    public void yes() {
                        getUiHandlers().save();
                    }
                });
    }

    @UiHandler("activateVersion")
    void onActiveClick(ClickEvent event){
        if(getUiHandlers()!=null){
            getUiHandlers().activate(false);
        }
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
			tab.setTargetHistoryToken(tab.getTargetHistoryToken() + formId);
		}

		this.formId = formId;
	}

	@Override
	public void setTitle(String title) {
		this.title.setText(title);
        this.title.setTitle(title);
	}

    @Override
    public void activateVersionName(String s) {
        activateVersion.setText(s);
    }

    @Override
    public void activateVersion(boolean isVisible) {
        activateVersion.setVisible(isVisible);
    }

    @Override
    public void setLockInformation(boolean isVisible, String lockDate, String lockedBy){
        lockInformation.setVisible(isVisible);
        if(lockedBy != null && lockDate != null){
            String text = "Выбранный макет в текущий момент редактируется другим пользователем \"" + lockedBy
                    + "\" ("+ lockDate + ")";
            lockInformation.setText(text);
            lockInformation.setTitle(text);
        }
        changeTableTopPosition(isVisible);
    }

    /**
     * Увеличивает верхний отступ у панели, когда показывается сообщение о блокировки
     * @param isLockInfoVisible показано ли сообщение
     */
    private void changeTableTopPosition(Boolean isLockInfoVisible){
        Style formDataTableStyle = tabPanel.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        formDataTableStyle.setProperty("top", downShift, Style.Unit.PX);
    }

    @UiHandler("returnAnchor")
    void onReturnAnchor(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onReturnClicked();
            event.preventDefault();
            event.stopPropagation();
        }

    }

    @UiHandler("historyVersion")
    void onHistoryClick(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onHistoryClicked();
        }
    }
}