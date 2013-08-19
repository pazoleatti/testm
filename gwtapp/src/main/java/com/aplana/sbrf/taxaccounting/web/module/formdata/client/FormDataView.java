package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.cell.IndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.Bar;
import com.aplana.sbrf.taxaccounting.web.widget.style.LeftBar;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;
import java.util.List;

public class FormDataView extends ViewWithUiHandlers<FormDataUiHandlers>
		implements FormDataPresenterBase.MyView,FileUploadHandler {

	private NoSelectionModel<DataRow> selectionModel;

    @Override
    public void onFileUploadSuccess(String uuid) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFileParse(uuid);
        }
    }

    interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private DataRowColumnFactory factory = new DataRowColumnFactory();

	private MyDataProvider dataProvider = new MyDataProvider();

	@UiField
	DockLayoutPanel dockPanel;
	@UiField
	DataGrid<DataRow<Cell>> formDataTable;
	@UiField
	FlexiblePager pager;
	@UiField
	Button addRowButton;
	@UiField
	Button removeRowButton;
	@UiField
	Button originalVersionButton;
	@UiField
	Button recalculateButton;
	@UiField
	Button checkButton;
	@UiField
	Button deleteFormButton;

	@UiField
	Anchor printAnchor;
	@UiField
	Anchor signersAnchor;
	@UiField
	Anchor returnAnchor;
	@UiField
	Anchor manualInputAnchor;
	@UiField
	Anchor infoAnchor;
	@UiField
	Button cancelButton;
	@UiField
	Button saveButton;

	@UiField
	LeftBar workflowButtons;
	@UiField
	Bar saveCancelBar;

	@UiField
	Panel manualInputPanel;

	@UiField
	Label formKindLabel;
	@UiField
	Label lockInformation;
	@UiField
	Label departmentIdLabel;
	@UiField
	Label reportPeriodLabel;
	@UiField
	Label stateLabel;
	@UiField
	Label title;

	@UiField
	CheckBox showCheckedColumns;

    @UiField
    FileUploadWidget fileUploader;

    @Inject
	public FormDataView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));

		selectionModel = new NoSelectionModel<DataRow>();
		formDataTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				FormDataUiHandlers handlers = getUiHandlers();
				if (handlers != null) {
					handlers.onSelectRow();
				}
			}
		});
		formDataTable.addCellPreviewHandler(new CellPreviewEvent.Handler<DataRow<Cell>>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DataRow<Cell>> event) {
				if ("mouseover".equals(event.getNativeEvent().getType())) {
					long index = (event.getIndex() - (pager.getPage() * formDataTable.getPageSize()));
					TableCellElement cellElement = formDataTable.getRowElement((int) index).getCells().getItem(event.getColumn());
					if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
						cellElement.removeAttribute("title");
					} else {
						cellElement.setTitle(cellElement.getInnerText());
					}
				}
			}
		});
		pager.setDisplay(formDataTable);
	}

	@Override
	public void setColumnsData(List<Column> columnsData, boolean readOnly, boolean forceEditMode) {

		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}
		//Create order column
		NumericColumn numericColumn = new NumericColumn();
		DataRowColumn indexColumn = new DataRowColumn(new IndexCell(), numericColumn) {
			@Override
			public Object getValue(Object object) {
				return object;
			}
		};
		indexColumn.setCellStyleNames("order");
		formDataTable.addColumn(indexColumn, "№");
		formDataTable.setColumnWidth(indexColumn, "3em");

		factory.setReadOnly(readOnly);
		factory.setEditOnly(forceEditMode);
		boolean hideCheckedColumnsCheckbox = true;
		for (Column col : columnsData) {
			if (col.isChecking()) {
				hideCheckedColumnsCheckbox = false;
			}

			if (showCheckedColumns.getValue() || !col.isChecking()) {
				com.google.gwt.user.cellview.client.Column<DataRow<Cell>, ?> tableCol = factory
						.createTableColumn(col, formDataTable);
				formDataTable.addColumn(tableCol, col.getName());
				((DataRowColumn)tableCol).addCellModifiedEventHandler(new CellModifiedEventHandler() {
					@Override
					public void onCellModified(CellModifiedEvent event) {
						if(getUiHandlers()!=null){
							getUiHandlers().onCellModified(event.getDataRow());
						}
					}
				});
				if (col.getWidth() >= 0) {
					formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
				}
			}
		}
		showCheckedColumns.setVisible(!hideCheckedColumnsCheckbox);

	}

	@Override
	public void setRowsData(int start, int totalCount, List<DataRow<Cell>> rowsData) {
		formDataTable.setRowCount(totalCount);
		formDataTable.setRowData(start, rowsData);
	}

	@Override
	public void addCustomHeader(List<DataRow<HeaderCell>> headers) {
		CustomHeaderBuilder builder = new CustomHeaderBuilder(formDataTable, false, 1, headers);
		formDataTable.setHeaderBuilder(builder);
	}

	// После вызова этого метода таблица получает возможность объединять ячейки и применять стили
	@Override
	public void addCustomTableStyles(List<FormStyle> allStyles) {
		CustomTableBuilder<DataRow<Cell>> builder = new CustomTableBuilder<DataRow<Cell>>(formDataTable, false);
		formDataTable.setTableBuilder(builder);
	}


	@UiHandler("cancelButton")
	void onCancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
	}

	@UiHandler("saveButton")
	void onSaveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSaveClicked();
		}
	}

	@UiHandler("addRowButton")
	void onAddRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
	}

	@UiHandler("removeRowButton")
	void onRemoveRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onRemoveRowClicked();
		}
	}

	@Override
	public DataRow getSelectedRow() {
		return selectionModel.getLastSelectedObject();
	}

	@Override
	public void setSelectedRow(DataRow<Cell> item, boolean selected) {
		selectionModel.setSelected(item, selected);
	}

	@UiHandler("manualInputAnchor")
	void onManualInputButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onManualInputClicked(false);
		}
	}

	@UiHandler("infoAnchor")
	void onInfoButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onInfoClicked();
		}
	}

	@UiHandler("originalVersionButton")
	void onOriginalVersionButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onOriginalVersionClicked();
		}
	}

	@UiHandler("recalculateButton")
	void onRecalculateButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onRecalculateClicked();
		}
	}

	@UiHandler("checkButton")
	void onCheckButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCheckClicked();
		}
	}

	@UiHandler("printAnchor")
	void onPrintAnchorClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onPrintClicked();
		}
	}

	@UiHandler("signersAnchor")
	void onSignersAnchorClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSignersClicked();
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

	@UiHandler("deleteFormButton")
	void onDeleteFormButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onDeleteFormClicked();
		}
	}

	@UiHandler("showCheckedColumns")
	void onShowCheckedColumnsClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onShowCheckedColumns();
		}
	}

	@Override
	public void setAdditionalFormInfo(
			String formType, TaxType taxType,
			String formKind, String departmentId, String reportPeriod,
			String state, Date startDate, Date endDate) {
		String taxFormType = taxType.getName() + " / " + formType;
		title.setText(taxFormType);
		title.setTitle(taxFormType);
		formKindLabel.setText(formKind);
		departmentIdLabel.setText(departmentId);
		reportPeriodLabel.setText(reportPeriod);
		stateLabel.setText(state);
		factory.setDateRange(startDate, endDate);
	}

	/**
	 * Показывает кнопки для доступных переходов, если null, то скрываем все
	 * кнопки.
	 */
	@Override
	public void setWorkflowButtons(List<WorkflowMove> moves) {		
		boolean show = false;
		workflowButtons.clear();

		if (moves != null && !moves.isEmpty()) {
			for (final WorkflowMove workflowMove : moves) {
				Button button = new Button(workflowMove.getName(),
						new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								getUiHandlers().onWorkflowMove(workflowMove);
							}
						});
				workflowButtons.add(button);
			}
			show = true;
		}

		workflowButtons.setVisible(show);
	}

	@Override
	public void setBackButton(String link) {
		returnAnchor.setHref(link);
	}

	@Override
	public void showOriginalVersionButton(boolean show) {
		// http://jira.aplana.com/browse/SBRFACCTAX-2242 В режиме редактирования доступна кнопка "Исходная версия",
		// при нажатии на которую диалоговое окно "в разработке". Кнопку в версии 0.2.1 необходимо скрыть.
		originalVersionButton.setVisible(false);
	}

	@Override
	public void showSaveCancelBar(boolean show) {
		saveCancelBar.setVisible(show);
	}

	@Override
	public void showRecalculateButton(boolean show) {
		recalculateButton.setVisible(show);
	}

	@Override
	public void showCheckButton(boolean show) {
		checkButton.setVisible(show);
	}

	@Override
	public void showAddRowButton(boolean show) {
		addRowButton.setVisible(show);
	}

	@Override
	public void showRemoveRowButton(boolean show) {
		removeRowButton.setVisible(show);
	}

	@Override
	public void enableRemoveRowButton(boolean enable) {
		removeRowButton.setEnabled(enable);
	}

	@Override
	public void showPrintAnchor(boolean show) {
		printAnchor.setVisible(show);
	}

	@Override
	public void showManualInputAnchor(boolean show) {
		manualInputPanel.setVisible(show);
	}

	@Override
	public void showDeleteFormButton(boolean show) {
		deleteFormButton.setVisible(show);
	}

	@Override
	public void setLockInformation(boolean isVisible, String lockDate, String lockedBy){
		dockPanel.setWidgetHidden(lockInformation, !isVisible);
		lockInformation.setVisible(isVisible);
		if(lockedBy != null && lockDate != null){
			lockInformation.setText("Выбранная налоговая форма в текущий момент редактируется другим пользователем \"" + lockedBy
					+ "\" (с "+ lockDate + " )");
		}
	}

	@Override
	public boolean getCheckedColumnsClicked() {
		return showCheckedColumns.getValue();
	}

	@Override
	public void assignDataProvider(int pageSize) {
		formDataTable.setPageSize(pageSize);
		if(!dataProvider.getDataDisplays().contains(formDataTable)) {
			dataProvider.addDataDisplay(formDataTable);
		}
	}

	@Override
	public void updateData() {
		dataProvider.update();
	}

	private class MyDataProvider extends AsyncDataProvider<DataRow<Cell>> {

		public void update() {
			for (HasData<DataRow<Cell>> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		protected void onRangeChanged(HasData<DataRow<Cell>> display) {
			final Range range = display.getVisibleRange();
			getUiHandlers().onRangeChange(range.getStart(), range.getLength());
		}
	}

    @UiFactory
    FormDataView getView(){
        return this;
    }

}
