package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.Form;
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
    TextArea createScriptBody;

	@UiField
	TextBox createScriptName;

	@UiField
	TextBox createScriptCondition;

	@UiField
	CheckBox createScriptPerRow;

    @UiField
    Button saveButton;

    @UiField
    Button cancelButton;

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
    public TextArea getCreateScriptBody() {
        return createScriptBody;
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
	public TextBox getCreateScriptName() {
		return createScriptName;
	}

	@Override
	public TextBox getCreateScriptCondition() {
		return createScriptCondition;
	}

	@Override
	public CheckBox getCreateScriptPerRow() {
		return createScriptPerRow;
	}
}
