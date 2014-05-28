package com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;

/**
 * Презентер для диалогового окна
 *
 * @author aivanov
 */
public class AssignDialogPresenter extends PresenterWidget<AssignDialogPresenter.MyView>
        implements AssignDialogUiHandlers {

    public interface MyView extends View, HasUiHandlers<AssignDialogUiHandlers> {

        void open(AssignDialogView.State state, PeriodsInterval periodsInterval, ButtonClickHandlers buttonClickHandlers);

        void setAcceptablePeriods(List<PeriodInfo> periods);

        void close();
    }

    private DispatchAsync dispatcher;
    private PlaceManager placeManager;

    @Inject
    public AssignDialogPresenter(final EventBus eventBus, final MyView view,
                                 DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    public void setAvailablePeriods(List<PeriodInfo> periods){
        getView().setAcceptablePeriods(periods);
    }

    public void open(AssignDialogView.State state, PeriodsInterval pi, ButtonClickHandlers handlers) {
        getView().open(state, pi, handlers);
    }

    public void close() {
        getView().close();
    }

}
