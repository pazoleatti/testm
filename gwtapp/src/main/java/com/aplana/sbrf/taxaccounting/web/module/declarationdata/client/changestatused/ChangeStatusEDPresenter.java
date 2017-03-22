package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class ChangeStatusEDPresenter extends PresenterWidget<ChangeStatusEDPresenter.MyView> implements ChangeStatusEDUiHandlers {

    private ChangeStatusHandler handler;

	public interface MyView extends PopupView, HasUiHandlers<ChangeStatusEDUiHandlers> {
        Long getDocStateId();
        void setDocStateId(Long docStateId);
	}

    public interface ChangeStatusHandler {
        void setDocState(Long docStateId);
    }

    @Inject
	public ChangeStatusEDPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
	}

	@Override
	public void onConfirm() {
        handler.setDocState(getView().getDocStateId());
	}

	public void hide() {
		getView().hide();
	}

    public void init(Long docStateId, ChangeStatusHandler changeStatusHandler) {
        this.handler = changeStatusHandler;
        getView().setDocStateId(docStateId);
    }

    interface Binder extends UiBinder<PopupPanel, ChangeStatusEDView> {
    }
}
