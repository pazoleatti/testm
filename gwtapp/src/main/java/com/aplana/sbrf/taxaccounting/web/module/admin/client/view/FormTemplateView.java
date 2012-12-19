package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.ScriptEditor;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplatePresenter;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление для формы редактирования шаблона формы.
 *
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

	@UiField
	ListBox eventScriptListBox;

	@UiField
	ListBox freeScriptListBox;

	@UiField
	Label titleLabel;

	@UiField
	Button upEventScript;

	private final Widget widget;

	@Inject
	public FormTemplateView(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@UiHandler("upEventScript")
	public void onUpEventScript(ClickEvent event){
		getUiHandlers().upEventScript();
	}

	@UiHandler("downEventScript")
	public void onDownEventScript(ClickEvent event){
		getUiHandlers().downEventScript();
	}

	@UiHandler("addScriptToEvent")
	public void onAddScriptToEvent(ClickEvent event){
		getUiHandlers().addScriptToEvent();
	}

	@UiHandler("removeScriptFromEvent")
	public void onRemoveScriptFromEvent(ClickEvent event){
		getUiHandlers().removeScriptFromEvent();
	}

	@UiHandler("eventListBox")
	public void onSelectEvent(ChangeEvent event){
		getUiHandlers().selectEvent();
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
			getUiHandlers().load();
		}
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().close();
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

	@Override
	public ListBox getEventListBox() {
		return eventListBox;
	}

	@Override
	public ListBox getEventScriptListBox() {
		return eventScriptListBox;
	}

	@Override
	public ListBox getFreeScriptListBox() {
		return freeScriptListBox;
	}

	@Override
	public Label getTitleLabel() {
		return titleLabel;
	}
}
