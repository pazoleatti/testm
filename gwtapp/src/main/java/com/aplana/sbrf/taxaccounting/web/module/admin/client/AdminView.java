package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
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
	ScriptEditor createScriptEditor;

	@UiField
	ScriptEditor calcScriptEditor;

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
	public ScriptEditor getCreateScriptEditor() {
		return createScriptEditor;
	}

	@Override
	public ScriptEditor getCalcScriptEditor() {
		return calcScriptEditor;
	}
}
