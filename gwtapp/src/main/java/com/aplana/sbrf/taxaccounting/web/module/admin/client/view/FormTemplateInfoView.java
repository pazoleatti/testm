package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateInfoPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class FormTemplateInfoView extends ViewWithUiHandlers<FormTemplateInfoUiHandlers> implements FormTemplateInfoPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateInfoView> { }

	private final Widget widget;

	@UiField
	TextBox versionBox;

	@UiField
	CheckBox numberedColumnsBox;

	@Inject
	public FormTemplateInfoView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setViewData(String version, boolean numberedColumns) {
		versionBox.setValue(version);
		numberedColumnsBox.setValue(numberedColumns);
	}

	@Override
	public void onFlush() {
		getUiHandlers().setVersion(versionBox.getValue());
		getUiHandlers().setNumberedColumns(numberedColumnsBox.getValue());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}