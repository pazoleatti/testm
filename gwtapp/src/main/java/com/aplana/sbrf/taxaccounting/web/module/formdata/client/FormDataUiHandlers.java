package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormDataUiHandlers extends UiHandlers{
	
	public void onCancelClicked();
	
	public void onSaveClicked();

	public void onAddRowClicked();

	public void onRemoveRowClicked();

	public void onManualInputClicked();

	public void onOriginalVersionClicked();
	
	public void onRecalculateClicked();

	public void onCheckClicked();
	
	public void onPrintClicked();

	public void onSignersClicked();
	
	public void onDeleteFormClicked();
	
	public void onWorkflowMove(WorkflowMove wfMove);

	public void onSelectRow();

	public void onShowCheckedColumns();
}
