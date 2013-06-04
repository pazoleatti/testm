package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFormView extends ViewWithUiHandlers implements AuditFormPresenter.MyView {

    interface Binder extends UiBinder<Widget, AuditFormView>{}

    @UiField
    Panel filterContentPanel;

    @UiField
    CellTable<LogSystem> formDataTable;

    @UiField
    AbstractPager pager;

    @UiField
    Label titleDesc;

    @Inject
    @UiConstructor
    public AuditFormView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AuditFormPresenter.TYPE_auditFilterPresenter) {
            filterContentPanel.clear();
            if (content!=null){
                filterContentPanel.add(content);
            }
        }
        else {
            super.setInSlot(slot, content);
        }

    }
}
