package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.style.Bar;
import com.aplana.sbrf.taxaccounting.web.widget.style.LeftBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FormDataView extends ViewWithUiHandlers<FormDataUiHandlers>
		implements FormDataPresenterBase.MyView {

	private SingleSelectionModel<DataRow> selectionModel;

	interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private DataRowColumnFactory factory = new DataRowColumnFactory();

	@UiField
	DockLayoutPanel dockPanel;

	@UiField
	Bar workflowBar;

	@UiField
	DataGrid<DataRow> formDataTable;
	@UiField
	Button saveButton;
	@UiField
	Button addRowButton;
	@UiField
	Button removeRowButton;
	@UiField
	Button manualInputButton;
	@UiField
	Button originalVersionButton;
	@UiField
	Button recalculateButton;
	@UiField
	Button checkButton;
	@UiField
	Button printButton;
	@UiField
	Button signersButton;
	@UiField
	Button deleteFormButton;

	@UiField
	LeftBar workflowButtons;

	@UiField
	Label formTypeLabel;
	@UiField
	Label taxTypeLabel;
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
	CheckBox showCheckedColumns;

	private final Widget widget;

	@Inject
	public FormDataView(final Binder binder) {
		widget = binder.createAndBindUi(this);

		selectionModel = new SingleSelectionModel<DataRow>();
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
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setColumnsData(List<Column> columnsData, boolean readOnly) {

		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}

		factory.setReadOnly(readOnly);

		for (Column col : columnsData) {
			if (showCheckedColumns.getValue() || !col.isChecking()) {
				com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory
						.createTableColumn(col, formDataTable);
				formDataTable.addColumn(tableCol, col.getName());
				formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
			}
		}

	}

	@Override
	public void setRowsData(List<DataRow> rowsData) {
		if (rowsData != null) {
			formDataTable.setRowCount(rowsData.size());
			formDataTable.setRowData(rowsData);
		} else {
			formDataTable.setRowCount(0);
			formDataTable.setRowData(new ArrayList<DataRow>(0));
		}
		formDataTable.redraw();
	}

	@Override
	public void addCustomHeader(boolean addNumberedHeader) {
		CustomHeaderBuilder builder = new CustomHeaderBuilder(formDataTable, false, addNumberedHeader, false);
		formDataTable.setHeaderBuilder(builder);
	}

	// После вызова этого метода таблица получает возможность объединять ячейки и применять стили
	@Override
	public void addCustomTableStyles(List<FormStyle> allStyles) {
		CustomTableBuilder<DataRow> builder = new CustomTableBuilder<DataRow>(formDataTable, allStyles, false);
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
		return selectionModel.getSelectedObject();
	}

	@UiHandler("manualInputButton")
	void onManualInputButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onManualInputClicked();
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

	@UiHandler("printButton")
	void onPrintButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onPrintClicked();
		}
	}

	@UiHandler("signersButton")
	void onSignersButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSignersClicked();
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
			String formType, String taxType,
			String formKind, String departmentId, String reportPeriod,
			String state
	) {
		taxTypeLabel.setText("Тип налога: " + taxType);
		formTypeLabel.setText(" Вид налоговой формы: " + formType);
		formKindLabel.setText("Тип налоговой формы: " + formKind);
		departmentIdLabel.setText("Подразделение: " + departmentId);
		reportPeriodLabel.setText("Отчётный период: " + reportPeriod);
		stateLabel.setText("Состояние: " + state);
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
		dockPanel.setWidgetHidden(workflowBar, !show);
		workflowButtons.setVisible(show);
	}

	@Override
	public void showOriginalVersionButton(boolean show) {
		originalVersionButton.setVisible(show);
	}

	@Override
	public void showSaveButton(boolean show) {
		saveButton.setVisible(show);
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
	public void showPrintButton(boolean show) {
		printButton.setVisible(show);
	}

	@Override
	public void showManualInputButton(boolean show) {
		manualInputButton.setVisible(show);
	}

	@Override
	public void showDeleteFormButton(boolean show) {
		deleteFormButton.setVisible(show);
	}

	@Override
	public void setLockInformation(boolean isVisible, String lockDate, String lockedBy){
		lockInformation.setVisible(isVisible);
		if(lockedBy != null && lockDate != null){
			lockInformation.setText("Данная налоговая форма в настоящий момент редактируется пользователем \"" + lockedBy
					+ "\" (с "+ lockDate + " )");
		}
	}

	@Override
	public boolean getCheckedColumnsClicked() {
		return showCheckedColumns.getValue();
	}

}
