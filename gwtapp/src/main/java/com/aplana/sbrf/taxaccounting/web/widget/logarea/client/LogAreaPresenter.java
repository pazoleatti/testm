package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.widget.log.LogEntriesView;
import com.aplana.sbrf.taxaccounting.web.widget.log.LogEntriesWidget;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.GetLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.GetLogEntriesResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.Map;

public class LogAreaPresenter extends
        PresenterWidget<LogAreaPresenter.MyView> implements
        LogAddEvent.MyHandler, LogCleanEvent.MyHandler, LogAreaUiHandlers {

    private boolean rangeChangeHandle = true;

    private AsyncDataProvider<LogEntry> dataProvider = new AsyncDataProvider<LogEntry>() {
        @Override
        protected void onRangeChanged(HasData<LogEntry> display) {
            if (!rangeChangeHandle) {
                return;
            }
            Range range = display.getVisibleRange();
            onRangeChange(range.getStart(), range.getLength());
        }
    };

    public static interface MyView extends View, HasUiHandlers<LogAreaUiHandlers> {
        LogEntriesView getLogEntriesView();

        void setLogEntriesCount(Map<LogLevel, Integer> map);

        void setPrintLink(String link);
    }

    protected final DispatchAsync dispatcher;

    private String uuid;

    @Inject
    public LogAreaPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        getView().getLogEntriesView().setDataProvider(dataProvider);
    }

    @Override
    protected void onBind() {
        super.onBind();
        addRegisteredHandler(LogAddEvent.getType(), this);
        addRegisteredHandler(LogCleanEvent.getType(), this);
    }

    @Override
    public void onLogUpdate(LogAddEvent event) {
        uuid = event.getUuid();
        if (uuid != null) {
            getView().setPrintLink(GWT.getHostPageBaseURL() + "download/logEntry/" + uuid);

        }
        onRangeChange(0, LogEntriesWidget.PAGE_SIZE);
    }

    @Override
    public void onLogClean(LogCleanEvent event) {
        clean();
    }

    @Override
    public void clean() {
        getView().setPrintLink(null);
        // Сброс состояния пагинатора не должен провоцировать попытку подгрузки данных
        rangeChangeHandle = false;
        getView().getLogEntriesView().clearLogEntries();
        rangeChangeHandle = true;
        getView().setLogEntriesCount(null);
    }

    @Override
    public void hide() {
        LogShowEvent.fire(this, false);
    }

    public void onRangeChange(final int start, int length) {
        if (uuid == null) {
            clean();
            return;
        }
        GetLogEntriesAction action = new GetLogEntriesAction();
        action.setUuid(uuid);
        action.setStart(start);
        action.setLength(length);

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetLogEntriesResult>() {
            @Override
            public void onSuccess(GetLogEntriesResult result) {
                PagingResult<LogEntry> logEntries = result.getLogEntries();
                if (logEntries.isEmpty()) {
                    clean();
                    return;
                }
                getView().getLogEntriesView().setLogEntries(start, logEntries.getTotalCount(), logEntries);
                getView().setLogEntriesCount(result.getLogEntriesCount());
                LogShowEvent.fire(LogAreaPresenter.this, true);
            }
        }, this));
    }
}
