package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import java.util.ArrayList;
import java.util.List;



import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.LogErrorAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.LogErrorResult;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class LogAreaPresenter extends
		PresenterWidget<LogAreaPresenter.MyView> implements
		LogAddEvent.MyHandler, LogCleanEvent.MyHandler, LogAreaUiHandlers {


	public static interface MyView extends View, HasUiHandlers<LogAreaUiHandlers>{

		void setLogEntries(List<LogEntry> entries);
		void setLogSize(int full, int error, int warn, int info);
        void getReport(String uuid);
	}

	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

    protected final DispatchAsync dispatcher;

	@Inject
	public LogAreaPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
		super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(LogAddEvent.getType(), this);
		addRegisteredHandler(LogCleanEvent.getType(), this);
	}

	@Override
	public void onLogAdd(LogAddEvent event) {
		if (event.getLogEntries()!=null){
			logEntries.addAll(event.getLogEntries());
		}
		updateView();

		if (!logEntries.isEmpty()){
			LogShowEvent.fire(this, true);
		}
	}

	@Override
	public void onLogClean(LogCleanEvent event) {
		logEntries.clear();
		updateView();
	}

	private void updateView(){
		getView().setLogEntries(logEntries);
		int error = 0, warn = 0, info = 0;
		for (LogEntry logEntry : logEntries) {
			switch (logEntry.getLevel()) {
			case ERROR:
				error++;
				break;
			case WARNING:
				warn++;
				break;
			case INFO:
				info++;
			}
		}
		getView().setLogSize(error + warn, error, warn, info);
	}

	@Override
	public void print() {
        LogErrorAction action = new LogErrorAction();
        action.setLogEntries(logEntries);
        dispatcher.execute(action, new AbstractCallback<LogErrorResult>() {
            @Override
            public void onSuccess(LogErrorResult result) {
                getView().getReport(result.getUuid());
            }
        });
		/*JSONObject requestJSON = new JSONObject();
		JSONArray jArr = new JSONArray();

		for(int i = 0; i < logEntries.size() ; i++){
			JSONObject jObj = new JSONObject();
			jObj.put("errorCode", new JSONString(logEntries.get(i).getLevel().name().toString()));
			jObj.put("message", new JSONString(logEntries.get(i).getMessage()));
			jArr.set(i, jObj);
		}
		requestJSON.put("listLogEntries", jArr);

        //additional screening because we have two daserialization, first when write a string, second on server side
		return requestJSON.toString().replace("\\\"","\\\\\"");*/
		// TODO: SBRFACCTAX-2494
	}

	@Override
	public void clean() {
		logEntries.clear();
		updateView();
	}

	@Override
	public void hide() {
		LogShowEvent.fire(this, false);
	}

}
