package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class AdminView extends ViewImpl implements AdminPresenter.MyView {
    interface Binder extends UiBinder<Widget, AdminView> {
    }

    private final Widget widget;

    @UiField
    ListBox formListBox;

    @UiField
    Button saveButton;

    @UiField
    Button cancelButton;

	@UiField
	ScriptEditor scriptEditor;

	@UiField
	ListBox scriptListBox;

	@Inject
    public AdminView(final Binder binder) {
        widget = binder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public ListBox getFormListBox() {
        return formListBox;
    }

    @Override
    public Button getSaveButton() {
        return saveButton;
    }

    @Override
    public Button getCancelButton() {
        return cancelButton;
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
