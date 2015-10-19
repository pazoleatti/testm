package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.gwt.client.mask.ui.YearMaskBox;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.client.CodeMirror;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
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
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class DeclarationTemplateView extends ViewWithUiHandlers<DeclarationTemplateUiHandlers>
		implements DeclarationTemplatePresenter.MyView, Editor<DeclarationTemplateExt> {

    interface Binder extends UiBinder<Widget, DeclarationTemplateView> { }

	interface MyDriver extends SimpleBeanEditorDriver<DeclarationTemplateExt, DeclarationTemplateView> {
	}

    interface UrlTemplates extends SafeHtmlTemplates {
        @Template("download/downloadByUuid/{0}")
        SafeHtml getDownloadUrl(String uuid);

        @Template("download/declarationTemplate/downloadDect/{0}")
        SafeHtml getDownloadDTUrl(int dtId);

        @Template("download/uploadJrxml/{0}")
        SafeHtml getUploadJrxmlUrl(int dtId);

        @Template("download/uploadXsd/{0}")
        SafeHtml getUploadXsdlUrl(int dtId);

        @Template("download/declarationTemplate/uploadDect/{0}")
        SafeHtml getUploadDTUrl(int dtId);
    }

    private static final UrlTemplates urlTemplates = GWT.create(UrlTemplates.class);
	private final MyDriver driver = GWT.create(MyDriver.class);

    private final static int DEFAULT_TABLE_TOP_POSITION = 140;
    private final static int LOCK_INFO_BLOCK_HEIGHT = 25;

    @UiField
    @Path("declarationTemplate.version")
    YearMaskBox versionDateBegin;

    @UiField
    @Path("endDate")
    YearMaskBox versionDateEnd;

	@UiField
	@Editor.Ignore
	FileUploadWidget uploadJrxmlFile, uploadDectFile, uploadXsdFile;

	@UiField
	@Editor.Ignore
	Button saveButton, resetButton, cancelButton, activateVersion;

	@UiField
	@Editor.Ignore
	Label title;

	@UiField
    @Path("declarationTemplate.createScript")
	CodeMirror createScript;

    @UiField
    @Editor.Ignore
    Label lockInformation;

    @UiField
    @Path("declarationTemplate.name")
    TextBox decName;

    @UiField
    @Editor.Ignore
    Anchor downloadDectButton, downloadJrxmlButton, downloadXsd;

    @UiField
    LinkAnchor returnAnchor;
    @UiField
    LinkButton deleteXsd, deleteJrxml;

    @Inject
	@UiConstructor
	public DeclarationTemplateView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        FormElement.as(uploadJrxmlFile.getElement()).setAcceptCharset("UTF-8");
        FormElement.as(uploadDectFile.getElement()).setAcceptCharset("UTF-8");
		driver.initialize(this);
	}

	@Override
	public void setDeclarationTemplate(final DeclarationTemplateExt declarationTemplateExt) {
        /*uploadDectForm.reset();*/
        /*uploadJrxmlForm.reset();*/
        Integer id = declarationTemplateExt.getDeclarationTemplate().getId() != null ?
                declarationTemplateExt.getDeclarationTemplate().getId() : 0;
        DeclarationTemplate template = declarationTemplateExt.getDeclarationTemplate();
        uploadDectFile.setActionUrl(urlTemplates.getUploadDTUrl(id).asString());
        uploadJrxmlFile.setActionUrl(urlTemplates.getUploadJrxmlUrl(id).asString());
        uploadXsdFile.setActionUrl(urlTemplates.getUploadXsdlUrl(id).asString());
        title.setText(template.getType().getName());
        driver.edit(declarationTemplateExt);
        setEnabled(template.getId() != null);
        downloadJrxmlButton.setEnabled(template.getJrxmlBlobId() != null);
        deleteJrxml.setEnabled(template.getJrxmlBlobId() != null);
        downloadXsd.setEnabled(template.getXsdId() != null);
        deleteXsd.setEnabled(template.getXsdId() != null);
	}

    @Override
    public HandlerRegistration addValueChangeHandlerJrxml(ValueChangeHandler<String> valueChangeHandler) {
        return uploadJrxmlFile.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public HandlerRegistration addValueChangeHandlerXsd(ValueChangeHandler<String> valueChangeHandler) {
        return uploadXsdFile.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public HandlerRegistration addChangeHandlerDect(ValueChangeHandler<String> valueChangeHandler) {
        return uploadDectFile.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public HandlerRegistration addEndLoadHandlerXsd(EndLoadFileEvent.EndLoadFileHandler handler) {
        return uploadXsdFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addEndLoadHandlerJrxml(EndLoadFileEvent.EndLoadFileHandler handler) {
        return uploadJrxmlFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addEndLoadHandlerDect(EndLoadFileEvent.EndLoadFileHandler handler) {
        return uploadDectFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addJrxmlLoadHandlerDect(JrxmlFileExistEvent.JrxmlFileExistHandler handler) {
        return uploadDectFile.addJrxmlLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addJrxmlLoadHandler(JrxmlFileExistEvent.JrxmlFileExistHandler handler) {
        return uploadJrxmlFile.addJrxmlLoadHandler(handler);
    }

    private void setEnabled(boolean isEnable){
        uploadJrxmlFile.setEnabled(isEnable);
        uploadDectFile.setEnabled(isEnable);
        uploadXsdFile.setEnabled(isEnable);
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

    @Override
    public void clearCode() {
        createScript.setText("");
    }

    /**
     * Увеличивает верхний отступ у панели, когда показывается сообщение о блокировки
     * @param isLockInfoVisible показано ли сообщение
     */
    private void changeTableTopPosition(Boolean isLockInfoVisible) {
        Style formDataTableStyle = createScript.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        formDataTableStyle.setProperty("top", DEFAULT_TABLE_TOP_POSITION + downShift, Style.Unit.PX);
    }

    @UiHandler("downloadJrxmlButton")
    void onDownloadJrxmlButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadJrxml();
        }
    }

    @UiHandler("downloadXsd")
    void onDownloadXsdClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadXsd();
        }
    }

    @UiHandler("downloadDectButton")
    void onDownloadDectButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadDect();
        }
    }


    @UiHandler("saveButton")
	public void onSave(ClickEvent event){
		driver.flush();
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
        Dialog.confirmMessage(getUiHandlers().getDeclarationId() != 0 ? "Редактирование версии макета" : "Создание версии макета", "Сохранить изменения?",
                new DialogHandler() {
                    @Override
                    public void no() {
                        getUiHandlers().close();
                    }

                    @Override
                    public void yes() {
                        driver.flush();
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

    @UiHandler("deleteXsd")
    void onDeleteXsd(ClickEvent event){
        Dialog.confirmMessage("Удаление xsd файла", "Вы действительно хотите удалить xsd-файл?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().onDeleteXsd();
            }
        });
    }

    @UiHandler("deleteJrxml")
    void onDeleteJrxml(ClickEvent event){
        Dialog.confirmMessage("Удаление jrxml файла", "Вы действительно хотите удалить jrxml файл?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().onCheckBeforeDeleteJrxml();
            }
        });
    }

}