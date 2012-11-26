package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * @author Vitalii Samolovskikh
 */
public class FormTemplateView extends ViewWithUiHandlers<FormTemplateUiHandlers> implements FormTemplatePresenter.MyView {
	interface Binder extends UiBinder<Widget, FormTemplateView> {
	}

	// Элементы управления редактирования скриптов
	@UiField
	ScriptEditor scriptEditor;

	@UiField
	ListBox scriptListBox;

	// элементы управления назначения скриптов на задания
	@UiField
	ListBox eventListBox;

	private final Widget widget;

	@Inject
	public FormTemplateView(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().save();
		}
	}

	@UiHandler("scriptListBox")
	public void onChange(ChangeEvent event){
		getUiHandlers().selectScript();
	}

	@UiHandler("createScriptButton")
	public void onCreate(ClickEvent event){
		getUiHandlers().createScript();
	}

	@UiHandler("deleteScriptButton")
	public void onDelete(ClickEvent event){
		getUiHandlers().deleteScript();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().load();
		}
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public ScriptEditor getScriptEditor() {
		return scriptEditor;
	}

	@Override
	public ListBox getScriptListBox() {
		return scriptListBox;
	}
}
