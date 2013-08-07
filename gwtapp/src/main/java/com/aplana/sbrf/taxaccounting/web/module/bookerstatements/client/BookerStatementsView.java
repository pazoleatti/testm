package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Map;

/**
 * View для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class BookerStatementsView extends ViewWithUiHandlers<BookerStatementsUiHandlers>
        implements BookerStatementsPresenter.MyView, ReportPeriodSelectHandler {

    interface Binder extends UiBinder<Widget, BookerStatementsView> {
    }

    @UiField(provided = true)
    ReportPeriodPicker period = new ReportPeriodPicker(this);

    @UiField
    DataGrid<DataRow<Cell>> formDataTable;

    @UiField
    FlexiblePager pager;

    @UiField
    DepartmentPicker departmentPicker;

    @UiField
    ListBox bookerReportType;

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    @Inject
    @UiConstructor
    public BookerStatementsView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        pager.setDisplay(formDataTable);

    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}