package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * View для формы настроек подразделений
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationView extends ViewWithUiHandlers<TaxFormNominationUiHandlers>
        implements TaxFormNominationPresenter.MyView, SelectDepartmentsEventHandler {

    interface Binder extends UiBinder<Widget, TaxFormNominationView> {
    }


    @Inject
    @UiConstructor
    public TaxFormNominationView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @Override
    public void onDepartmentsReceived(SelectDepartmentsEvent event) {

    }
}