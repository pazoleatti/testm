package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ScreenLockEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Map;

public class TestPagePresenter extends Presenter<TestPagePresenter.MyView,
        TestPagePresenter.MyProxy> implements TestPageUiHandlers {
    @ProxyCodeSplit
    @NameToken(TestPageTokens.TEST_PAGE)
    public interface MyProxy extends ProxyPlace<TestPagePresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TestPageUiHandlers> {
        void setIds(int fpicker, int hpicker);
        Long getDepId();
        Boolean getDepUsageValue();
        String getSelectedEvent();
        void setEvents(Map<Integer, String> map);
    }

    private final DispatchAsync dispatcher;

    @Inject
    public TestPagePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                             DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    private int fpicker;
    private int hpicker;

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);

        fpicker = Integer.valueOf(request.getParameter("f", "-1"));
        hpicker = Integer.valueOf(request.getParameter("h", "-1"));
    }

    @Override
    public void openMessageDialog() {
        ScreenLockEvent.fire(this, true);
        TaActionException ffffffff = new TaActionException("ffffffff");
        ffffffff.setNeedStackTrace(true);
        ffffffff.setTrace(",efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf, " +
                "\n efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf," +
                "\n efufufufufufufdsfsdfsdfsdf," +
                "\n efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf,efufufufufufufdsfsdfsdfsdf," +
                "\n efufufufufufufdsfsdfsdfsdf");
        MessageEvent.fire(this, true, "Ошибочная ошибка ошибки в ошибочной ошибке ошибочно ошибается за ошибки ошибки ололололололо ", ffffffff);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().setIds(fpicker, hpicker);
    }

    @Override
    public void updateIdsFromPath() {
        getView().setIds(fpicker, hpicker);
    }

    @Override
    public void setUsageDepartment() {
        if (getView().getDepId() == null) {
            Dialog.errorMessage("Не выбрано подразделение.");
        } else {
        }
    }

    @Override
    public void doEvent() {
        if (getView().getSelectedEvent() == null) {
            Dialog.errorMessage("Не выбрано событие.");
        } else {
        }
    }
}
