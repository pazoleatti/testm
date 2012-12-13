package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class FormDataPresenterBase<Proxy_ extends ProxyPlace<?>> extends
		Presenter<FormDataPresenterBase.MyView, Proxy_>{
	protected Logger logger = Logger.getLogger(getClass().getName());


	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenterBase}
	 * 's view.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataUiHandlers> {

		void setColumnsData(List<Column> columnsData, boolean readOnly);

		void setRowsData(List<DataRow> rowsData);

		void setLogMessages(List<LogEntry> logEntries);

		void setAdditionalFormInfo(String formType, String taxType,
				String formKind, String departmentId, String reportPeriod,
				String state);

		void setWorkflowButtons(List<WorkflowMove> moves);

		void showOriginalVersionButton(boolean show);

		void showSaveButton(boolean show);

		void showRecalculateButton(boolean show);

		void showAddRowButton(boolean show);

		void showRemoveRowButton(boolean show);

		void showPrintButton(boolean show);

		void showManualInputButton(boolean show);

		void showDeleteFormButton(boolean show);

		DataRow getSelectedRow();

		void enableRemoveRowButton(boolean enable);
	}

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";
	public static final String READ_ONLY = "readOnly";

	public static final String DEPARTMENT_ID = "departmentId";

	public static final String FORM_DATA_KIND_ID = "formDataKindId";

	public static final String FORM_DATA_TYPE_ID = "formDataTypeId";

	public static final String FORM_DATA_RPERIOD_ID = "formDataRPeriodId";

	public static final String WORK_FLOW_ID = "goWorkFlowId";

	protected final DispatchAsync dispatcher;
	protected final PlaceManager placeManager;

	protected FormData formData;
	protected FormDataAccessParams accessParams;
	protected boolean readOnlyMode;

	public FormDataPresenterBase(EventBus eventBus, MyView view, Proxy_ proxy,
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
				this);
	}
	
	
	protected void showReadOnlyModeButtons() {
		MyView view = getView();

		view.showSaveButton(false);
		view.showRemoveRowButton(false);
		view.showRecalculateButton(false);
		view.showAddRowButton(false);
		view.showOriginalVersionButton(false);

		view.showPrintButton(true);

		view.showManualInputButton(accessParams.isCanEdit());
		view.showDeleteFormButton(accessParams.isCanDelete());
	}

	protected void showEditModeButtons() {
		MyView view = getView();
		// сводная форма уровня Банка.
		if ((formData.getDepartmentId() == 1)
				&& (formData.getKind() == FormDataKind.SUMMARY)) {
			view.showOriginalVersionButton(true);
		} else {
			view.showOriginalVersionButton(false);
		}

		view.showSaveButton(true);
		view.showRecalculateButton(true);
		view.showAddRowButton(true);
		view.showRemoveRowButton(true);

		view.showPrintButton(false);
		view.showManualInputButton(false);
		view.showDeleteFormButton(false);
	}
	
	protected void revealForm(Boolean readOnly) {
		placeManager.revealPlace(new PlaceRequest(FormDataPresenterBase.NAME_TOKEN)
				.with(FormDataPresenterBase.READ_ONLY, readOnly.toString()).with(
						FormDataPresenterBase.FORM_DATA_ID,
						formData.getId().toString()));
	}

	protected void revealForm(Boolean readOnly, Integer wfMove) {
		placeManager.revealPlace(new PlaceRequest(FormDataPresenterBase.NAME_TOKEN)
				.with(FormDataPresenterBase.WORK_FLOW_ID, wfMove.toString())
				.with(FormDataPresenterBase.READ_ONLY, readOnly.toString()).with(
						FormDataPresenterBase.FORM_DATA_ID,
						formData.getId().toString()));
	}
}
