package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateStylePresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FormTemplateStyleView extends ViewWithUiHandlers<FormTemplateStyleUiHandlers> implements FormTemplateStylePresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateStyleView> { }

	private final Widget widget;

	@Inject
	public FormTemplateStyleView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}