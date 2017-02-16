package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

    private static final int MAX_LENGTH = 255;
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private long declarationId;

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
        hide();
        LogCleanEvent.fire(this);
        AcceptDeclarationDataAction action = new AcceptDeclarationDataAction();
        action.setAccepted(false);
        action.setDeclarationId(declarationId);
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<AcceptDeclarationDataResult>() {
						@Override
						public void onSuccess(AcceptDeclarationDataResult result) {
							revealPlaceRequest();
						}
					}, this));
	}

	@Override
	public void hide() {
		getView().hide();
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
	}

	private void revealPlaceRequest() {
		placeManager.revealPlace(new PlaceRequest(DeclarationDataTokens.declarationData)
				.with(DeclarationDataTokens.declarationId, String.valueOf(declarationId)));
	}

}
