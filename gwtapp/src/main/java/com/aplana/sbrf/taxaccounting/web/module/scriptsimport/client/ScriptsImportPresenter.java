package com.aplana.sbrf.taxaccounting.web.module.scriptsimport.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.shared.ScriptsImportAction;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.shared.ScriptsImportResult;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * @author Denis Loshkarev
 */
public class ScriptsImportPresenter extends Presenter<ScriptsImportPresenter.MyView, ScriptsImportPresenter.MyProxy>
        implements ScriptsImportUiHandlers {

    private final DispatchAsync dispatcher;

    @Inject
    public ScriptsImportPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    public void onStartLoad() {
        LockInteractionEvent.fire(this, true);
    }

    @ProxyStandard
    @NameToken(ScriptsImportTokens.SCRIPTS_IMPORT)
    public interface MyProxy extends ProxyPlace<ScriptsImportPresenter> {
    }

    public interface MyView extends View, HasUiHandlers<ScriptsImportUiHandlers> {
        void addImportHandler(ValueChangeHandler<String> handler);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        super.prepareFromRequest(request);
    }

    @Override
    protected void onBind() {
        super.onBind();
        ValueChangeHandler<String> importHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                LogCleanEvent.fire(ScriptsImportPresenter.this);
                ScriptsImportAction importAction = new ScriptsImportAction();
                importAction.setUuid(event.getValue());
                dispatcher.execute(importAction, new AbstractCallback<ScriptsImportResult>() {
                    @Override
                    public void onSuccess(ScriptsImportResult result) {
                        if (result.getUuid() != null) {
                            LogAddEvent.fire(ScriptsImportPresenter.this, result.getUuid());
                            onEndLoad();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        onEndLoad();
                    }
                });
            }
        };
        getView().addImportHandler(importHandler);
    }

    private void onEndLoad() {
        LockInteractionEvent.fire(this, false);
    }
}