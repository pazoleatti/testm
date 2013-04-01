package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;


public class FormTemplateInfoView extends ViewWithUiHandlers<FormTemplateInfoUiHandlers> implements FormTemplateInfoPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateInfoView> { }

	private final Widget widget;

	@UiField
	TextBox versionBox;

	@UiField
	CheckBox numberedColumnsBox;

	@UiField
	CheckBox fixedRowsCheckBox;
	
	@UiField
	TextBox nameBox;
	
	@UiField
	TextBox fullnameBox;
	
	@UiField
	TextBox codeBox;

	@Inject
	public FormTemplateInfoView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setViewData(String version, boolean numberedColumns, boolean fixedRows, String name, String fullName, String code) {
		versionBox.setValue(version);
		numberedColumnsBox.setValue(numberedColumns);
		fixedRowsCheckBox.setValue(fixedRows);
		nameBox.setValue(name);
		fullnameBox.setValue(fullName);
		codeBox.setValue(code);
	}

	@Override
	public void onFlush() {
		getUiHandlers().setVersion(versionBox.getValue());
		getUiHandlers().setNumberedColumns(numberedColumnsBox.getValue());
		getUiHandlers().setFixedRows(fixedRowsCheckBox.getValue());
		getUiHandlers().setName(nameBox.getValue());
		getUiHandlers().setFullname(fullnameBox.getValue());
		getUiHandlers().setCode(codeBox.getValue());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}