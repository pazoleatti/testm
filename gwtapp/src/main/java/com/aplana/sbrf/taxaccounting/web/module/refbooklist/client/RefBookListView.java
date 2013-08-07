package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * View для формы списка справочников
 *
 * @author Stanislav Yasinskiy
 */
public class RefBookListView extends ViewWithUiHandlers<RefBookListUiHandlers>
        implements RefBookListPresenter.MyView {

    interface Binder extends UiBinder<Widget, RefBookListView> {
    }

    @Inject
    @UiConstructor
    public RefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }
}