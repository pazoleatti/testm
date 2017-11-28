package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetCheсksAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetCheсksResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.*;

public class DeclarationTemplateChecksPresenter
		extends Presenter<DeclarationTemplateChecksPresenter.MyView, DeclarationTemplateChecksPresenter.MyProxy>
		implements DeclarationTemplateChecksUiHandlers, UpdateTemplateEvent.MyHandler, DeclarationTemplateFlushEvent.MyHandler {

    @Title("Шаблоны")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateChecks)
	@TabInfo(container = DeclarationTemplateMainPresenter.class, label = DeclarationTemplateTokens.declarationTemplateChecksLabel, priority = DeclarationTemplateTokens.declarationTemplateChecksPriority)
	public interface MyProxy extends
			TabContentProxyPlace<DeclarationTemplateChecksPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<DeclarationTemplateChecksUiHandlers> {
        void setTableData(List<DeclarationTemplateCheck> checks);
        Set<DeclarationTemplateCheck> getFatalChecks();
	}

    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;
    private final DispatchAsync dispatcher;
    private final TaPlaceManager placeManager;

    private List<DeclarationTemplateCheck> dataRows = new ArrayList<DeclarationTemplateCheck>();

	@Inject
	public DeclarationTemplateChecksPresenter(final EventBus eventBus,
                                              final MyView view, final MyProxy proxy,
                                              DeclarationTemplateMainPresenter declarationTemplateMainPresenter,
                                              DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view, proxy, DeclarationTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
        this.declarationTemplateMainPresenter = declarationTemplateMainPresenter;
        this.dispatcher = dispatcher;
        this.placeManager = (TaPlaceManager) placeManager;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
        addRegisteredHandler(UpdateTemplateEvent.getType(), this);
        addRegisteredHandler(DeclarationTemplateFlushEvent.getType(), this);
    }

    @ProxyEvent
    @Override
    public void onUpdateTemplate(UpdateTemplateEvent event) {
        loadGridData();
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {
        if (declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate() != null) {
            declarationTemplateMainPresenter.getDeclarationTemplateExt().setChecks(getChecks());
        }
    }

    private void loadGridData() {
        GetCheсksAction action = new GetCheсksAction();
        action.setDeclarationTypeId(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getType().getId());
        action.setDeclarationTemplateId(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getId());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetCheсksResult>() {
                            @Override
                            public void onSuccess(GetCheсksResult result) {
                                LogCleanEvent.fire(DeclarationTemplateChecksPresenter.this);
                                getView().setTableData(result.getChecks());
                                dataRows = result.getChecks();
                            }
                        }, this).addCallback(TaManualRevealCallback.create(this, placeManager)));
    }

    @Override
    public List<DeclarationTemplateCheck> getChecks() {
        Set<DeclarationTemplateCheck> fatalChecks = getView().getFatalChecks();
        for (DeclarationTemplateCheck check : dataRows) {
            if (fatalChecks.contains(check)) {
                check.setFatal(true);
            }
        }
        return dataRows;
    }
}
