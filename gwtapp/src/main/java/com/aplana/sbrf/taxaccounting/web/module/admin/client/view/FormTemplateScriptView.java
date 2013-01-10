package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.ScriptEditor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;


public class FormTemplateScriptView extends ViewWithUiHandlers<FormTemplateScriptUiHandlers> implements FormTemplateScriptPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateScriptView> { }

	private final Widget widget;
	private List<Script> scriptList;

	// Элементы управления редактирования скриптов
	@UiField
	ScriptEditor scriptEditor;

	@UiField
	ListBox scriptListBox;

	@Inject
	public FormTemplateScriptView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("scriptListBox")
	public void onChange(ChangeEvent event){
		selectScript();
	}

	@UiHandler("createScriptButton")
	public void onCreate(ClickEvent event){
		getUiHandlers().createScript();
	}

	@UiHandler("deleteScriptButton")
	public void onDelete(ClickEvent event){
		getUiHandlers().deleteScript(getSelectedScript(scriptList));
	}

	@Override
	public void bindScripts(List<Script> scriptList) {
		this.scriptList = scriptList;
		int i = 0;
		scriptListBox.clear();
		for (Script script : scriptList) {
			scriptListBox.addItem(script.getName(), String.valueOf(i++));
		}
		scriptListBox.setSelectedIndex(0);
		selectScript();
	}

	@Override
	public void flush() {
		scriptEditor.flush();
	}

	private void selectScript() {
		scriptEditor.flush();
		scriptEditor.setValue(getSelectedScript(scriptList));
	}

	private Script getSelectedScript(List<Script> scriptList) {
		Script script = null;
		int selInd = scriptListBox.getSelectedIndex();
		if (selInd >= 0) {
			String str = scriptListBox.getValue(selInd);
			int scrInd = Integer.valueOf(str);
			script = scriptList.get(scrInd);
		}
		return script;
	}
}