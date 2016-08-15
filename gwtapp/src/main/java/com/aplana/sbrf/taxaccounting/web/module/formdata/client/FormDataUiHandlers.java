package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormDataUiHandlers extends UiHandlers{
	
	void onReturnClicked();

	void onCancelClicked();

	void onSaveClicked();

    void onExitAndSaveClicked();

	void onAddRowClicked();

	void onRemoveRowClicked();

    void onFillPreviousButtonClicked();

    void onModeChangeClicked();

	void onEditClicked(boolean readOnlyMode, boolean force);

	void onInfoClicked();

	void onOriginalVersionClicked();

    void onRefreshClicked(boolean force, boolean cancelTask);

    void onRecalculateClicked(boolean force, boolean cancelTask);

	void onCheckClicked(boolean force);

    void onPrintClicked(FormDataReportType fdReportType, boolean force);

	void onSignersClicked();

	void onDeleteFormClicked();

	void onWorkflowMove(WorkflowMove wfMove);

	void onSelectRow();

	void onShowCheckedColumns();

	void onRangeChange(final int start, int length);

	void onCellModified(DataRow<Cell> dataRow);

    void onStartLoad();

    void onEndLoad();

    void onOpenSearchDialog(int sessionId);

    void onCreateManualClicked();

    void onDeleteManualClicked();

    void onOpenSourcesDialog();

    void onFilesCommentsDialog();

    void onCorrectionSwitch();

    void onConsolidate(boolean force, boolean cancelTask);
}
