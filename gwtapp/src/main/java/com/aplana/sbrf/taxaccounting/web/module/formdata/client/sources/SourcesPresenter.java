package com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SourcesResult;
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

        void initCheckboxes();
        void setTableData(int start, List<FormToFormRelation> result, int size);
        void updateTableData();

        boolean getShowDestinations();

        boolean getShowSources();

        boolean getShowUncreated();
    }

    private final DispatchAsync dispatcher;
    private FormData formData;

    @Inject
    public SourcesPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void open() {
        getView().initCheckboxes();
        getView().updateTableData();
    }

    @Override
    public void onRangeChange(final int start) {
        SourcesAction action = new SourcesAction();
        action.setFormData(formData);
        action.setShowDestinations(getView().getShowDestinations());
        action.setShowSources(getView().getShowSources());
        action.setShowUncreated(getView().getShowUncreated());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SourcesResult>() {
            @Override
            public void onSuccess(SourcesResult result) {
                getView().setTableData(start, result.getData(), result.getData().size());
            }
        }, this));
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}

