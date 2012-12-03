package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.util.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.cell.LogEntryCell;
import com.aplana.sbrf.taxaccounting.web.widget.style.LeftBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FormDataView extends ViewWithUiHandlers<FormDataUiHandlers>
		implements FormDataPresenterBase.MyView {

	interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private DataRowColumnFactory factory = new DataRowColumnFactory();

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
	Button printButton;
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
	Label departmentIdLabel;
	@UiField
	Label reportPeriodLabel;
	@UiField
	Label stateLabel;

	@UiField(provided = true)
	CellList<LogEntry> loggerList = new CellList<LogEntry>(new LogEntryCell());

	private final Widget widget;

	@Inject
	public FormDataView(final Binder binder) {
		widget = binder.createAndBindUi(this);
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
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory
					.createTableColumn(col, formDataTable);
			formDataTable.addColumn(tableCol, col.getName());
			formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
			final SingleSelectionModel<DataRow> selectionModel = new SingleSelectionModel<DataRow>();
			formDataTable.setSelectionModel(selectionModel);
		}

	}

	@Override
	public void setRowsData(List<DataRow> rowsData) {
		formDataTable.setRowCount(rowsData.size());
		formDataTable.setRowData(rowsData);
		formDataTable.redraw();
	}

	@Override
	public void setLogMessages(List<LogEntry> logEntries) {
		loggerList.setRowCount(logEntries.size());
		loggerList.setRowData(logEntries);
		loggerList.redraw();
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

	@SuppressWarnings("unchecked")
	@UiHandler("removeRowButton")
	void onRemoveRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onRemoveRowClicked(
					((SingleSelectionModel<DataRow>) formDataTable
							.getSelectionModel()).getSelectedObject());
		}
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

	@UiHandler("printButton")
	void onPrintButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onPrintClicked();
		}
	}

	@UiHandler("deleteFormButton")
	void onDeleteFormButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onDeleteFormClicked();
		}
	}

	@Override
	public void setAdditionalFormInfo(String formType, String taxType,
			String formKind, String departmentId, String reportPeriod,
			String state) {
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
		workflowButtons.clear();
		if (moves != null) {
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
		}
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
	public void showAddRowButton(boolean show) {
		addRowButton.setVisible(show);
	}

	@Override
	public void showRemoveRowButton(boolean show) {
		removeRowButton.setVisible(show);
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

}
