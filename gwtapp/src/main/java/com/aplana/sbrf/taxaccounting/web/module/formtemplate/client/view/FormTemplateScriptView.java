package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.ScriptEditor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;


public class FormTemplateScriptView extends ViewWithUiHandlers<FormTemplateScriptUiHandlers> implements FormTemplateScriptPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateScriptView> { }

	private List<Script> scriptList;
	private int lastSelectedScriptIndex = 0;

	// Элементы управления редактирования скриптов
	@UiField
	ScriptEditor scriptEditor;

	@UiField
	ListBox scriptListBox;

	@Inject
	public FormTemplateScriptView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
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
	public void bindScripts(List<Script> scriptList, boolean isFormChanged) {
		this.scriptList = scriptList;
		int i = 0;
		scriptListBox.clear();
		for (Script script : scriptList) {
			scriptListBox.addItem(script.getName(), String.valueOf(i++));
		}

		if (isFormChanged) {
			lastSelectedScriptIndex = 0;
		}

		scriptListBox.setSelectedIndex(lastSelectedScriptIndex);
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
		lastSelectedScriptIndex = scriptListBox.getSelectedIndex();
		if (lastSelectedScriptIndex >= 0) {
			String str = scriptListBox.getValue(lastSelectedScriptIndex);
			int scrInd = Integer.valueOf(str);
			script = scriptList.get(scrInd);
		}
		return script;
	}
}