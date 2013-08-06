package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * View для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class BookStatementsView extends ViewWithUiHandlers<BookStatementsUiHandlers>
        implements BookStatementsPresenter.MyView {

    interface Binder extends UiBinder<Widget, BookStatementsView> {
    }

    @Inject
    @UiConstructor
    public BookStatementsView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }
}