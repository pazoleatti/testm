package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources;

import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * Презентер попап окна с инфомацией об источниках приемниках,
 * данное окно вызывается с формы нф
 *
 * @author auldanov
 */
public class SourcesPresenter extends PresenterWidget<SourcesPresenter.MyView> implements SourcesUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<SourcesUiHandlers> {
        void setTableData(List<Relation> tableData);
    }

    private final DispatchAsync dispatcher;
    private DeclarationDataPresenter declarationDataPresenter;
    private Long declarationId;

    @Inject
    public SourcesPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    private void reloadData() {
        getView().setTableData(null);
        SourcesAction action = new SourcesAction();
        action.setDeclarationId(declarationId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SourcesResult>() {
            @Override
            public void onSuccess(SourcesResult result) {
                if (!declarationDataPresenter.checkExistDeclarationData(result)) return;
                getView().setTableData(result.getData());
            }
        }, this));
    }

    public void setDeclarationId(long declarationId) {
        this.declarationId = declarationId;
        reloadData();
    }

    public void setDeclarationDataPresenter(DeclarationDataPresenter declarationDataPresenter) {
        this.declarationDataPresenter = declarationDataPresenter;
    }
}

