package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.CodeMirror;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DeclarationTemplateView extends ViewWithUiHandlers<DeclarationTemplateUiHandlers>
		implements DeclarationTemplatePresenter.MyView, Editor<DeclarationTemplate> {

	interface Binder extends UiBinder<Widget, DeclarationTemplateView> { }

	interface MyDriver extends SimpleBeanEditorDriver<DeclarationTemplate, DeclarationTemplateView> {
	}

	private final Widget widget;
	private final MyDriver driver = GWT.create(MyDriver.class);

	@UiField
	@Editor.Ignore
	FormPanel form;

	@UiField
	@Editor.Ignore
	Label titleLabel;

	@UiField
	@Editor.Ignore
	Button saveButton;

	@UiField
	@Editor.Ignore
	Button resetButton;

	@UiField
	@Editor.Ignore
	Button cancelButton;

	@UiField(provided = true)
	ValueListBox<TaxType> taxType;

	@UiField
	TextBox version;

	@UiField
	CheckBox active;

	@UiField
	CodeMirror createScript;

	@Inject
	@UiConstructor
	public DeclarationTemplateView(final Binder uiBinder) {
		List<TaxType> taxTypes = new ArrayList<TaxType>();
		Collections.addAll(taxTypes, TaxType.values());
		taxType = new ValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
			@Override
			public String render(TaxType type) {
				if (type == null) {
					return "";
				}
				return type.getName();
			}
		});
		taxType.setAcceptableValues(taxTypes);

		widget = uiBinder.createAndBindUi(this);
		driver.initialize(this);
		addFileUploader();
	}

	@Override
	public void setDeclarationTemplate(final DeclarationTemplate declaration) {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				titleLabel.setText(declaration.getId().toString());
				driver.edit(declaration);
				form.setAction(GWT.getHostPageBaseURL() + "download/uploadJrxml/" + declaration.getId());
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		driver.flush();
		form.submit();
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
		getUiHandlers().close();
	}

	private void addFileUploader() {
		// Because we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		VerticalPanel panel = new VerticalPanel();
		form.setWidget(panel);

		// Create a FileUpload widget.
		FileUpload upload = new FileUpload();
		upload.setName("uploadJrxmlFile");
		panel.add(upload);

		// Add a 'submit' button.
		// Add an event handler to the form.
		form.addSubmitHandler(new FormPanel.SubmitHandler() {
			@Override
			public void onSubmit(FormPanel.SubmitEvent event) {
				// This event is fired just before the form is submitted. We can take
				// this opportunity to perform validation.
				Window.alert("onSubmit");
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this event is
				// fired. Assuming the service returned a response of type text/html,
				// we can get the result text here (see the FormPanel documentation for
				// further explanation).
				Window.alert(event.getResults());
			}
		});
	}

}