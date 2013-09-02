package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormDataUiHandlers extends UiHandlers{
	
	void onReturnClicked();

	void onCancelClicked();

	void onSaveClicked();

	void onAddRowClicked();

	void onRemoveRowClicked();

	void onManualInputClicked(boolean readOnlyMode);

	void onInfoClicked();

	void onOriginalVersionClicked();

	void onRecalculateClicked();

	void onCheckClicked();

	void onPrintClicked();

	void onSignersClicked();

	void onDeleteFormClicked();

	void onWorkflowMove(WorkflowMove wfMove);

	void onSelectRow();

	void onShowCheckedColumns();

	void onRangeChange(final int start, int length);

	void onCellModified(DataRow<Cell> dataRow);

    void onUploadDataRow(String uuid);
}
