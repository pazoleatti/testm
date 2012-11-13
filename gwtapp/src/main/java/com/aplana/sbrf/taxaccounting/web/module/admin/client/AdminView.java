package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
}
