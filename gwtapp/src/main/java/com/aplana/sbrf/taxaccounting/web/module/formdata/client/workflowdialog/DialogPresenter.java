package com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenterBase;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

	private static final String READ_ONLY = "true";
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private FormData formData;
	private WorkflowMove workflowMove;

	public interface MyView extends PopupView, HasUiHandlers<DialogUiHandlers> {
		void clearInput();
		String getComment();
	}

	@Inject
	public DialogPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
		super(eventBus, view);
		this.placeManager = placeManager;
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
	    getView().clearInput();
	}

	@Override
	public void onConfirm() {
		String reasonForReturn = getView().getComment();
		if("".equals(reasonForReturn.trim())){
			Window.alert("Необходимо указать причину возврата");
		} else {
			getView().hide();
			GoMoveAction action = new GoMoveAction();
			action.setFormDataId(formData.getId());
			action.setMove(workflowMove);
			if (reasonForReturn.length() > 255) {
				reasonForReturn = reasonForReturn.substring(0, 255);
			}
			action.setReasonToWorkflowMove(reasonForReturn);
			dispatchAsync.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<GoMoveResult>() {
						@Override
						public void onSuccess(GoMoveResult result) {
							placeManager.revealPlace(new PlaceRequest.Builder().nameToken(FormDataPresenterBase.NAME_TOKEN)
									.with(FormDataPresenterBase.READ_ONLY, READ_ONLY).with(
											FormDataPresenterBase.FORM_DATA_ID,
											String.valueOf(formData.getId())).build());
						}
					}, this));
		}
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}

	public void setWorkFlow(WorkflowMove workflowMove) {
		this.workflowMove = workflowMove;
	}

}
