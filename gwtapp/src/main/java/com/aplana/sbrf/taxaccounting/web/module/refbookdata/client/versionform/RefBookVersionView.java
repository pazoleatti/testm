package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

public class RefBookVersionView extends ViewWithUiHandlers<RefBookVersionUiHandlers> implements RefBookVersionPresenter.MyView {

	interface Binder extends UiBinder<Widget, RefBookVersionView> {
	}

	@UiField
	GenericDataGrid<RefBookDataRow> refBookDataTable;
	@UiField
	FlexiblePager pager;

    SingleSelectionModel<RefBookDataRow> selectionModel = new SingleSelectionModel<RefBookDataRow>();

	@Inject
	public RefBookVersionView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		refBookDataTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				getUiHandlers().onSelectionChanged();
			}
		});
        refBookDataTable.setPageSize(pager.getPageSize());
        refBookDataTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
		pager.setDisplay(refBookDataTable);
	}

	@Override
	public void setTableColumns(final List<RefBookColumn> columns) {
        TextColumn<RefBookDataRow> versionFromColumn = new TextColumn<RefBookDataRow>() {
            @Override
            public String getValue(RefBookDataRow object) {
                return object.getValues().get(RefBook.RECORD_VERSION_FROM_ALIAS);
            }
        };
        refBookDataTable.addResizableColumn(versionFromColumn, RefBook.REF_BOOK_VERSION_FROM_TITLE);
        refBookDataTable.setColumnWidth(versionFromColumn, RefBook.REF_BOOK_VERSION_FROM_WIDTH, Style.Unit.EM);

        TextColumn<RefBookDataRow> versionToColumn = new TextColumn<RefBookDataRow>() {
            @Override
            public String getValue(RefBookDataRow object) {
                return object.getValues().get(RefBook.RECORD_VERSION_TO_ALIAS);
            }
        };
        refBookDataTable.addResizableColumn(versionToColumn, RefBook.REF_BOOK_VERSION_TO_TITLE);
        refBookDataTable.setColumnWidth(versionToColumn, RefBook.REF_BOOK_VERSION_TO_WIDTH, Style.Unit.EM);

		for (final RefBookColumn header : columns) {
			TextColumn<RefBookDataRow> column = new TextColumn<RefBookDataRow>() {
				@Override
				public String getValue(RefBookDataRow object) {
					return object.getValues().get(header.getAlias());
				}
			};
			column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
			refBookDataTable.addResizableColumn(column, header.getName());
			refBookDataTable.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
		}
	}

	@Override
	public void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data) {
		refBookDataTable.setPageSize(pageSize);
		data.addDataDisplay(refBookDataTable);
	}

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
	public void setRange(Range range) {
		refBookDataTable.setVisibleRangeAndClearData(range, true);
	}

	@Override
	public void updateTable() {
        selectionModel.clear();
		Range range = new Range(pager.getPageStart(), pager.getPageSize());
		refBookDataTable.setVisibleRangeAndClearData(range, true);
	}

	@Override
	public void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows, Long selectedItem) {
        if (dataRows == null) {
            refBookDataTable.setRowCount(0);
            refBookDataTable.setRowData(new ArrayList<RefBookDataRow>());
        } else {
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
            refBookDataTable.setRowCount(totalCount);
            refBookDataTable.setRowData(start, dataRows);
            if (selectedItem != null) {
                for(RefBookDataRow item: dataRows) {
                    if (item.getRefBookRowId().equals(selectedItem)) {
                        selectionModel.setSelected(item, true);
                        break;
                    }
                }
            }
            //Если не было среди записей необходимой, то выставляем на первую
            if (selectionModel.getSelectedObject()==null){
                selectionModel.setSelected(dataRows.get(0), true);
            }
        }
	}

	@Override
	public void setSelected(Long recordId) {
		selectionModel.clear();
		int i = 0;
		for (RefBookDataRow row : refBookDataTable.getVisibleItems()) {

			if (row.getRefBookRowId().equals(recordId)) {
				selectionModel.setSelected(row, true);
				refBookDataTable.setKeyboardSelectedRow(i, true);
				return;
			}
			i++;
		}
	}

	@Override
    public void resetRefBookElements() {
        int i;
        while ((i = refBookDataTable.getColumnCount()) != 0) {
            refBookDataTable.removeColumn(i - 1);
        }
    }

	@Override
	public RefBookDataRow getSelectedRow() {
		return selectionModel.getSelectedObject();
	}

    @Override
	public void deleteRowButtonClicked() {
		if (selectionModel.getSelectedObject() == null) {
			return;
		}
        getUiHandlers().onDeleteRowClicked();
        /*Dialog.confirmMessage("Удаление версии элемента справочника", "Удалить выбранную версию элемента справочника?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().onDeleteRowClicked();
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
        });*/
	}

	@Override
	public void setPage(int page) {
		pager.setPage(page);
	}

    @Override
    public int getPageStart() {
        return pager.getPageStart();
    }

    @Override
    public int getTotalCount() {
        return refBookDataTable.getRowCount();
    }

    @Override
    public void updateMode(FormMode mode) {
        refBookDataTable.setEnabled(mode != FormMode.CREATE);
    }

    @Override
    public Integer getSelectedRowIndex() {
        List<RefBookDataRow> visibleItems = refBookDataTable.getVisibleItems();
        RefBookDataRow selectedItem = selectionModel.getSelectedObject();
        for(int i = 0; i < visibleItems.size(); i++) {
            if (visibleItems.get(i) == selectedItem)
                return i;
        }
        return null;
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
