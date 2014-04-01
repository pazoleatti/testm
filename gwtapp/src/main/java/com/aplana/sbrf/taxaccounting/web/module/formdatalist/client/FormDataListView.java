package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FormDataListView extends
		ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {
	
	public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы";
	public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы";
    public static final String FORM_DATA_TYPE_TITLE_D = "Вид формы";
	public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String PERIOD_YEAR_TITLE = "Год";
	public static final String REPORT_PERIOD_TITLE = "Период";
    public static final String PERIOD_MONTH_TITLE = "Месяц";
	public static final String FORM_DATA_STATE_TITLE = "Состояние";
	public static final String FORM_DATA_RETURN_TITLE = "Признак возрата";

    public static final String FORM_DATA_CREATE = "Создать налоговую форму...";
    public static final String FORM_DATA_CREATE_D = "Создать форму...";
    public static final String FORM_DATA_CREATE_TITLE = "Создание налоговой формы";
    public static final String FORM_DATA_CREATE_TITLE_D = "Создание формы";

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

    private GenericDataGrid.DataGridResizableHeader formKindHeader, formTypeHeader, periodMonthHeader;
    private TextColumn<FormDataSearchResultItem> periodMonthColumn;

	private FormDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

    private Map<Integer, String> departmentFullNames;

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

	private AsyncDataProvider<FormDataSearchResultItem> dataProvider = new  AsyncDataProvider<FormDataSearchResultItem>() {
		@Override
		protected void onRangeChanged(HasData<FormDataSearchResultItem> display) {
			if (getUiHandlers() != null){
				Range range = display.getVisibleRange();
				getUiHandlers().onRangeChange(range.getStart(), range.getLength());	
			}
		}
	};

	@Inject
	public FormDataListView(final MyBinder binder) {
		initWidget(binder.createAndBindUi(this));

		TextColumn<FormDataSearchResultItem> formKindColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getFormDataKind().getName();
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

		TextColumn<FormDataSearchResultItem> stateColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getState().getName();
			}
		};

        TextColumn<FormDataSearchResultItem> returnColumn = new TextColumn<FormDataSearchResultItem>() {
            @Override
            public String getValue(FormDataSearchResultItem object) {
                Boolean isReturn = object.getReturnSign();
                if (isReturn == null){
                    return "";
                }
                return isReturn ? "Возвращена" : "Не возвращена";
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
			public FormDataSearchResultItem getValue(
					FormDataSearchResultItem object) {
				return object;
			}
		};

        formKindHeader = (GenericDataGrid.DataGridResizableHeader)getHeader(FORM_DATA_KIND_TITLE, formKindColumn);
		formDataTable.addColumn(formKindColumn, formKindHeader);
        formDataTable.setColumnWidth(formKindColumn, 8.5, Style.Unit.EM);
        formTypeHeader = (GenericDataGrid.DataGridResizableHeader)getHeader(FORM_DATA_TYPE_TITLE, linkColumn);
		formDataTable.addColumn(linkColumn, formTypeHeader);

		formDataTable.addColumn(departmentColumn, getHeader(DEPARTMENT_TITLE, departmentColumn));

        formDataTable.addColumn(periodYearColumn, getHeader(PERIOD_YEAR_TITLE, periodYearColumn));
        formDataTable.setColumnWidth(periodYearColumn, 3.5, Style.Unit.EM);
        periodYearColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        
		formDataTable.addColumn(reportPeriodColumn, getHeader(REPORT_PERIOD_TITLE, reportPeriodColumn));
        formDataTable.setColumnWidth(reportPeriodColumn, 7, Style.Unit.EM);
        periodMonthHeader = (GenericDataGrid.DataGridResizableHeader)getHeader(PERIOD_MONTH_TITLE, periodMonthColumn);
		formDataTable.addColumn(periodMonthColumn, periodMonthHeader);
        formDataTable.setColumnWidth(periodMonthColumn, 6, Style.Unit.EM);
		formDataTable.addColumn(stateColumn, getHeader(FORM_DATA_STATE_TITLE, stateColumn));
        formDataTable.setColumnWidth(stateColumn, 6, Style.Unit.EM);
		formDataTable.addColumn(returnColumn, getHeader(FORM_DATA_RETURN_TITLE, returnColumn));
        formDataTable.setColumnWidth(returnColumn, 8, Style.Unit.EM);

		pager.setDisplay(formDataTable);
        formDataTable.setPageSize(pager.getPageSize());
		dataProvider.addDataDisplay(formDataTable);

	}

    @Override
    public void updateFormDataTable(TaxType taxType) {
        if (!taxType.equals(TaxType.DEAL)) {
            create.setText(FORM_DATA_CREATE);
            create.setTitle(FORM_DATA_CREATE_TITLE);
            formKindHeader.setTitle(FORM_DATA_KIND_TITLE);
            formTypeHeader.setTitle(FORM_DATA_TYPE_TITLE);
            periodMonthHeader.setTitle(PERIOD_MONTH_TITLE);
            formDataTable.setColumnWidth(periodMonthColumn, 6, Style.Unit.EM);
        } else {
            create.setText(FORM_DATA_CREATE_D);
            create.setTitle(FORM_DATA_CREATE_TITLE_D);
            formKindHeader.setTitle(FORM_DATA_KIND_TITLE_D);
            formTypeHeader.setTitle(FORM_DATA_TYPE_TITLE_D);
            periodMonthHeader.setTitle("");
            formDataTable.setColumnWidth(periodMonthColumn, 0, Style.Unit.EM);
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
	public void setTableData(int start, long totalCount, List<FormDataSearchResultItem> records, Map<Integer, String> departmentFullNames) {
		formDataTable.setRowCount((int) totalCount);
		formDataTable.setRowData(start, records);
        this.departmentFullNames = departmentFullNames;
	}

	@Override
	public FormDataSearchOrdering getSearchOrdering(){
		if (sortByColumn == null){
			setSortByColumn("");
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

	/**
	 * Добавление заголовка для столбца, который может резайзиться
	 * @param columnName название
	 * @param returnColumn объект колонки
	 * @return заголовок с сортировкой и резайзом
	 */
	private Header<String> getHeader(final String columnName, Column<FormDataSearchResultItem, ?> returnColumn){
		GenericDataGrid.DataGridResizableHeader resizableHeader;
		final SortingHeaderCell headerCell = new SortingHeaderCell();
		resizableHeader = formDataTable.createResizableHeader(columnName, returnColumn, headerCell);

		resizableHeader.setUpdater(new ValueUpdater<String>() {
			@Override
			public void update(String value) {
				setAscSorting(headerCell.isAscSort());
				setSortByColumn(columnName);
				if (getUiHandlers() != null) {
					getUiHandlers().onSortingChanged();
				}
			}
		});
		return resizableHeader;
	}

	private void setSortByColumn(String sortByColumn){
		if (FORM_DATA_KIND_TITLE.equals(sortByColumn) || FORM_DATA_KIND_TITLE_D.equals(sortByColumn) ) {
			this.sortByColumn = FormDataSearchOrdering.KIND;
		} else if (FORM_DATA_TYPE_TITLE.equals(sortByColumn) || FORM_DATA_TYPE_TITLE_D.equals(sortByColumn)) {
			this.sortByColumn = FormDataSearchOrdering.FORM_TYPE_NAME;
		} else if (PERIOD_YEAR_TITLE.equals(sortByColumn)) {
			this.sortByColumn = FormDataSearchOrdering.YEAR;
		} else if (FORM_DATA_RETURN_TITLE.equals(sortByColumn)) {
			this.sortByColumn = FormDataSearchOrdering.RETURN;
		} else if (DEPARTMENT_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.DEPARTMENT_NAME;
		} else if (REPORT_PERIOD_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.REPORT_PERIOD_NAME;
		} else if (FORM_DATA_STATE_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.STATE;
		} else {
			this.sortByColumn = FormDataSearchOrdering.ID;
		}
	}

	private void setAscSorting(boolean ascSorting){
		this.isAscSorting = ascSorting;
	}

	@Override
	public void updateData() {
		formDataTable.setVisibleRangeAndClearData(formDataTable.getVisibleRange(), true);
	}

	@Override
	public void updateData(int pageNumber) {
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
