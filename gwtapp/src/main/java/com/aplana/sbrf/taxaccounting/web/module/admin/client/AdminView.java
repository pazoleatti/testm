package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.event.dom.client.ChangeEvent;
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
public class AdminView extends ViewWithUiHandlers<AdminUiHandlers> implements AdminPresenter.MyView {
	interface Binder extends UiBinder<Widget, AdminView> {
	}

	private final Widget widget;

	@UiField
	ListBox formListBox;

	@Inject
	public AdminView(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@UiHandler("formListBox")
	public void onFormListBoxChange(ChangeEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().select();
		}
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public ListBox getListBox() {
		return formListBox;
	}
}
