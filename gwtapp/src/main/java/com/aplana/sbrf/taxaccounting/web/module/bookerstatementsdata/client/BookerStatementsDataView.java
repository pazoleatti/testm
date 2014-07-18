package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для Формы просмотра бухгалтерской отчётности
 *
 * @author lhaziev
 */
public class BookerStatementsDataView extends ViewWithUiHandlers<BookerStatementsDataUiHandlers>
        implements BookerStatementsDataPresenter.MyView {

    interface Binder extends UiBinder<Widget, BookerStatementsDataView> {
    }

    private final static int DEFAULT_ACCOUNT_PERIOD_LABEL_WIDTH = 150;

    @UiField
    Label title;

    @UiField
    HorizontalPanel centerBlock;

    @UiField
    Label departmentIdLabel;

    @UiField
    Label accountPeriodLabel;

    @UiField
    Anchor returnAnchor;

    @UiField
    FileUploadWidget fileUploader;

    @UiField
    LinkButton deleteButton;

    @UiField
    GenericDataGrid<RefBookDataRow> dataTable;
    @UiField
    FlexiblePager pager;

    @Inject
    @UiConstructor
    public BookerStatementsDataView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        dataTable.setSelectionModel(new NoSelectionModel<RefBookDataRow>());
        dataTable.setPageSize(pager.getPageSize());
        pager.setDisplay(dataTable);
        dataTable.setVisible(false);
        pager.setVisible(false);
        recalcAccountPeriodLabelWidth();
    }

    @Override
    public void addAccImportValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
        fileUploader.addValueChangeHandler(valueChangeHandler);
    }


    @Override
    public void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows) {
        dataTable.setVisible(true);
        pager.setVisible(true);
        if (dataRows == null) {
            dataTable.setRowCount(0);
            dataTable.setRowData(new ArrayList<RefBookDataRow>());
        } else {
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
            dataTable.setRowCount(totalCount);
            dataTable.setRowData(start, dataRows);
        }
    }

    @Override
    public void updateTable() {
        Range range = new Range(pager.getPageStart(), pager.getPageSize());
        dataTable.setVisibleRangeAndClearData(range, true);
    }

    @Override
    public void setTableColumns(final List<RefBookColumn> columns) {
        while (dataTable.getColumnCount() > 0) {
            dataTable.removeColumn(0);
        }

        for (final RefBookColumn header : columns) {
            if (!header.getAlias().equals("DEPARTMENT_ID") && !header.getAlias().equals("ACCOUNT_PERIOD_ID")) {
                TextColumn<RefBookDataRow> column = new TextColumn<RefBookDataRow>() {
                    @Override
                    public String getValue(RefBookDataRow object) {
                        return object.getValues().get(header.getAlias());
                    }
                };
                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
                dataTable.addResizableSortableColumn(column, header.getName());
                dataTable.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
            }
        }
    }

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data) {
        dataTable.setPageSize(pageSize);
        data.addDataDisplay(dataTable);
    }


    @UiHandler("deleteButton")
    void onDeleteClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onDelete();
        }

    }

    @UiHandler("returnAnchor")
    void onReturnAnchorClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onReturnClicked();
            event.preventDefault();
            event.stopPropagation();
        }
    }

    @Override
    public void setAdditionalFormInfo(String department, String accountPeriod, String type) {
        departmentIdLabel.setText(department);
        accountPeriodLabel.setText(accountPeriod);
        title.setText(type);
    }

    private HasHorizontalAlignment.HorizontalAlignmentConstant convertAlignment(HorizontalAlignment alignment) {
        switch (alignment) {
            case ALIGN_LEFT:
                return HasHorizontalAlignment.ALIGN_LEFT;
            case ALIGN_CENTER:
                return HasHorizontalAlignment.ALIGN_CENTER;
            case ALIGN_RIGHT:
                return HasHorizontalAlignment.ALIGN_RIGHT;
            default:
                return HasHorizontalAlignment.ALIGN_LEFT;
        }
    }
    /**
     * Перечет и установка ширины контейнера с значением налогового периода.
     * делается в ручную потому что контернер находится в табличной ячейке
     * и заворачивание во многоточние происходит если только явно задать ширину в пикселях
     */
    private void recalcAccountPeriodLabelWidth(){

        // сбрасывает прошлое значение лейбла что бы он не мешал замеру его родительского контейнра
        accountPeriodLabel.getElement().getStyle().setPropertyPx("width", DEFAULT_ACCOUNT_PERIOD_LABEL_WIDTH);

        // берется ширина ячейки в которой находится контейнер с информационном блоком формы
        Element centerBlockParentElement = centerBlock.getElement().getParentElement();
        if (centerBlockParentElement != null) {
            Integer parentWidth = centerBlockParentElement.getOffsetWidth();
            if (parentWidth != null) {
                int width = parentWidth - 135;
                if (width > 0) {
                    accountPeriodLabel.getElement().getStyle().setPropertyPx("width", width);
                }
            }
        }
    }

    @Override
    public String getDepartmentName() {
        return departmentIdLabel.getText();
    }

    @Override
    public String getAccountPeriodName() {
        return accountPeriodLabel.getText();
    }

    @Override
    public String getBookerReportType() {
        return title.getText();
    }
}
