package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
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
    DateMaskBoxPicker relevanceDate;
    @UiField
    LinkButton addRow;
    @UiField
    LinkButton deleteRow;
    @UiField
    LinkAnchor backAnchor;
    @UiField
    Button search;
    @UiField
    LinkButton edit;
    @UiField
    Button cancelEdit;
    @UiField
    HTML separator;
    @UiField
    Label editModeLabel;
    @UiField
    TextBox filterText;
    @UiField
    Label relevanceDateLabel;

    private String searchPattern;

    SingleSelectionModel<RefBookDataRow> selectionModel = new SingleSelectionModel<RefBookDataRow>();

	@Inject
	public RefBookDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

		relevanceDate.setValue(new Date());
		relevanceDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
                if(event.getValue()==null){
                    relevanceDate.setValue(new Date());
                } else {
                    if (getUiHandlers() != null) {
                        getUiHandlers().onRelevanceDateChanged();
                    }
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
        refbookDataTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
		pager.setDisplay(refbookDataTable);
        filterText.addKeyPressHandler(new HandlesAllKeyEvents() {
            @Override
            public void onKeyDown(KeyDownEvent event) {}

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getUnicodeCharCode() == KeyCodes.KEY_ENTER){
                    search.click();
                }
            }

            @Override
            public void onKeyUp(KeyUpEvent event) {}
        });

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
            Column column;
            if (Formats.BOOLEAN.equals(header.getFormat())) {
                column = new Column<RefBookDataRow, Boolean>(new AbstractCell<Boolean>() {
                    @Override
                    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                        sb.append(value != null && value ? WidgetUtils.UNCHECKABLE_TRUE : WidgetUtils.UNCHECKABLE_FALSE);
                    }
                }) {
                    @Override
                    public Boolean getValue(RefBookDataRow object) {
                        String s = object.getValues().get(header.getAlias());
                        if (s != null && !s.trim().isEmpty()) {
                            try {
                                long l = Long.parseLong(s.trim());
                                return l > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }

                        } else {
                            return false;
                        }
                    }
                };
                column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            } else {
                column = new TextColumn<RefBookDataRow>() {
                    @Override
                    public String getValue(RefBookDataRow object) {
                        return object.getValues().get(header.getAlias());
                    }
                };

                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            }

			refbookDataTable.addResizableSortableColumn(column, header.getName());
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
        selectionModel.clear();
		Range range = new Range(pager.getPageStart(), pager.getPageSize());
		refbookDataTable.setVisibleRangeAndClearData(range, true);
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
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
			refbookDataTable.setRowCount(totalCount);
			refbookDataTable.setRowData(start, dataRows);
		}
	}

	@Override
	public void setSelected(Long recordId) {
        selectionModel.clear();
		int i = 0;
		for (RefBookDataRow row : refbookDataTable.getVisibleItems()) {

			if (row.getRefBookRowId().equals(recordId)) {
				selectionModel.setSelected(row, true);
				refbookDataTable.setKeyboardSelectedRow(i, true);
				return;
			}
			i++;
		}
	}

    @Override
    public Long getSelected() {
        RefBookDataRow selectedObject = selectionModel.getSelectedObject();
        if (selectedObject != null) {
            return selectedObject.getRefBookRowId();
        }
        return null;
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

    @Override
    public void setReadOnlyMode(boolean readOnly) {
        addRow.setVisible(!readOnly);
        deleteRow.setVisible(!readOnly);
    }

    @Override
    public int getPage(){
        return pager.getPage();
    }

    @Override
    public void setPage(int page){
        pager.setPage(page);
    }

    @UiHandler("addRow")
	void addRowButtonClicked(ClickEvent event) {
        selectionModel.clear();
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
	}

    @UiHandler("cancelEdit")
    void cancelEditButtonClicked(ClickEvent event) {
        Dialog.confirmMessage("Выход из режима редактирования", "Вы подтверждаете переход к режиму просмотра?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().setMode(FormMode.VIEW);
                getUiHandlers().cancelChanges();
            }
        });
    }

    @UiHandler("search")
    void searchButtonClicked(ClickEvent event) {
        searchPattern = filterText.getText();
        updateTable();
    }

    @UiHandler("edit")
    void editButtonClicked(ClickEvent event) {
        getUiHandlers().setMode(FormMode.EDIT);
    }

	@UiHandler("deleteRow")
	void deleteRowButtonClicked(ClickEvent event) {
		if (selectionModel.getSelectedObject() == null) {
			return;
		}
        Dialog.confirmMessage("Удаление элемента справочника", "Вы подтверждаете удаление всех версий элемента?", new DialogHandler() {
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

    @UiHandler("backAnchor")
    void onPrintButtonClicked(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onBackClicked();
        }
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

    @Override
    public void updateMode(FormMode mode) {
        switch (mode){
            case EDIT:
                addRow.setVisible(true);
                deleteRow.setVisible(true);
                cancelEdit.setVisible(true);
                edit.setVisible(false);
                // для красовы на форме
                separator.setVisible(true);
                break;
            case READ:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                cancelEdit.setVisible(false);
                edit.setVisible(false);
                // для красовы на форме
                separator.setVisible(false);
                break;
            case VIEW:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                cancelEdit.setVisible(false);
                edit.setVisible(true);
                // для красовы на форме
                separator.setVisible(false);
                break;
        }
    }

    @Override
    public void setVersionedFields(boolean isVisible) {
        relevanceDate.setVisible(isVisible);
        relevanceDateLabel.setVisible(isVisible);
    }

    @Override
    public String getSearchPattern() {
        return searchPattern;
    }

    @Override
    public void resetSearchInputBox() {
        filterText.setValue("");
        searchPattern = "";
    }
}
