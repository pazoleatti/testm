package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
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
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FormDataListView extends
		ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {
	
	private static final int PAGE_SIZE = 20;

	public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы";
	public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы";
	public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String PERIOD_YEAR_TITLE = "Год";
	public static final String REPORT_PERIOD_TITLE = "Период";
    public static final String PERIOD_MONTH_TITLE = "Месяц";
	public static final String FORM_DATA_STATE_TITLE = "Состояние";
	public static final String FORM_DATA_RETURN_TITLE = "Признак возрата";

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

	private FormDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

	@UiField
	Panel filterContentPanel;

	@UiField
	DataGrid<FormDataSearchResultItem> formDataTable;
	
	@UiField
	FlexiblePager pager;

	@UiField
	Label titleDesc;

	@UiField
	Label formHeader;
	
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
				return object.getDepartmentName();
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

        TextColumn<FormDataSearchResultItem> periodMonthColumn = new TextColumn<FormDataSearchResultItem>() {
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
		
		

		formDataTable.addColumn(formKindColumn, getHeader(FORM_DATA_KIND_TITLE));
		formDataTable.addColumn(linkColumn, getHeader(FORM_DATA_TYPE_TITLE));
		formDataTable.setColumnWidth(linkColumn, 40, Style.Unit.EM);
		
		formDataTable.addColumn(departmentColumn, getHeader(DEPARTMENT_TITLE));
		
        formDataTable.addColumn(periodYearColumn, getHeader(PERIOD_YEAR_TITLE));
        formDataTable.setColumnWidth(periodYearColumn, 5, Style.Unit.EM);
        periodYearColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        
		formDataTable.addColumn(reportPeriodColumn, getHeader(REPORT_PERIOD_TITLE));
		formDataTable.addColumn(periodMonthColumn, getHeader(PERIOD_MONTH_TITLE));
		formDataTable.addColumn(stateColumn, getHeader(FORM_DATA_STATE_TITLE));
		formDataTable.addColumn(returnColumn, getHeader(FORM_DATA_RETURN_TITLE));

		pager.setDisplay(formDataTable);
		formDataTable.setPageSize(PAGE_SIZE);
		dataProvider.addDataDisplay(formDataTable);

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
	public void setTableData(int start, long totalCount, List<FormDataSearchResultItem> records) {
		formDataTable.setRowCount((int) totalCount);
		formDataTable.setRowData(start, records);
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

	private Header<String> getHeader(final String columnName){
		Header<String> columnHeader = new Header<String>(new SortingHeaderCell()) {
			@Override
			public String getValue() {
				return columnName;
			}
		};

		columnHeader.setUpdater(new ValueUpdater<String>() {
			@Override
			public void update(String value) {
				setAscSorting(!isAscSorting);
				setSortByColumn(columnName);
				if (getUiHandlers() != null) {
					getUiHandlers().onSortingChanged();
				}
			}
		});
		return columnHeader;
	}

	private void setSortByColumn(String sortByColumn){
		if(FORM_DATA_KIND_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.KIND;
		} else if (FORM_DATA_TYPE_TITLE.equals(sortByColumn)){
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

}
