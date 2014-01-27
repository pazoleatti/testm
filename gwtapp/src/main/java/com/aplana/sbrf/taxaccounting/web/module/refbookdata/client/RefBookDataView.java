package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RefBookDataView extends ViewWithUiHandlers<RefBookDataUiHandlers> implements RefBookDataPresenter.MyView {

	interface Binder extends UiBinder<Widget, RefBookDataView> {
	}

	@UiField
	GenericDataGrid<RefBookDataRow> refbookDataTable;
	@UiField
	FlexiblePager pager;
	@UiField
	Panel contentPanel;
	@UiField
	Label titleDesc;
	@UiField
	DateBox relevanceDate;

	SingleSelectionModel<RefBookDataRow> selectionModel = new SingleSelectionModel<RefBookDataRow>();

	@Inject
	public RefBookDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

		relevanceDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		relevanceDate.setValue(new Date());
		relevanceDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				if (getUiHandlers() != null) {
					getUiHandlers().onRelevanceDateChanged();
				}
			}
		});
		refbookDataTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				getUiHandlers().onSelectionChanged();
			}
		});
        refbookDataTable.setPageSize(pager.getPageSize());
		pager.setDisplay(refbookDataTable);
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		if (slot == RefBookDataPresenter.TYPE_editFormPresenter) {
			contentPanel.clear();
			if (content!=null){
				contentPanel.add(content);
			}
		}
		else {
			super.setInSlot(slot, content);
		}
	}

	@Override
	public void setTableColumns(final List<RefBookColumn> columns) {
		for (final RefBookColumn header : columns) {
			TextColumn<RefBookDataRow> column = new TextColumn<RefBookDataRow>() {
				@Override
				public String getValue(RefBookDataRow object) {
					return object.getValues().get(header.getAlias());
				}
			};
			column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
			refbookDataTable.addColumn(column, header.getName());
			refbookDataTable.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
		}
	}

	@Override
	public void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data) {
		refbookDataTable.setPageSize(pageSize);
		data.addDataDisplay(refbookDataTable);
	}

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
	public void setRange(Range range) {
		refbookDataTable.setVisibleRangeAndClearData(range, true);
	}

	@Override
	public void updateTable() {
		Range range = new Range(pager.getPageStart(), pager.getPageSize());
		refbookDataTable.setVisibleRangeAndClearData(range, true);
		selectionModel.clear();
	}

	@Override
	public void setRefBookNameDesc(String desc) {
		titleDesc.setText(desc);
	}

	@Override
	public void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows) {
		if (dataRows == null) {
			refbookDataTable.setRowCount(0);
			refbookDataTable.setRowData(new ArrayList<RefBookDataRow>());
		} else {
			refbookDataTable.setRowCount(totalCount);
			refbookDataTable.setRowData(start, dataRows);
		}
	}

	@Override
	public void setSelected(Long recordId) {
		selectionModel.clear();
		int i = 0;
		for (RefBookDataRow row : refbookDataTable.getVisibleItems()) {

			if (row.getRefBookRowId() == recordId) {
				selectionModel.setSelected(row, true);
				refbookDataTable.setKeyboardSelectedRow(i, true);
				return;
			}
			i++;
		}
	}

	@Override
    public void resetRefBookElements() {
        int i;
        while ((i = refbookDataTable.getColumnCount()) != 0) {
            refbookDataTable.removeColumn(i - 1);
        }
    }

	@Override
	public RefBookDataRow getSelectedRow() {
		return selectionModel.getSelectedObject();
	}

	@Override
	public Date getRelevanceDate() {
		return relevanceDate.getValue();
	}

	@UiHandler("addRow")
	void addRowButtonClicked(ClickEvent event) {
        selectionModel.clear();
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
	}

	@UiHandler("deleteRow")
	void deleteRowButtonClicked(ClickEvent event) {
		if (selectionModel.getSelectedObject() == null) {
			return;
		}
        Dialog.confirmMessage("Удалить выбранную запись справочника?", new DialogHandler() {
            @Override
            public void yes() {
                if (getUiHandlers() != null) {
                    getUiHandlers().onDeleteRowClicked();
                }
                Dialog.hideMessage();
            }

            @Override
            public void no() {
                Dialog.hideMessage();
            }

            @Override
            public void close() {
                no();
            }
        });
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
}
