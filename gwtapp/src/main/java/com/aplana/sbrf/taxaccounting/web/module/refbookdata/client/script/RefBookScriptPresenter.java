package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookScriptResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookScriptResult;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
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

    @Inject
    public RefBookScriptPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatchAsync = dispatchAsync;
        this.placeManager = placeManager;
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
        action.setScript(getView().getScriptCode());
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
                        getView().setScriptCode(result.getScript());
                        getView().setPageTitle(result.getName());
                    }
                }, this));
    }

    public interface MyView extends View, HasUiHandlers<RefBookScriptUiHandlers> {
        String getScriptCode();

        void setScriptCode(String script);

        void setPageTitle(String title);

        void showSavedMessage(boolean isRedirect);
    }

    @ProxyStandard
    @NameToken(RefBookDataTokens.REFBOOK_SCRIPT)
    public interface MyProxy extends ProxyPlace<RefBookScriptPresenter> {
    }
}