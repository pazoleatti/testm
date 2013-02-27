package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class LogAreaPresenter extends
		PresenterWidget<LogAreaPresenter.MyView> implements
		LogAddEvent.MyHandler, LogCleanEvent.MyHandler, LogAreaUiHandlers {

	
	public static interface MyView extends View, HasUiHandlers<LogAreaUiHandlers>{
		
		void setLogEntries(List<LogEntry> entries);
		void setLogSize(int full, int error, int warn, int info);

	}
	
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

	@Inject
	public LogAreaPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
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
		getView().setLogSize(logEntries.size(), error, warn, info);
	}

	@Override
	public void print() {
		Window.alert("Не реализовано");	
		// TODO: SBRFACCTAX-1414
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
