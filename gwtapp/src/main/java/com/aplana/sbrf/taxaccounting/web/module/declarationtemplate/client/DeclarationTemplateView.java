package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.CodeMirror;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
	}

	@Override
	public void setDeclarationTemplate(final DeclarationTemplate declaration) {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				titleLabel.setText(declaration.getId().toString());
				driver.edit(declaration);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers()!=null){
			driver.flush();
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
}