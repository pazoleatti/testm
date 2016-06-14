package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.RefBookHistoryPresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Презентор редактирования скрипта справочника
 *
 * @author Fail Mukhametdinov
 */
public class RefBookScriptPresenter extends Presenter<RefBookScriptPresenter.MyView, RefBookScriptPresenter.MyProxy>
        implements RefBookScriptUiHandlers {

    private final DispatchAsync dispatchAsync;
    private final PlaceManager placeManager;

    private final RefBookHistoryPresenter versionHistoryPresenter;

    @Inject
    public RefBookScriptPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatchAsync, PlaceManager placeManager, RefBookHistoryPresenter refBookVersionPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatchAsync = dispatchAsync;
        this.placeManager = placeManager;
        this.versionHistoryPresenter = refBookVersionPresenter;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        getScript();
    }

    @Override
    public void saveScript(final boolean isRedirect) {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        SaveRefBookScriptAction action = new SaveRefBookScriptAction();
        action.setRefBookId(Long.valueOf(request.getParameter("id", null)));
        action.setScript(getView().getData().getScript());
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<SaveRefBookScriptResult>() {
                    @Override
                    public void onSuccess(SaveRefBookScriptResult result) {
                        getView().showSavedMessage(isRedirect);
                    }
                }, this));
    }

    @Override
    public void cancelEdit() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(RefBookListTokens.REFBOOK_LIST_ADMIN).build());
    }

    @Override
    public void getScript() {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        GetRefBookScriptAction action = new GetRefBookScriptAction();
        action.setRefBookId(Long.valueOf(request.getParameter("id", null)));
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookScriptResult>() {
                    @Override
                    public void onSuccess(GetRefBookScriptResult result) {
                        RefBookScript refBookScript = new RefBookScript();
                        refBookScript.setScript(result.getScript());
                        refBookScript.setPageTitle(result.getName());
                        getView().setData(refBookScript);
                    }
                }, this));
    }

    public interface MyView extends View, HasUiHandlers<RefBookScriptUiHandlers> {
        RefBookScript getData();

        void setData(RefBookScript refBookScript);

        void showSavedMessage(boolean isRedirect);
    }

    @ProxyStandard
    @NameToken(RefBookDataTokens.REFBOOK_SCRIPT)
    public interface MyProxy extends ProxyPlace<RefBookScriptPresenter> {
    }

    @Override
    public void onHistoryClicked() {
        Integer id = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(RefBookDataTokens.REFBOOK_DATA_ID, ""));
        if (id == 0)
            return;
        versionHistoryPresenter.init(id);
        addToPopupSlot(versionHistoryPresenter);
    }
}