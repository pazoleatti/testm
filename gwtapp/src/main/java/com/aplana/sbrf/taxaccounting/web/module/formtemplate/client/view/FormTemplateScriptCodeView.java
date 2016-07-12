package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateScriptCodePresenter;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.client.CodeMirror;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class FormTemplateScriptCodeView extends ViewWithUiHandlers<FormTemplateScriptCodeUiHandlers> implements FormTemplateScriptCodePresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateScriptCodeView> { }

	@UiField
	CodeMirror script;

	@Inject
	public FormTemplateScriptCodeView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setScriptCode(String text) {
		script.setText(text);
	}

	@UiHandler("script")
	void onDecNameChanged(ChangeEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onDataViewChanged();
		}
	}

	@Override
	public String getScriptCode() {
		return script.getText();
	}
}