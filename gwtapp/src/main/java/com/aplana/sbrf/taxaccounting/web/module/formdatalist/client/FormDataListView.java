package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;

public class FormDataListView extends ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

    @UiField
	Panel filterContentPanel;

	@UiField
	GenericDataGrid<FormDataSearchResultItem> formDataTable;
	
	@UiField
	FlexiblePager pager;

	@UiField
	Label titleDesc;

	@UiField
	Label formHeader;

    @UiField
    LinkButton create;

    private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    private GenericDataGrid.DataGridResizableHeader formKindHeader, formTypeHeader, periodMonthHeader, comparativPeriodHeader;
    private TextColumn<FormDataSearchResultItem> periodMonthColumn, comparativPeriodColumn;

    private FormDataSearchOrdering sortByColumn;

    private boolean isAscSorting;

    private Map<Integer, String> departmentFullNames;

    private SingleSelectionModel<FormDataSearchResultItem> selectionModel;

    private ProvidesKey<FormDataSearchResultItem> keyProvider = new ProvidesKey<FormDataSearchResultItem>() {
        @Override
        public Long getKey(FormDataSearchResultItem item) {
            return item.getFormDataId();
        }
    };

    private AsyncDataProvider<FormDataSearchResultItem> dataProvider = new AsyncDataProvider<FormDataSearchResultItem>(keyProvider) {
        @Override
        protected void onRangeChanged(HasData<FormDataSearchResultItem> display) {
            if (getUiHandlers() != null) {
                // заполенине параметров по какой сортировать
                if (formDataTable.getColumnSortList().size() > 0) {
                    isAscSorting = formDataTable.getColumnSortList().get(0).isAscending();
                    setSortByColumn(formDataTable.getColumnSortList().get(0).getColumn().getDataStoreName());
                }
                Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

	@Inject
	public FormDataListView(final MyBinder binder) {
		initWidget(binder.createAndBindUi(this));

        selectionModel = new SingleSelectionModel<FormDataSearchResultItem>(keyProvider);
        formDataTable.setSelectionModel(selectionModel);

		TextColumn<FormDataSearchResultItem> formKindColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getFormDataKind().getTitle();
			}
		};

		TextColumn<FormDataSearchResultItem> departmentColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return departmentFullNames.get(object.getDepartmentId());
			}
		};

		TextColumn<FormDataSearchResultItem> reportPeriodColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getReportPeriodName();
			}
		};

        comparativPeriodColumn = new TextColumn<FormDataSearchResultItem>() {
            @Override
            public String getValue(FormDataSearchResultItem object) {
                return object.getComparativPeriodName();
            }
        };

		TextColumn<FormDataSearchResultItem> stateColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getState().getTitle();
			}
		};

        TextColumn<FormDataSearchResultItem> returnColumn = new TextColumn<FormDataSearchResultItem>() {
            @Override
            public String getValue(FormDataSearchResultItem object) {
                Boolean isReturn = object.getReturnSign();
                return isReturn == null ? "" : isReturn ? "Возвращена" : "Не возвращена";
            }
        };

        TextColumn<FormDataSearchResultItem> periodYearColumn = new TextColumn<FormDataSearchResultItem>() {
            @Override
            public String getValue(FormDataSearchResultItem object) {
                return String.valueOf(object.getReportPeriodYear());
            }
        };

        periodMonthColumn = new TextColumn<FormDataSearchResultItem>() {
            @Override
            public String getValue(FormDataSearchResultItem object) {
                Integer periodOrder = object.getReportPeriodMonth();
                return periodOrder == null ? "" : Formats.getRussianMonthNameWithTier(periodOrder);
            }
        };

        Column<FormDataSearchResultItem, FormDataSearchResultItem> linkColumn = new Column<FormDataSearchResultItem, FormDataSearchResultItem>(
                new AbstractCell<FormDataSearchResultItem>() {

                    @Override
                    public void render(Context context,
                                       FormDataSearchResultItem formData,
                                       SafeHtmlBuilder sb) {
                        if (formData == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#"
                                + FormDataPresenter.NAME_TOKEN + ";"
                                + FormDataPresenter.FORM_DATA_ID + "="
                                + formData.getFormDataId() + "\">"
                                + formData.getFormTypeName() + "</a>");
                    }
                }) {
            @Override
            public FormDataSearchResultItem getValue(
                    FormDataSearchResultItem object) {
                return object;
            }
        };

        formKindHeader = formDataTable.createResizableHeader(FormDataListUtils.FORM_DATA_KIND_TITLE, formKindColumn);
		formDataTable.addColumn(formKindColumn, formKindHeader);
        formDataTable.setColumnWidth(formKindColumn, 8.5, Style.Unit.EM);

        formTypeHeader = formDataTable.createResizableHeader(FormDataListUtils.FORM_DATA_TYPE_TITLE, linkColumn);
		formDataTable.addColumn(linkColumn, formTypeHeader);

		formDataTable.addResizableColumn(departmentColumn, FormDataListUtils.DEPARTMENT_TITLE);

        formDataTable.addResizableColumn(periodYearColumn, FormDataListUtils.PERIOD_YEAR_TITLE);
        formDataTable.setColumnWidth(periodYearColumn, 3.5, Style.Unit.EM);
        periodYearColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
		formDataTable.addResizableColumn(reportPeriodColumn, FormDataListUtils.REPORT_PERIOD_TITLE);
        formDataTable.setColumnWidth(reportPeriodColumn, 7, Style.Unit.EM);

        comparativPeriodHeader = formDataTable.createResizableHeader(FormDataListUtils.REPORT_PERIOD_TITLE, comparativPeriodColumn);
        formDataTable.addColumn(comparativPeriodColumn, comparativPeriodHeader);
        formDataTable.setColumnWidth(comparativPeriodColumn, 7, Style.Unit.EM);

        periodMonthHeader = formDataTable.createResizableHeader(FormDataListUtils.PERIOD_MONTH_TITLE, periodMonthColumn);
		formDataTable.addColumn(periodMonthColumn, periodMonthHeader);
        formDataTable.setColumnWidth(periodMonthColumn, 6, Style.Unit.EM);

		formDataTable.addResizableColumn(stateColumn, FormDataListUtils.FORM_DATA_STATE_TITLE);
        formDataTable.setColumnWidth(stateColumn, 6, Style.Unit.EM);

		formDataTable.addResizableColumn(returnColumn, FormDataListUtils.FORM_DATA_RETURN_TITLE);
        formDataTable.setColumnWidth(returnColumn, 8, Style.Unit.EM);

        formKindColumn.setDataStoreName(FormDataSearchOrdering.KIND.name());
        linkColumn.setDataStoreName(FormDataSearchOrdering.FORM_TYPE_NAME.name());
        departmentColumn.setDataStoreName(FormDataSearchOrdering.DEPARTMENT_NAME.name());
        periodYearColumn.setDataStoreName(FormDataSearchOrdering.YEAR.name());
        reportPeriodColumn.setDataStoreName(FormDataSearchOrdering.REPORT_PERIOD_NAME.name());
        comparativPeriodColumn.setDataStoreName(FormDataSearchOrdering.COMPARATIV_PERIOD_NAME.name());
        periodMonthColumn.setDataStoreName(FormDataSearchOrdering.REPORT_PERIOD_MONTH_NAME.name());
        stateColumn.setDataStoreName(FormDataSearchOrdering.STATE.name());
        returnColumn.setDataStoreName(FormDataSearchOrdering.RETURN.name());

        formKindColumn.setSortable(true);
        linkColumn.setSortable(true);
        departmentColumn.setSortable(true);
        periodYearColumn.setSortable(true);
        reportPeriodColumn.setSortable(true);
        comparativPeriodColumn.setSortable(true);
        periodMonthColumn.setSortable(true);
        stateColumn.setSortable(true);
        returnColumn.setSortable(true);

		pager.setDisplay(formDataTable);
        formDataTable.setPageSize(pager.getPageSize());
		dataProvider.addDataDisplay(formDataTable);

        formDataTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(formDataTable));
        formDataTable.getColumnSortList().setLimit(1);       // сортировка только по одной колонке
	}

    @Override
    public Long getSelectedId() {
        FormDataSearchResultItem item = selectionModel.getSelectedObject();
        return (Long) selectionModel.getKey(item);
    }

    @Override
    public void updateFormDataTable(TaxType taxType) {
        if (taxType.isTax()) {
            create.setText(FormDataListUtils.FORM_DATA_CREATE);
            create.setTitle(FormDataListUtils.FORM_DATA_CREATE_TITLE);
            formKindHeader.setTitle(FormDataListUtils.FORM_DATA_KIND_TITLE);
            formTypeHeader.setTitle(FormDataListUtils.FORM_DATA_TYPE_TITLE);
        } else {
            create.setText(FormDataListUtils.FORM_DATA_CREATE_D);
            create.setTitle(FormDataListUtils.FORM_DATA_CREATE_TITLE_D);
            formKindHeader.setTitle(FormDataListUtils.FORM_DATA_KIND_TITLE_D);
            formTypeHeader.setTitle(FormDataListUtils.FORM_DATA_TYPE_TITLE_D);
        }
        if (taxType.equals(TaxType.INCOME) || taxType.equals(TaxType.VAT) || taxType.equals(TaxType.PROPERTY)
                || taxType.equals(TaxType.TRANSPORT) || taxType.equals(TaxType.MARKET)) {
            periodMonthHeader.setTitle(FormDataListUtils.PERIOD_MONTH_TITLE);
            formDataTable.setColumnWidth(periodMonthColumn, 6, Style.Unit.EM);
        } else {
            periodMonthHeader.setTitle("");
            formDataTable.setColumnWidth(periodMonthColumn, 0, Style.Unit.EM);
        }
        if (taxType.equals(TaxType.ETR)) {
            comparativPeriodHeader.setTitle(FormDataListUtils.COMPARATIV_PERIOD_TITLE);
            formDataTable.setColumnWidth(comparativPeriodColumn, 7, Style.Unit.EM);
        } else {
            comparativPeriodHeader.setTitle("");
            formDataTable.setColumnWidth(comparativPeriodColumn, 0, Style.Unit.EM);

        }
        formDataTable.redrawHeaders();

    }

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		if (slot == FormDataListPresenter.TYPE_filterPresenter) {
			filterContentPanel.clear();
			if (content != null) {
				filterContentPanel.add(content);
			}
		} else {
			super.setInSlot(slot, content);
		}
	}

	@Override
	public void setTableData(int start, long totalCount, List<FormDataSearchResultItem> records, Map<Integer, String> departmentFullNames, Long selectedItemId) {
        formDataTable.setRowCount((int) totalCount);
		formDataTable.setRowData(start, records);
        this.departmentFullNames = departmentFullNames;
        selectionModel.clear();
        if (selectedItemId != null) {
            for(FormDataSearchResultItem item: records) {
                if (item.getFormDataId().equals(selectedItemId)) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
	}

    @Override
    public void setPage(Integer page) {
        if (page != null) pager.setPage(page);
    }

    @Override
    public int getPage() {
        return pager.getPage();
    }

    @Override
    public FormDataSearchOrdering getSearchOrdering() {
        if (sortByColumn == null) {
            sortByColumn = FormDataSearchOrdering.DATE;
        }
        return sortByColumn;
    }

	@Override
	public boolean isAscSorting(){
		return isAscSorting;
	}

	@Override
	public void updateTitle(String title){
		titleDesc.setText(title);
	}

	@Override
	public void updateHeader(String title){
		formHeader.setText(title);
	}

	private void setSortByColumn(String sortByColumn){
        this.sortByColumn = FormDataListUtils.getSortByColumn(sortByColumn);
	}

	@Override
	public void updateData() {
		formDataTable.setVisibleRangeAndClearData(formDataTable.getVisibleRange(), true);
	}

	@Override
	public void updateData(int pageNumber) {
        if(pageNumber == 0){
            formDataTable.getColumnSortList().clear();
        }
		if (pager.getPage() == pageNumber){
			updateData();
		} else {
			pager.setPage(pageNumber);
		}
	}

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }

    @Override
    public void updatePageSize(TaxType taxType) {
        pager.setType("formDataList" + taxType.getCode());
        formDataTable.setPageSize(pager.getPageSize());
    }
}
