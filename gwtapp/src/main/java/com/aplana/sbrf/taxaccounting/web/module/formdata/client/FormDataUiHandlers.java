package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FormDataUiHandlers extends UiHandlers{
	
	public void onCancelClicked();
	
	public void onSaveClicked();

	public void onAddRowClicked();

	public void onRemoveRowClicked();

	public void onManualInputClicked();

	public void onOriginalVersionClicked();
	
	public void onRecalculateClicked();
	
	public void onPrintClicked();
	
	public void onDeleteFormClicked();
}
