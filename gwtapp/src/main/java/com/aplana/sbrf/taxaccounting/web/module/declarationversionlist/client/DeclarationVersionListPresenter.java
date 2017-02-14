package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client.event.CreateNewDTVersionEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.DeclarationHistoryPresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

/**
 * Список версий макетов деклараций
 * User: avanteev
 */
public class DeclarationVersionListPresenter extends Presenter<DeclarationVersionListPresenter.MyView, DeclarationVersionListPresenter.MyProxy>
        implements DTVersionListUIHandlers {

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;

    protected DeclarationHistoryPresenter versionHistoryPresenter;

    @Inject
    public DeclarationVersionListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager,
                                           DeclarationHistoryPresenter versionHistoryPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.placeManager = placeManager;
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        this.versionHistoryPresenter = versionHistoryPresenter;
    }

    @Override
    public void onReturnClicked() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateList).build());
    }

    @Override
    public void onCreateVersion() {
        CreateNewDTVersionEvent.fire(this,
                Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationType, "")));
    }

    @Override
    public void onDeleteVersion() {
        DeleteDTVersionAction action = new DeleteDTVersionAction();
        action.setDeclarationTemplateId(Integer.valueOf(getView().getSelectedElement().getDtId()));
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteDTVersionResult>() {
            @Override
            public void onSuccess(DeleteDTVersionResult result) {
                if (result.isLastVersion()){
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateList).build());
                    UpdateTableEvent.fire(DeclarationVersionListPresenter.this, result.getLogEntryUuid());
                    return;
                }
                String typeId = placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationType, "");
                LogAddEvent.fire(DeclarationVersionListPresenter.this, result.getLogEntryUuid());
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationVersionList)
                        .with(DeclarationTemplateTokens.declarationType, typeId).build());
                getView().resetSelectedLine();
            }
        }, this).addCallback(new ManualRevealCallback<DeleteDTVersionResult>(DeclarationVersionListPresenter.this)));
    }

    @Override
    public void onHistoryClick() {
        Integer id = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationType, ""));
        versionHistoryPresenter.init(id);
        addToPopupSlot(versionHistoryPresenter);
    }


    interface MyView extends View, HasUiHandlers<DTVersionListUIHandlers> {
        void setDTVersionTable(List<DeclarationTemplateVersion> fullList);
        DeclarationTemplateVersion getSelectedElement();
        void setLabelName(String labelName);
        void resetSelectedLine();
        void setKindLabel(String kindLabel);
    }

    @Title("Версии макетов налоговых форм")
    @ProxyCodeSplit
    @NameToken(DeclarationTemplateTokens.declarationVersionList)
    public interface MyProxy extends ProxyPlace<DeclarationVersionListPresenter> {
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        GetDTVersionListAction action = new GetDTVersionListAction();
        action.setDeclarationFormTypeId(Integer.parseInt(request.getParameter(DeclarationTemplateTokens.declarationType, "-1")));
        dispatcher.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<GetDTVersionListResult>() {
                    @Override
                    public void onSuccess(GetDTVersionListResult result) {
                        getView().setDTVersionTable(result.getTemplateVersions());
                        getView().setLabelName(result.getDtTypeName());
                        getView().setKindLabel(result.getTaxType().getName());
                    }
                }, this)
        );
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().resetSelectedLine();
    }
}
