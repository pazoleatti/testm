package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.gwt.client.mask.ui.YearMaskBox;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.client.CodeMirror;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
	FileUpload uploadJrxml;

    @UiField
    @Editor.Ignore
    FileUpload uploadDectFile;

	@UiField
	@Editor.Ignore
	FormPanel uploadJrxmlForm;
	
	@UiField
	@Editor.Ignore
	FormPanel uploadDectForm;

	@UiField
	@Editor.Ignore
	Button saveButton;

	@UiField
	@Editor.Ignore
	Button resetButton;

	@UiField
	@Editor.Ignore
	Button cancelButton;

	@UiField
	@Editor.Ignore
	Label title;

	@UiField
    @Path("declarationTemplate.createScript")
	CodeMirror createScript;

    @UiField
    @Editor.Ignore
    FileUploadWidget fileUploader;

    @UiField
    @Editor.Ignore
    Button activateVersion;

    @UiField
    @Editor.Ignore
    Label lockInformation;

    @UiField
    @Path("declarationTemplate.name")
    TextBox decName;

    @UiField
    @Editor.Ignore
    Anchor downloadDectButton;

    @UiField
    @Editor.Ignore
    Anchor downloadJrxmlButton;
    @UiField
    LinkAnchor returnAnchor;

    private static String respPattern = "(<pre.*>)(.+?)(</pre>)";

    @Inject
	@UiConstructor
	public DeclarationTemplateView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        FormElement.as(uploadJrxmlForm.getElement()).setAcceptCharset("UTF-8");
        FormElement.as(uploadDectForm.getElement()).setAcceptCharset("UTF-8");
		driver.initialize(this);

		uploadDectForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                if(event.getResults() == null){
                    getUiHandlers().uploadDectFail("Ошибки при импорте формы.");
                    return;
                }
                String resultString = event.getResults().toLowerCase();
                if (resultString.contains(ERROR_RESP)) {
                    String errorUuid = resultString.replaceAll(respPattern, "$2");
                    getUiHandlers().uploadDectResponseWithErrorUuid(errorUuid.replaceFirst(ERROR_RESP, ""));
                }else if (resultString.toLowerCase().contains(ERROR)) {
                    String errorText = resultString.replaceAll(respPattern, "$2");
                    getUiHandlers().uploadDectFail(errorText.replaceFirst(ERROR, ""));
                } else {
                    String uuid = resultString.replaceAll(respPattern, "$2");
                    getUiHandlers().uploadDectResponseWithUuid(uuid.replaceFirst(SUCCESS_RESP, ""));
                }
			}
		});

		uploadJrxmlForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
				if (!event.getResults().toLowerCase().contains(ERROR)) {
					getUiHandlers().save();
				}
				else {
                    uploadJrxmlForm.reset();
					getUiHandlers().uploadJrxmlFail(event.getResults().replaceFirst(ERROR, ""));
				}
			}
		});
	}

	@Override
	public void setDeclarationTemplate(final DeclarationTemplateExt declarationTemplateExt) {
        uploadDectForm.reset();
        uploadJrxmlForm.reset();
        Integer id = declarationTemplateExt.getDeclarationTemplate().getId();
		uploadDectForm.setAction(GWT.getHostPageBaseURL() + "download/declarationTemplate/uploadDect/" + (id != null?id:0));
		uploadJrxmlForm.setAction(GWT.getHostPageBaseURL() + "download/uploadJrxml/" + (id != null?id:0));
        title.setText(declarationTemplateExt.getDeclarationTemplate().getType().getName());
        driver.edit(declarationTemplateExt);

        downloadDectButton.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        uploadJrxml.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        uploadDectFile.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        fileUploader.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        downloadJrxmlButton.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
	}

    @Override
    public void addDeclarationValueHandler(ValueChangeHandler<String> valueChangeHandler) {
        fileUploader.addValueChangeHandler(valueChangeHandler);
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
                    + "\" (с "+ lockDate + " )";
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
        Style formDataTableStyle = createScript.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        formDataTableStyle.setProperty("top", DEFAULT_TABLE_TOP_POSITION + downShift, Style.Unit.PX);
    }

    @UiHandler("saveButton")
	public void onSave(ClickEvent event){
		driver.flush();
		if (uploadJrxml.getFilename().isEmpty()) {
			getUiHandlers().save();
		}
		else {
			uploadJrxmlForm.submit();
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
                        getUiHandlers().close();
                    }
                });
	}

	@UiHandler("downloadJrxmlButton")
	public void onDownloadJrxmlButton(ClickEvent event){
		getUiHandlers().downloadJrxml();
	}
	
	@UiHandler("downloadDectButton")
	public void onDownloadDectButton(ClickEvent event){
		getUiHandlers().downloadDect();
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

}