package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
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

    void onTimerReport(ReportType reportType, String specificReportType, boolean isTimer);

    void onPrintClicked(ReportType reportType, String specificReportType, boolean force);

	void onSignersClicked();

	void onDeleteFormClicked();

	void onWorkflowMove(WorkflowMove wfMove);

	void onSelectRow();

	void onShowCheckedColumns();

	void onRangeChange(final int start, int length);

	void onCellModified(DataRow<Cell> dataRow);

    void onStartLoad();

    void onEndLoad();

    void onOpenSearchDialog();

    void onCreateManualClicked();

    void onDeleteManualClicked();

    void onOpenSourcesDialog();

    void onFilesCommentsDialog();

    void onCorrectionSwitch();

    void onConsolidate(boolean force, boolean cancelTask);
}
