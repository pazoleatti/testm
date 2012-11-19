package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
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
    TextArea createScriptBody;

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

    public ListBox getFormListBox() {
        return formListBox;
    }

    public TextArea getCreateScriptBody() {
        return createScriptBody;
    }
}
