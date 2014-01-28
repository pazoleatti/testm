package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client.event.CreateNewVersionEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.VersionHistoryPresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class TemplateVersionListPresenter extends Presenter<TemplateVersionListPresenter.MyView, TemplateVersionListPresenter.MyProxy> implements FTVersionListUiHandlers {

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;
    protected VersionHistoryPresenter versionHistoryPresenter;

    @Inject
    public TemplateVersionListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                        DispatchAsync dispatcher, PlaceManager placeManager,
                                        VersionHistoryPresenter versionHistoryPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
        this.versionHistoryPresenter = versionHistoryPresenter;
    }

    @Override
    public void onCreateVersion() {
        /*placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateInfoPage).
                with(AdminConstants.NameTokens.formTemplateId, "0").
                with(AdminConstants.NameTokens.formTypeId, placeManager.getCurrentPlaceRequest().getParameter(AdminConstants.NameTokens.formTypeId, "")).build());*/
        CreateNewVersionEvent.fire(this,
                Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminConstants.NameTokens.formTypeId, "")));
    }

    @Override
    public void onDeleteVersion() {
        FormTemplateVersion selectedVersion = getView().getSelectedElement();
        DeleteVersionAction action = new DeleteVersionAction();
        action.setFormTemplateId(Integer.valueOf(selectedVersion.getFormTemplateId()));
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteVersionResult>() {
            @Override
            public void onSuccess(DeleteVersionResult result) {
                LogAddEvent.fire(TemplateVersionListPresenter.this, result.getUuid());
            }
        }, this).addCallback(TaManualRevealCallback.create(this, (TaPlaceManager)placeManager)));

    }

    @Override
    public void onHistoryClick() {
        GetFTVersionHistoryAction action = new GetFTVersionHistoryAction();
        action.setFormTypeId(Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminConstants.NameTokens.formTypeId, "")));
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetFTVersionHistoryResult>() {
            @Override
            public void onSuccess(GetFTVersionHistoryResult result) {
                versionHistoryPresenter.initHistory(result.getChangeses());
                addToPopupSlot(versionHistoryPresenter);
            }
        }, this));
    }

    @Override
    public void onReturnClicked() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.adminPage).build());
    }

    @Title("Версии макетов НФ")
    @ProxyCodeSplit
    @NameToken(AdminConstants.NameTokens.formTemplateVersionList)
    public interface MyProxy extends ProxyPlace<TemplateVersionListPresenter> {
    }

    public interface MyView extends View, HasUiHandlers<FTVersionListUiHandlers> {
        void setFTVersionTable(List<FormTemplateVersion> userFullList);
        FormTemplateVersion getSelectedElement();
        void setLabelName(String labelName);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        GetFTVersionListAction action = new GetFTVersionListAction();
        action.setFormTypeId(Integer.parseInt(request.getParameter("formTypeId", "")));
        dispatcher.execute(action, CallbackUtils.defaultCallback(
        new AbstractCallback<GetFTVersionListResult>() {
                    @Override
                    public void onSuccess(GetFTVersionListResult result) {
                        getView().setFTVersionTable(result.getFormTemplateVersions());
                        getView().setLabelName(result.getFormTypeName());
                    }
                }, this)
        );
    }
}
