package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateScriptCodePresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class FormTemplateScriptCodeView extends ViewWithUiHandlers<FormTemplateScriptCodeUiHandlers> implements FormTemplateScriptCodePresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateScriptCodeView> { }

	private final Widget widget;

	@UiField
	HasText script;

	@Inject
	public FormTemplateScriptCodeView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setScriptCode(String text) {
		script.setText(text);
	}

	@Override
	public String getScriptCode() {
		return script.getText();
	}
}