package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.mask.DateMaskBoxAbstract;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.client.CodeMirror;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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

    @UiField
    @Path("declarationTemplate.version")
    DateMaskBoxAbstract versionDateBegin;

    @UiField
    @Path("endDate")
    DateMaskBoxAbstract versionDateEnd;
	
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
    @Path("declarationTemplate.active")
	CheckBox active;

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
    @Path("declarationTemplate.name")
    TextBox decName;

    @UiField
    @Editor.Ignore
    Anchor downloadDectButton;

	@Inject
	@UiConstructor
	public DeclarationTemplateView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		
		driver.initialize(this);
		
		uploadDectForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
				if (!event.getResults().toLowerCase().contains("error")) {
					getUiHandlers().uploadDectSuccess();
				} else {
					getUiHandlers().uploadDectFail(event.getResults().replaceFirst("error ", ""));
				}
			}
		});

		uploadJrxmlForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
				if (!event.getResults().toLowerCase().contains("error")) {
					getUiHandlers().save();
				}
				else {
					getUiHandlers().uploadJrxmlFail(event.getResults().replaceFirst("error ", ""));
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
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				title.setText(declarationTemplateExt.getDeclarationTemplate().getType().getName());
				driver.edit(declarationTemplateExt);
			}
		});

        downloadDectButton.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        uploadJrxml.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        uploadDectFile.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
        fileUploader.setEnabled(declarationTemplateExt.getDeclarationTemplate().getId() != null);
	}

    @Override
    public void addDeclarationValueHandler(ValueChangeHandler<String> valueChangeHandler) {
        fileUploader.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public void activateButtonName(String name) {
        activateVersion.setText(name);
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
		getUiHandlers().close();
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
            getUiHandlers().activate();
    }

}