package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.BaseTab;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.SimpleTabPanel;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class DeclarationTemplateMainView extends ViewWithUiHandlers<DeclarationTemplateMainUiHandlers>
		implements DeclarationTemplateMainPresenter.MyView {

    interface Binder extends UiBinder<Widget, DeclarationTemplateMainView> { }

    interface UrlTemplates extends SafeHtmlTemplates {
        @Template("download/declarationTemplate/uploadDect/{0}")
        SafeHtml getUploadDTUrl(int dtId);
    }

    private static final UrlTemplates urlTemplates = GWT.create(UrlTemplates.class);

    private final static int DEFAULT_TABLE_TOP_POSITION = 0;
    private final static int LOCK_INFO_BLOCK_HEIGHT = 25;

	@UiField
	FileUploadWidget uploadDectFile;

	@UiField
	@Editor.Ignore
	Button saveButton, resetButton, cancelButton, activateVersion;

	@UiField
	@Editor.Ignore
	Label title;

    @UiField
    @Editor.Ignore
    Label lockInformation;

    @UiField
    Anchor downloadDectButton;

    @UiField
    LinkAnchor returnAnchor;

    @UiField
    @Editor.Ignore
    SimpleTabPanel tabPanel;

    @Inject
	@UiConstructor
	public DeclarationTemplateMainView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        FormElement.as(uploadDectFile.getElement()).setAcceptCharset("UTF-8");
	}

    private int templateId;

	@Override
	public void setDeclarationTemplate(final DeclarationTemplateExt declarationTemplateExt) {
        Integer id = declarationTemplateExt.getDeclarationTemplate().getId() != null ?
                declarationTemplateExt.getDeclarationTemplate().getId() : 0;
        DeclarationTemplate template = declarationTemplateExt.getDeclarationTemplate();
        uploadDectFile.setActionUrl(urlTemplates.getUploadDTUrl(id).asString());
        title.setText(template.getType().getName());
        setEnabled(template.getId() != null);
        setTemplateId(templateId);
	}

    @Override
    public HandlerRegistration addChangeHandlerDect(ValueChangeHandler<String> valueChangeHandler) {
        return uploadDectFile.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public HandlerRegistration addStartLoadHandlerDect(StartLoadFileEvent.StartLoadFileHandler handler) {
        return uploadDectFile.addStartLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addEndLoadHandlerDect(EndLoadFileEvent.EndLoadFileHandler handler) {
        return uploadDectFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addJrxmlLoadHandlerDect(JrxmlFileExistEvent.JrxmlFileExistHandler handler) {
        return uploadDectFile.addJrxmlLoadHandler(handler);
    }

    private void setEnabled(boolean isEnable){
        uploadDectFile.setEnabled(isEnable);
        downloadDectButton.setEnabled(isEnable);
    }

    @Override
    public void activateButtonName(String name) {
        activateVersion.setText(name);
    }

    @Override
    public void activateButton(boolean isVisible) {
        activateVersion.setVisible(isVisible);
    }

    @Override
    public void setLockInformation(boolean isVisible, String lockDate, String lockedBy) {
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
    private void changeTableTopPosition(Boolean isLockInfoVisible) {
        Style tabPanelStyle = tabPanel.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        tabPanelStyle.setProperty("top", DEFAULT_TABLE_TOP_POSITION + downShift, Style.Unit.PX);
    }

    @UiHandler("downloadDectButton")
    void onDownloadDectButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadDect();
        }
    }


    @UiHandler("saveButton")
	public void onSave(ClickEvent event){
        getUiHandlers().setOnLeaveConfirmation(null);
        getUiHandlers().save();
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
        Dialog.confirmMessage(getUiHandlers().getDeclarationId() != 0 ? "Редактирование версии макета" : "Создание версии макета", "Сохранить изменения?",
                new DialogHandler() {
                    @Override
                    public void no() {
                        getUiHandlers().close();
                    }

                    @Override
                    public void yes() {
                        //driver.flush();
                        getUiHandlers().save();
                    }
                });
	}

    @UiHandler("activateVersion")
    public void onActivatetButton(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().activate(false);
    }

    @UiHandler("returnAnchor")
    void onReturnAnchor(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().close();
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


    @Override
    public void setTemplateId(int templateId) {
        List<BaseTab> tabList = tabPanel.getTabList();

        if (this.templateId != 0) {
            for (BaseTab tab : tabList) {
                tab.setTargetHistoryToken(tab.getTargetHistoryToken()
                        .substring(0, tab.getTargetHistoryToken().length() - String.valueOf(this.templateId).length()));
            }
        }

        for (BaseTab tab : tabList) {
            tab.setTargetHistoryToken(tab.getTargetHistoryToken() + templateId);
        }

        this.templateId = templateId;
    }

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        if (templateId != 0) {
            return tabPanel.addTab(tabData, historyToken + ";" + DeclarationTemplateTokens.declarationTemplateId + "=" + templateId);
        }
        return tabPanel.addTab(tabData, historyToken + ";" + DeclarationTemplateTokens.declarationTemplateId + "=");
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
        tabPanel.changeTab(tab, tabData, historyToken + ";" + DeclarationTemplateTokens.declarationTemplateId + "=" + templateId);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == DeclarationTemplateMainPresenter.TYPE_SetTabContent) {
            tabPanel.setPanelContent(content);
        } else {
            super.setInSlot(slot, content);
        }
    }
}